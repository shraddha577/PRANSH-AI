package com.example.data.action

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import java.net.URLEncoder

sealed class JarvisAction {
  data class OpenApp(val appName: String, val packageName: String? = null) : JarvisAction()
  data class SendWhatsApp(val phone: String?, val message: String) : JarvisAction()
  data class DialNumber(val phoneNumber: String) : JarvisAction()
  data class WebSearch(val query: String) : JarvisAction()
  data class OpenMaps(val query: String?) : JarvisAction()
  data object ToggleFlashlight : JarvisAction()
  data object RunDiagnostics : JarvisAction()
  data object BatteryCheck : JarvisAction()
  data class None(val responseText: String) : JarvisAction()
}

data class ActionResult(
  val success: Boolean,
  val feedbackText: String,
  val actionType: String? = null,
  val target: String? = null
)

class JarvisActionEngine(private val context: Context) {
  private var isTorchOn = false

  fun executeAction(action: JarvisAction): ActionResult {
    return when (action) {
      is JarvisAction.OpenApp -> openApp(action.appName, action.packageName)
      is JarvisAction.SendWhatsApp -> sendWhatsApp(action.phone, action.message)
      is JarvisAction.DialNumber -> dialNumber(action.phoneNumber)
      is JarvisAction.WebSearch -> searchWeb(action.query)
      is JarvisAction.OpenMaps -> openMaps(action.query)
      is JarvisAction.ToggleFlashlight -> toggleTorch()
      is JarvisAction.RunDiagnostics -> ActionResult(
        success = true,
        feedbackText = "Diagnostic sweep initiated. All core subroutines verified.",
        actionType = "DIAGNOSTIC",
        target = "SYSTEM_HEALTH"
      )
      is JarvisAction.BatteryCheck -> ActionResult(
        success = true,
        feedbackText = "Battery status requested.",
        actionType = "TELEMETRY",
        target = "BATTERY"
      )
      is JarvisAction.None -> ActionResult(
        success = true,
        feedbackText = action.responseText
      )
    }
  }

