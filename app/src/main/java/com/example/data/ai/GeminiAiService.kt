package com.example.data.ai

import com.example.BuildConfig
import com.example.data.action.JarvisAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiResponse(
  val spokenText: String,
  val detectedAction: JarvisAction? = null,
  val isOfflineFallback: Boolean = false
)

class GeminiAiService {
  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val systemPrompt = """
    You are MYRAAA, an advanced holographic AI mobile assistant created for Android, inspired by Tony Stark's JARVIS.
    You possess high intelligence, poise, efficiency, and slight dry sci-fi wit.
    You speak and understand English, Hindi, and Hinglish fluently. Match the user's language style.
    Keep your spoken responses concise (1 to 3 punchy sentences), like a high-tech assistant giving tactical debriefs.
    
    If the user's intent is to control the device or launch apps, respond in character AND append one of these exact action tags at the very end of your message:
    - Launching YouTube: [ACTION:OPEN_APP, TARGET:youtube]
    - Launching WhatsApp: [ACTION:OPEN_APP, TARGET:whatsapp]
    - Sending WhatsApp message: [ACTION:WHATSAPP, TARGET:recipient, MSG:text]
    - Launching Camera: [ACTION:OPEN_APP, TARGET:camera]
    - Launching Maps: [ACTION:MAPS, TARGET:location]
    - Calling someone: [ACTION:CALL, TARGET:number]
    - Searching web/Google: [ACTION:SEARCH, QUERY:query]
    - Device health / Battery check: [ACTION:DIAGNOSTIC]
    - Flashlight / Torch: [ACTION:FLASHLIGHT]
    
    If no device action is needed, do not add an action tag.
  """.trimIndent()

