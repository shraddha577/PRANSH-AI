package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.action.ActionResult
import com.example.data.action.JarvisAction
import com.example.data.action.JarvisActionEngine
import com.example.data.ai.GeminiAiService
import com.example.data.db.AppDatabase
import com.example.data.db.MessageEntity
import com.example.data.telemetry.DeviceTelemetry
import com.example.data.telemetry.TelemetryProvider
import com.example.data.voice.VoiceManager
import com.example.ui.components.ReactorState
import com.example.ui.theme.JarvisThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JarvisViewModel(application: Application) : AndroidViewModel(application) {
  private val context = application.applicationContext
  private val database = AppDatabase.getDatabase(context)
  private val messageDao = database.messageDao()
  private val telemetryProvider = TelemetryProvider(context)
  private val actionEngine = JarvisActionEngine(context)
  private val geminiService = GeminiAiService()

  private val prefs = context.getSharedPreferences("myraaa_settings", Context.MODE_PRIVATE)

  // Voice manager
  private val voiceManager = VoiceManager(
    context = context,
    onSpeechRecognized = { recognizedText ->
      processUserPrompt(recognizedText)
    },
    onListeningStateChanged = { isListening ->
      if (!isListening && _reactorState.value == ReactorState.LISTENING) {
        _reactorState.value = ReactorState.IDLE
      }
    }
  )

  // UI States
  val messages: StateFlow<List<MessageEntity>> = messageDao.getAllMessages()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _telemetry = MutableStateFlow(DeviceTelemetry())
  val telemetry: StateFlow<DeviceTelemetry> = _telemetry.asStateFlow()

  private val _reactorState = MutableStateFlow(ReactorState.IDLE)
  val reactorState: StateFlow<ReactorState> = _reactorState.asStateFlow()

  val audioAmplitude: StateFlow<Float> = voiceManager.audioAmplitude
  val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking
  val isListening: StateFlow<Boolean> = voiceManager.isListening

  private val _currentStatusText = MutableStateFlow("ALL SYSTEMS OPERATIONAL // READY FOR DIRECTIVE")
  val currentStatusText: StateFlow<String> = _currentStatusText.asStateFlow()

  private val _activeTab = MutableStateFlow(0)
  val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

  private val _isTorchActive = MutableStateFlow(false)
  val isTorchActive: StateFlow<Boolean> = _isTorchActive.asStateFlow()

  private val _themeMode = MutableStateFlow(
    try {
      JarvisThemeMode.valueOf(prefs.getString("theme_mode", JarvisThemeMode.ARC_CYAN.name) ?: JarvisThemeMode.ARC_CYAN.name)
    } catch (_: Exception) {
      JarvisThemeMode.ARC_CYAN
    }
  )
  val themeMode: StateFlow<JarvisThemeMode> = _themeMode.asStateFlow()

  private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
  val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

  private val _speechPitch = MutableStateFlow(prefs.getFloat("speech_pitch", 1.05f))
  val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

  private val _speechRate = MutableStateFlow(prefs.getFloat("speech_rate", 1.02f))
  val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

  init {
    voiceManager.speechPitch = _speechPitch.value
    voiceManager.speechRate = _speechRate.value

    // Periodic telemetry update
    viewModelScope.launch(Dispatchers.Default) {
      while (true) {
        val latest = telemetryProvider.getLatestTelemetry()
        _telemetry.value = latest
        delay(3000)
      }
    }

    // Monitor speaking state to update reactor state
    viewModelScope.launch {
      voiceManager.isSpeaking.collect { speaking ->
        if (speaking) {
          _reactorState.value = ReactorState.SPEAKING
        } else if (_reactorState.value == ReactorState.SPEAKING) {
          _reactorState.value = ReactorState.IDLE
        }
      }
    }

    // Initial greeting if no messages
    viewModelScope.launch {
      val count = withContext(Dispatchers.IO) { messageDao.getMessageCount() }
      if (count == 0) {
        val greeting = MessageEntity(
          text = "Systems online. Mark IV holographic interface operational. Good day, Operator. MYRAAA is at your command.",
          sender = "MYRAAA",
          actionType = "SYSTEM",
          actionTarget = "BOOT_COMPLETE"
        )
        withContext(Dispatchers.IO) { messageDao.insertMessage(greeting) }
      }
    }
  }

  fun setActiveTab(tab: Int) {
    _activeTab.value = tab
  }

  fun toggleVoiceListening() {
    if (voiceManager.isListening.value) {
      voiceManager.stopListening()
      _reactorState.value = ReactorState.IDLE
    } else {
      voiceManager.stopSpeaking()
      _reactorState.value = ReactorState.LISTENING
      _currentStatusText.value = "LISTENING FOR VOCAL DIRECTIVE…"
      voiceManager.startListening()
    }
  }

  fun stopSpeaking() {
    voiceManager.stopSpeaking()
    _reactorState.value = ReactorState.IDLE
  }

  fun speakText(text: String) {
    voiceManager.speak(text)
  }

  fun processUserPrompt(prompt: String) {
    val cleanPrompt = prompt.trim()
    if (cleanPrompt.isBlank()) return

    viewModelScope.launch {
      // 1. Save user query in DB
      val userMsg = MessageEntity(
        text = cleanPrompt,
        sender = "USER"
      )
      withContext(Dispatchers.IO) { messageDao.insertMessage(userMsg) }

      _reactorState.value = ReactorState.PROCESSING
      _currentStatusText.value = "PROCESSING DIRECTIVE: \"$cleanPrompt\""

      // 2. Query AI Service (handles online Gemini 3.5 Flash or local Jarvis engine)
      val aiResult = geminiService.askGemini(cleanPrompt, _customApiKey.value)

      // 3. If action detected, execute it
      var actionExecResult: ActionResult? = null
      val actionToExecute = aiResult.detectedAction ?: actionEngine.parseLocalCommand(cleanPrompt)
      if (actionToExecute != null) {
        actionExecResult = withContext(Dispatchers.Main) {
          actionEngine.executeAction(actionToExecute)
        }
      }

      val finalText = if (actionExecResult != null && actionExecResult.feedbackText.isNotBlank()) {
        "${aiResult.spokenText}\n[${actionExecResult.feedbackText}]"
      } else {
        aiResult.spokenText
      }

      // 4. Save AI Response
      val aiMsg = MessageEntity(
        text = finalText,
        sender = "MYRAAA",
        actionType = actionExecResult?.actionType,
        actionTarget = actionExecResult?.target
      )
      withContext(Dispatchers.IO) { messageDao.insertMessage(aiMsg) }

      _currentStatusText.value = "RESPONSE TRANSMITTED"

      // 5. Vocalize response
      voiceManager.speak(aiResult.spokenText)
    }
  }

  fun executeDirectAction(action: JarvisAction) {
    viewModelScope.launch {
      val result = withContext(Dispatchers.Main) {
        actionEngine.executeAction(action)
      }
      val msg = MessageEntity(
        text = result.feedbackText,
        sender = "MYRAAA",
        actionType = result.actionType,
        actionTarget = result.target
      )
      withContext(Dispatchers.IO) { messageDao.insertMessage(msg) }
      voiceManager.speak(result.feedbackText)
    }
  }

  fun toggleTorch() {
    val result = actionEngine.executeAction(JarvisAction.ToggleFlashlight)
    _isTorchActive.value = result.target == "ON"
    viewModelScope.launch {
      val msg = MessageEntity(
        text = result.feedbackText,
        sender = "SYSTEM",
        actionType = "FLASHLIGHT",
        actionTarget = result.target
      )
      withContext(Dispatchers.IO) { messageDao.insertMessage(msg) }
      voiceManager.speak(result.feedbackText)
    }
  }

  fun runDiagnosticsSweep() {
    viewModelScope.launch {
      _reactorState.value = ReactorState.PROCESSING
      _currentStatusText.value = "RUNNING FULL TACTICAL SCAN…"
      delay(800)
      val telemetry = telemetryProvider.getLatestTelemetry()
      _telemetry.value = telemetry

      val report = "Diagnostic scan complete. Power cells at ${telemetry.batteryLevel}%, temperature nominal at ${telemetry.batteryTempCelsius}°C. Memory load at ${telemetry.usedRamPercent}%. Defense matrices secure."
      val msg = MessageEntity(
        text = report,
        sender = "MYRAAA",
        actionType = "DIAGNOSTIC",
        actionTarget = "ALL_SYSTEMS"
      )
      withContext(Dispatchers.IO) { messageDao.insertMessage(msg) }
      _currentStatusText.value = "DIAGNOSTICS NOMINAL"
      voiceManager.speak(report)
    }
  }

  fun clearLogs() {
    viewModelScope.launch(Dispatchers.IO) {
      messageDao.clearAll()
    }
  }

  fun updateTheme(newTheme: JarvisThemeMode) {
    _themeMode.value = newTheme
    prefs.edit().putString("theme_mode", newTheme.name).apply()
  }

  fun updateApiKey(apiKey: String) {
    _customApiKey.value = apiKey
    prefs.edit().putString("custom_api_key", apiKey).apply()
  }

  fun updateSpeechPitch(pitch: Float) {
    _speechPitch.value = pitch
    voiceManager.speechPitch = pitch
    prefs.edit().putFloat("speech_pitch", pitch).apply()
  }

  fun updateSpeechRate(rate: Float) {
    _speechRate.value = rate
    voiceManager.speechRate = rate
    prefs.edit().putFloat("speech_rate", rate).apply()
  }

  override fun onCleared() {
    super.onCleared()
    voiceManager.shutdown()
  }
}
