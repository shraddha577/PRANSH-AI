package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JarvisViewModel
import com.example.ui.components.SciFiHudHeader
import com.example.ui.screens.ArcReactorScreen
import com.example.ui.screens.CommandMatrixScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TelemetryDetailScreen
import com.example.ui.screens.TerminalLogScreen
import com.example.ui.theme.LocalJarvisColors
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: JarvisViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

      MyApplicationTheme(themeMode = themeMode) {
        JarvisMainApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun JarvisMainApp(viewModel: JarvisViewModel) {
  val context = LocalContext.current
  val colors = LocalJarvisColors.current

  val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
  val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
  val reactorState by viewModel.reactorState.collectAsStateWithLifecycle()
  val audioAmplitude by viewModel.audioAmplitude.collectAsStateWithLifecycle()
  val currentStatusText by viewModel.currentStatusText.collectAsStateWithLifecycle()
  val messages by viewModel.messages.collectAsStateWithLifecycle()
  val isListening by viewModel.isListening.collectAsStateWithLifecycle()
  val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
  val isTorchActive by viewModel.isTorchActive.collectAsStateWithLifecycle()
  val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
  val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
  val speechPitch by viewModel.speechPitch.collectAsStateWithLifecycle()
  val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()

  // Audio permission launcher
  val audioPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      viewModel.toggleVoiceListening()
    }
  }

  fun requestMicAndListen() {
    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
      viewModel.toggleVoiceListening()
    } else {
      audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  // Handle hardware back button to return to HUD tab
  if (activeTab != 0) {
    BackHandler {
      viewModel.setActiveTab(0)
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(colors.backgroundVoid),
    topBar = {
      Box(modifier = Modifier.statusBarsPadding()) {
        SciFiHudHeader(
          telemetry = telemetry,
          isTorchActive = isTorchActive,
          onTorchToggle = { viewModel.toggleTorch() },
          onTelemetryClick = { viewModel.setActiveTab(3) }
        )
      }
    },
    bottomBar = {
      NavigationBar(
        containerColor = colors.surfaceHud,
        contentColor = colors.textPrimary,
        modifier = Modifier
          .navigationBarsPadding()
          .border(1.dp, colors.borderNormal.copy(alpha = 0.5f))
      ) {
        val navItems = listOf(
          Triple(0, "CORE HUD", Icons.Default.DonutLarge),
          Triple(1, "MATRIX", Icons.Default.GridView),
          Triple(2, "TERMINAL", Icons.Default.Terminal),
          Triple(3, "TELEMETRY", Icons.Default.Memory),
          Triple(4, "CONFIG", Icons.Default.Settings)
        )

        navItems.forEach { (index, title, icon) ->
          val isSelected = activeTab == index
          NavigationBarItem(
            selected = isSelected,
            onClick = { viewModel.setActiveTab(index) },
            icon = {
              Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(20.dp)
              )
            },
            label = {
              Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 0.5.sp
                )
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.Black,
              selectedTextColor = colors.arcPrimary,
              indicatorColor = colors.arcPrimary,
              unselectedIconColor = colors.textMuted,
              unselectedTextColor = colors.textMuted
            ),
            modifier = Modifier.testTag("nav_tab_$index")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(colors.backgroundVoid)
        .padding(innerPadding)
    ) {
      when (activeTab) {
        0 -> ArcReactorScreen(
          reactorState = reactorState,
          audioAmplitude = audioAmplitude,
          statusText = currentStatusText,
          latestMessage = messages.lastOrNull(),
          isListening = isListening,
          isSpeaking = isSpeaking,
          onMicTapped = { requestMicAndListen() },
          onStopSpeaking = { viewModel.stopSpeaking() },
          onActionTriggered = { viewModel.executeDirectAction(it) },
          onRunDiagnostics = { viewModel.runDiagnosticsSweep() }
        )
        1 -> CommandMatrixScreen(
          onActionTriggered = { viewModel.executeDirectAction(it) },
          onRunDiagnostics = { viewModel.runDiagnosticsSweep() },
          onToggleTorch = { viewModel.toggleTorch() }
        )
        2 -> TerminalLogScreen(
          messages = messages,
          isListening = isListening,
          onSendMessage = { viewModel.processUserPrompt(it) },
          onMicTapped = { requestMicAndListen() },
          onSpeakAgain = { viewModel.speakText(it) },
          onClearLogs = { viewModel.clearLogs() }
        )
        3 -> TelemetryDetailScreen(
          telemetry = telemetry
        )
        4 -> SettingsScreen(
          currentTheme = themeMode,
          customApiKey = customApiKey,
          speechPitch = speechPitch,
          speechRate = speechRate,
          onThemeChanged = { viewModel.updateTheme(it) },
          onApiKeyChanged = { viewModel.updateApiKey(it) },
          onPitchChanged = { viewModel.updateSpeechPitch(it) },
          onRateChanged = { viewModel.updateSpeechRate(it) },
          onTestVoice = { viewModel.speakText(it) }
        )
      }
    }
  }
}