  suspend fun askGemini(
    userPrompt: String,
    customApiKey: String? = null
  ): AiResponse = withContext(Dispatchers.IO) {
    val apiKey = when {
      !customApiKey.isNullOrBlank() -> customApiKey.trim()
      BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
      else -> null
    }

    if (apiKey.isNullOrEmpty()) {
      return@withContext generateLocalJarvisResponse(userPrompt)
    }

    try {
      val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

      val jsonBody = JSONObject().apply {
        put("systemInstruction", JSONObject().apply {
          put("parts", JSONArray().apply {
            put(JSONObject().apply { put("text", systemPrompt) })
          })
        })
        put("contents", JSONArray().apply {
          put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
              put(JSONObject().apply { put("text", userPrompt) })
            })
          })
        })
        put("generationConfig", JSONObject().apply {
          put("temperature", 0.7)
          put("maxOutputTokens", 500)
        })
      }

      val request = Request.Builder()
        .url(url)
        .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = okHttpClient.newCall(request).execute()
      val responseString = response.body?.string()

      if (!response.isSuccessful || responseString.isNullOrBlank()) {
        return@withContext generateLocalJarvisResponse(userPrompt)
      }

      val rootJson = JSONObject(responseString)
      val candidates = rootJson.optJSONArray("candidates")
      val firstCandidate = candidates?.optJSONObject(0)
      val content = firstCandidate?.optJSONObject("content")
      val parts = content?.optJSONArray("parts")
      val rawText = parts?.optJSONObject(0)?.optString("text", "") ?: ""

      if (rawText.isBlank()) {
        return@withContext generateLocalJarvisResponse(userPrompt)
      }

      parseAiTextAndAction(rawText)
    } catch (e: Exception) {
      generateLocalJarvisResponse(userPrompt)
    }
  }

  private fun parseAiTextAndAction(rawText: String): AiResponse {
    var detectedAction: JarvisAction? = null
    val actionRegex = Regex("\\[ACTION:([A-Z_]+)(?:,\\s*TARGET:([^,\\]]+))?(?:,\\s*MSG:([^,\\]]+))?(?:,\\s*QUERY:([^,\\]]+))?\\]")
    val match = actionRegex.find(rawText)

    if (match != null) {
      val actionType = match.groupValues.getOrNull(1)
      val target = match.groupValues.getOrNull(2)?.trim()
      val msg = match.groupValues.getOrNull(3)?.trim()
      val query = match.groupValues.getOrNull(4)?.trim()

      detectedAction = when (actionType) {
        "OPEN_APP" -> JarvisAction.OpenApp(target ?: "App")
        "WHATSAPP" -> JarvisAction.SendWhatsApp(target, msg ?: "Hello")
        "MAPS" -> JarvisAction.OpenMaps(target)
        "CALL" -> JarvisAction.DialNumber(target ?: "")
        "SEARCH" -> JarvisAction.WebSearch(query ?: target ?: "")
        "DIAGNOSTIC" -> JarvisAction.RunDiagnostics
        "FLASHLIGHT" -> JarvisAction.ToggleFlashlight
        else -> null
      }
    }

    val cleanSpoken = rawText.replace(actionRegex, "").trim()
    return AiResponse(
      spokenText = cleanSpoken,
      detectedAction = detectedAction,
      isOfflineFallback = false
    )
  }

  /**
   * High-fidelity local JARVIS offline fallback when internet is down or API key is not yet set.
   */
  private fun generateLocalJarvisResponse(query: String): AiResponse {
    val q = query.lowercase().trim()

    val (reply, action) = when {
      q.contains("who are you") || q.contains("kaun ho") || q.contains("identity") -> {
        "I am MYRAAA, your Mark IV holographic AI assistant. All subroutines and optical sensors are operating at peak efficiency." to null
      }
      q.contains("youtube") -> {
        "Accessing the global video network. Launching YouTube feed now." to JarvisAction.OpenApp("YouTube")
      }
      q.contains("whatsapp") -> {
        if (q.contains("send") || q.contains("msg") || q.contains("message")) {
          "Preparing WhatsApp communication stream. Transmitting buffer now." to JarvisAction.SendWhatsApp(null, "Transmitted via MYRAAA AI")
        } else {
          "Opening WhatsApp protocol." to JarvisAction.OpenApp("WhatsApp", "com.whatsapp")
        }
      }
      q.contains("camera") || q.contains("photo") -> {
        "Optical lens array activated. Camera ready." to JarvisAction.OpenApp("Camera")
      }
      q.contains("battery") || q.contains("charge") -> {
        "Power cells are nominal. Core telemetry updated on your HUD." to JarvisAction.BatteryCheck
      }
      q.contains("diagnostic") || q.contains("scan") || q.contains("health") -> {
        "Executing tactical diagnostic sweep. All defense algorithms and memory blocks are secure." to JarvisAction.RunDiagnostics
      }
      q.contains("torch") || q.contains("flashlight") || q.contains("light") -> {
        "Toggling photon emitter." to JarvisAction.ToggleFlashlight
      }
      q.contains("map") || q.contains("location") || q.contains("direction") -> {
        "Satellite telemetry synchronized. Displaying tactical maps." to JarvisAction.OpenMaps(null)
      }
      q.contains("search") || q.contains("google") -> {
        val searchTerm = q.replace("search", "").replace("google", "").trim()
        "Scanning global databanks for $searchTerm." to JarvisAction.WebSearch(searchTerm)
      }
      q.contains("call") || q.contains("phone") -> {
        val number = q.filter { it.isDigit() || it == '+' }
        "Connecting audio transmission." to if (number.isNotEmpty()) JarvisAction.DialNumber(number) else JarvisAction.OpenApp("Phone")
      }
      q.contains("hello") || q.contains("hi") || q.contains("hey") || q.contains("namaste") -> {
        "At your service. Systems are online and ready for your directive." to null
      }
      q.contains("how are you") || q.contains("kaise ho") -> {
        "All Mark IV subsystems are operating at 100% capacity. Ready for commands." to null
      }
      else -> {
        "Acknowledged. Analyzing directive '$query'. Ready for further instruction." to null
      }
    }

    return AiResponse(
      spokenText = reply,
      detectedAction = action,
      isOfflineFallback = true
    )
  }
}