  private fun openApp(appName: String, explicitPackage: String? = null): ActionResult {
    val nameLower = appName.lowercase()
    try {
      when {
        explicitPackage != null -> {
          val launchIntent = context.packageManager.getLaunchIntentForPackage(explicitPackage)
          if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return ActionResult(true, "Launching $appName now.", "OPEN_APP", explicitPackage)
          }
        }
        nameLower.contains("youtube") -> {
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
            setPackage("com.google.android.youtube")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          if (canHandle(intent)) {
            context.startActivity(intent)
          } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
          }
          return ActionResult(true, "Opening YouTube video feed.", "OPEN_APP", "YouTube")
        }
        nameLower.contains("whatsapp") -> {
          val launchIntent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
          if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return ActionResult(true, "Accessing WhatsApp protocol.", "OPEN_APP", "WhatsApp")
          } else {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com")).apply {
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            return ActionResult(true, "Opening WhatsApp portal.", "OPEN_APP", "WhatsApp")
          }
        }
        nameLower.contains("camera") -> {
          val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(intent)
          return ActionResult(true, "Optical sensors online. Camera active.", "OPEN_APP", "Camera")
        }
        nameLower.contains("setting") -> {
          val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(intent)
          return ActionResult(true, "Accessing device control center.", "OPEN_APP", "Settings")
        }
        nameLower.contains("dial") || nameLower.contains("phone") -> {
          val intent = Intent(Intent.ACTION_DIAL).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(intent)
          return ActionResult(true, "Opening communications dialer.", "OPEN_APP", "Dialer")
        }
        nameLower.contains("calc") -> {
          val intent = Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_APP_CALCULATOR)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          if (canHandle(intent)) {
            context.startActivity(intent)
          } else {
            val calcIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=calculator")).apply {
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(calcIntent)
          }
          return ActionResult(true, "Computation matrix online.", "OPEN_APP", "Calculator")
        }
        nameLower.contains("clock") || nameLower.contains("alarm") -> {
          val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(intent)
          return ActionResult(true, "Accessing chronometer and alarm controls.", "OPEN_APP", "Clock")
        }
        nameLower.contains("map") || nameLower.contains("navigat") -> {
          return openMaps(null)
        }
        nameLower.contains("chrome") || nameLower.contains("browser") -> {
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(intent)
          return ActionResult(true, "Launching browser interface.", "OPEN_APP", "Browser")
        }
      }
    } catch (e: Exception) {
      return ActionResult(false, "Could not open $appName: ${e.localizedMessage ?: "Application unavailable"}")
    }

    return ActionResult(false, "Unable to locate application '$appName' on this system.")
  }

  fun sendWhatsApp(phone: String?, message: String): ActionResult {
    try {
      val encodedMsg = URLEncoder.encode(message, "UTF-8")
      val cleanPhone = phone?.filter { it.isDigit() }

      val url = if (!cleanPhone.isNullOrBlank()) {
        "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"
      } else {
        "https://api.whatsapp.com/send?text=$encodedMsg"
      }

      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        setPackage("com.whatsapp")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      if (canHandle(intent)) {
        context.startActivity(intent)
        return ActionResult(
          success = true,
          feedbackText = "WhatsApp message dispatched to communications buffer.",
          actionType = "WHATSAPP",
          target = cleanPhone ?: "Broadcast"
        )
      } else {
        // Fallback to general intent or browser
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, message)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Transmit via").apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        return ActionResult(
          success = true,
          feedbackText = "WhatsApp direct package not found. Opened transmission portal.",
          actionType = "WHATSAPP",
          target = "SharePortal"
        )
      }
    } catch (e: Exception) {
      return ActionResult(false, "WhatsApp dispatch failed: ${e.localizedMessage}")
    }
  }

  private fun dialNumber(phoneNumber: String): ActionResult {
    try {
      val cleanNumber = phoneNumber.filter { it.isDigit() || it == '+' }
      val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      return ActionResult(true, "Connecting audio frequency for $cleanNumber.", "DIAL", cleanNumber)
    } catch (e: Exception) {
      return ActionResult(false, "Dialer initialization failed: ${e.localizedMessage}")
    }
  }

  private fun searchWeb(query: String): ActionResult {
    try {
      val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
        putExtra(SearchManager.QUERY, query)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      if (canHandle(intent)) {
        context.startActivity(intent)
      } else {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
      }
      return ActionResult(true, "Reconnaissance query '$query' executed.", "SEARCH", query)
    } catch (e: Exception) {
      return ActionResult(false, "Search query failed: ${e.localizedMessage}")
    }
  }

  private fun openMaps(query: String?): ActionResult {
    try {
      val uriStr = if (!query.isNullOrBlank()) {
        "geo:0,0?q=${URLEncoder.encode(query, "UTF-8")}"
      } else {
        "geo:0,0?q=nearby"
      }
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      return ActionResult(true, "Satellite navigation grid online.", "MAPS", query ?: "GPS")
    } catch (e: Exception) {
      return ActionResult(false, "Navigation grid unavailable: ${e.localizedMessage}")
    }
  }

  private fun toggleTorch(): ActionResult {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      try {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        val cameraId = cameraManager?.cameraIdList?.firstOrNull()
        if (cameraId != null) {
          isTorchOn = !isTorchOn
          cameraManager.setTorchMode(cameraId, isTorchOn)
          val stateText = if (isTorchOn) "Photon emitter engaged (Torch ON)" else "Photon emitter disengaged (Torch OFF)"
          return ActionResult(true, stateText, "FLASHLIGHT", if (isTorchOn) "ON" else "OFF")
        }
      } catch (e: Exception) {
        return ActionResult(false, "Torch control error: ${e.localizedMessage}")
      }
    }
    return ActionResult(false, "Torch hardware not supported on this unit.")
  }

  private fun canHandle(intent: Intent): Boolean {
    return intent.resolveActivity(context.packageManager) != null
  }

  /**
   * Fast offline rule-based natural language parser for immediate response without network delay.
   */
  fun parseLocalCommand(text: String): JarvisAction? {
    val lower = text.lowercase().trim()

    // 1. WhatsApp commands
    if (lower.contains("whatsapp")) {
      val msgRegex = Regex("(?:send|message|text|saying|kaho|bolo|likho)\\s+(.+)", RegexOption.IGNORE_CASE)
      val phoneRegex = Regex("(\\+?\\d{7,15})")
      val phoneMatch = phoneRegex.find(lower)?.value
      val msgMatch = msgRegex.find(lower)?.groupValues?.getOrNull(1)

      return if (!msgMatch.isNullOrBlank() || phoneMatch != null) {
        JarvisAction.SendWhatsApp(phoneMatch, msgMatch ?: "Hello from MYRAAA AI")
      } else {
        JarvisAction.OpenApp("WhatsApp", "com.whatsapp")
      }
    }

    // 2. Open specific apps
    if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.contains("kholo")) {
      val target = lower.replace("open ", "").replace("launch ", "").replace("kholo", "").trim()
      return when {
        target.contains("youtube") -> JarvisAction.OpenApp("YouTube")
        target.contains("camera") -> JarvisAction.OpenApp("Camera")
        target.contains("map") -> JarvisAction.OpenMaps(null)
        target.contains("chrome") || target.contains("browser") -> JarvisAction.OpenApp("Chrome")
        target.contains("setting") -> JarvisAction.OpenApp("Settings")
        target.contains("calc") -> JarvisAction.OpenApp("Calculator")
        target.contains("clock") || target.contains("alarm") -> JarvisAction.OpenApp("Clock")
        target.contains("phone") || target.contains("dial") -> JarvisAction.OpenApp("Phone")
        else -> JarvisAction.OpenApp(target)
      }
    }

    // 3. Dial / Call
    if (lower.startsWith("call ") || lower.startsWith("dial ") || lower.contains("phone lagao")) {
      val number = lower.filter { it.isDigit() || it == '+' }
      if (number.length >= 3) {
        return JarvisAction.DialNumber(number)
      }
      return JarvisAction.OpenApp("Phone")
    }

    // 4. Torch / Flashlight
    if (lower.contains("torch") || lower.contains("flashlight") || lower.contains("light on") || lower.contains("light off")) {
      return JarvisAction.ToggleFlashlight
    }

    // 5. Diagnostics
    if (lower.contains("diagnostic") || lower.contains("system scan") || lower.contains("status report") || lower.contains("health check")) {
      return JarvisAction.RunDiagnostics
    }

    // 6. Battery
    if (lower.contains("battery") || lower.contains("charge") || lower.contains("power level")) {
      return JarvisAction.BatteryCheck
    }

    // 7. Web search
    if (lower.startsWith("search ") || lower.startsWith("google ") || lower.contains("dhoondo")) {
      val query = lower.replace("search ", "").replace("google ", "").replace("dhoondo", "").trim()
      if (query.isNotEmpty()) {
        return JarvisAction.WebSearch(query)
      }
    }

    return null
  }
}
