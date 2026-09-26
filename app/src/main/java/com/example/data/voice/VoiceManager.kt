package com.example.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(
  private val context: Context,
  private val onSpeechRecognized: (String) -> Unit,
  private val onListeningStateChanged: (Boolean) -> Unit
) {
  private var speechRecognizer: SpeechRecognizer? = null
  private var textToSpeech: TextToSpeech? = null
  private var isTtsReady = false

  private val _audioAmplitude = MutableStateFlow(0f)
  val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _isListening = MutableStateFlow(false)
  val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

  var speechPitch: Float = 1.05f
    set(value) {
      field = value
      textToSpeech?.setPitch(value)
    }

  var speechRate: Float = 1.02f
    set(value) {
      field = value
      textToSpeech?.setSpeechRate(value)
    }

  init {
    initTts()
  }

  private fun initTts() {
    textToSpeech = TextToSpeech(context) { status ->
      if (status == TextToSpeech.SUCCESS) {
        val result = textToSpeech?.setLanguage(Locale.US)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
          textToSpeech?.setLanguage(Locale.getDefault())
        }
        textToSpeech?.setPitch(speechPitch)
        textToSpeech?.setSpeechRate(speechRate)
        isTtsReady = true

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
          override fun onStart(utteranceId: String?) {
            _isSpeaking.value = true
          }

          override fun onDone(utteranceId: String?) {
            _isSpeaking.value = false
            _audioAmplitude.value = 0f
          }

          @Deprecated("Deprecated in Java")
          override fun onError(utteranceId: String?) {
            _isSpeaking.value = false
            _audioAmplitude.value = 0f
          }
        })
      }
    }
  }

  fun startListening() {
    stopSpeaking()
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      onSpeechRecognized("Voice recognition hardware is not available on this device.")
      return
    }

    Handler(Looper.getMainLooper()).post {
      try {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
          putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
          putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            onListeningStateChanged(true)
          }

          override fun onBeginningOfSpeech() {}

          override fun onRmsChanged(rmsdB: Float) {
            // rmsdB typically ranges from -2 to 10
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _audioAmplitude.value = normalized
          }

          override fun onBufferReceived(buffer: ByteArray?) {}

          override fun onEndOfSpeech() {
            _isListening.value = false
            onListeningStateChanged(false)
            _audioAmplitude.value = 0f
          }

          override fun onError(error: Int) {
            _isListening.value = false
            onListeningStateChanged(false)
            _audioAmplitude.value = 0f
          }

          override fun onResults(results: Bundle?) {
            _isListening.value = false
            onListeningStateChanged(false)
            _audioAmplitude.value = 0f
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull()?.trim()
            if (!recognizedText.isNullOrEmpty()) {
              onSpeechRecognized(recognizedText)
            }
          }

          override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partial = matches?.firstOrNull()?.trim()
            if (!partial.isNullOrEmpty()) {
              // Could preview live transcription
            }
          }

          override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
      } catch (e: Exception) {
        _isListening.value = false
        onListeningStateChanged(false)
      }
    }
  }

  fun stopListening() {
    Handler(Looper.getMainLooper()).post {
      try {
        speechRecognizer?.stopListening()
      } catch (_: Exception) {}
      _isListening.value = false
      onListeningStateChanged(false)
      _audioAmplitude.value = 0f
    }
  }

  fun speak(text: String) {
    if (!isTtsReady || text.isBlank()) return
    // Clean sci-fi action tags from spoken audio so only clean speech is spoken
    val cleanSpeech = text
      .replace(Regex("\\[ACTION:[^\\]]+\\]"), "")
      .replace(Regex("[*#`_]"), "")
      .trim()

    if (cleanSpeech.isEmpty()) return

    _isSpeaking.value = true
    val params = Bundle().apply {
      putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MYRAAA_VOICE_${System.currentTimeMillis()}")
    }
    textToSpeech?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, params, "MYRAAA_VOICE")
  }

  fun stopSpeaking() {
    if (_isSpeaking.value) {
      textToSpeech?.stop()
      _isSpeaking.value = false
      _audioAmplitude.value = 0f
    }
  }

  fun shutdown() {
    try {
      speechRecognizer?.destroy()
      textToSpeech?.stop()
      textToSpeech?.shutdown()
    } catch (_: Exception) {}
  }
}
