package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.components.SciFiCard
import com.example.ui.theme.ArcCyanBright
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.JarvisThemeMode
import com.example.ui.theme.LocalJarvisColors
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.StarkGold

@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
  currentTheme: JarvisThemeMode,
  customApiKey: String,
  speechPitch: Float,
  speechRate: Float,
  onThemeChanged: (JarvisThemeMode) -> Unit,
  onApiKeyChanged: (String) -> Unit,
  onPitchChanged: (Float) -> Unit,
  onRateChanged: (Float) -> Unit,
  onTestVoice: (String) -> Unit
) {
  val colors = LocalJarvisColors.current
  val scrollState = rememberScrollState()

  var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
  var pitchVal by remember(speechPitch) { mutableFloatStateOf(speechPitch) }
  var rateVal by remember(speechRate) { mutableFloatStateOf(speechRate) }

  val hasEnvKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 10.dp)
  ) {
    Text(
      text = "SYSTEM CONFIGURATION // SETTINGS",
      style = MaterialTheme.typography.titleMedium.copy(
        color = colors.textPrimary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
      )
    )
    Text(
      text = "CALIBRATE AI INTELLIGENCE, VOICE SYNTHESIS & THEMES",
      style = MaterialTheme.typography.labelSmall.copy(
        color = colors.arcPrimary,
        fontSize = 9.sp
      )
    )

    Spacer(modifier = Modifier.height(14.dp))

    // 1. Theme Selection
    SciFiCard(modifier = Modifier.fillMaxWidth()) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.ColorLens,
            contentDescription = null,
            tint = colors.arcPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "HOLOGRAPHIC THEME RESONANCE",
            style = MaterialTheme.typography.labelMedium.copy(
              color = colors.arcPrimary,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ThemeOptionItem(
            name = "ARC CYAN",
            color = ArcCyanBright,
            isSelected = currentTheme == JarvisThemeMode.ARC_CYAN,
            modifier = Modifier.weight(1f),
            onClick = { onThemeChanged(JarvisThemeMode.ARC_CYAN) }
          )
          ThemeOptionItem(
            name = "STARK GOLD",
            color = StarkGold,
            isSelected = currentTheme == JarvisThemeMode.STARK_GOLD,
            modifier = Modifier.weight(1f),
            onClick = { onThemeChanged(JarvisThemeMode.STARK_GOLD) }
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ThemeOptionItem(
            name = "CRIMSON",
            color = NeonCrimson,
            isSelected = currentTheme == JarvisThemeMode.NEON_CRIMSON,
            modifier = Modifier.weight(1f),
            onClick = { onThemeChanged(JarvisThemeMode.NEON_CRIMSON) }
          )
          ThemeOptionItem(
            name = "EMERALD",
            color = CyberEmerald,
            isSelected = currentTheme == JarvisThemeMode.CYBER_EMERALD,
            modifier = Modifier.weight(1f),
            onClick = { onThemeChanged(JarvisThemeMode.CYBER_EMERALD) }
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 2. Speech Synthesis Controls
    SciFiCard(modifier = Modifier.fillMaxWidth()) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.RecordVoiceOver,
            contentDescription = null,
            tint = colors.accentSecondary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "VOICE SYNTHESIS & ACOUSTICS",
            style = MaterialTheme.typography.labelMedium.copy(
              color = colors.accentSecondary,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Vocal Pitch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "VOCAL PITCH",
            style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted)
          )
          Text(
            text = String.format("%.2fx", pitchVal),
            style = MaterialTheme.typography.labelSmall.copy(
              color = colors.textPrimary,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          )
        }
        Slider(
          value = pitchVal,
          onValueChange = {
            pitchVal = it
            onPitchChanged(it)
          },
          valueRange = 0.8f..1.5f,
          colors = SliderDefaults.colors(
            thumbColor = colors.accentSecondary,
            activeTrackColor = colors.accentSecondary,
            inactiveTrackColor = colors.surfaceHud
          ),
          modifier = Modifier.testTag("pitch_slider")
        )

        // Speech Rate
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "TRANSMISSION RATE (SPEED)",
            style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted)
          )
          Text(
            text = String.format("%.2fx", rateVal),
            style = MaterialTheme.typography.labelSmall.copy(
              color = colors.textPrimary,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          )
        }
        Slider(
          value = rateVal,
          onValueChange = {
            rateVal = it
            onRateChanged(it)
          },
          valueRange = 0.8f..1.5f,
          colors = SliderDefaults.colors(
            thumbColor = colors.accentSecondary,
            activeTrackColor = colors.accentSecondary,
            inactiveTrackColor = colors.surfaceHud
          ),
          modifier = Modifier.testTag("rate_slider")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
          onClick = {
            onTestVoice("Acoustic calibration complete. Mark IV voice frequency synchronized.")
          },
          colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceHud),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.accentSecondary, RoundedCornerShape(8.dp))
            .testTag("test_voice_btn")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.VolumeUp,
              contentDescription = null,
              tint = colors.accentSecondary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "TEST VOICE AUDIO",
              style = MaterialTheme.typography.labelSmall.copy(
                color = colors.accentSecondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. Gemini API Key Configuration
    SciFiCard(modifier = Modifier.fillMaxWidth()) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Key,
            contentDescription = null,
            tint = colors.warningGold,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "GEMINI AI INTELLIGENCE CORE",
            style = MaterialTheme.typography.labelMedium.copy(
              color = colors.warningGold,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = if (hasEnvKey) {
            "✓ GEMINI_API_KEY detected in build configuration. Live online Gemini 3.5 Flash intelligence active."
          } else {
            "MYRAAA is running with built-in Jarvis offline intelligence. You can optionally supply your own Gemini API key below for deep generative reasoning."
          },
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (hasEnvKey) colors.healthyEmerald else colors.textMuted,
            fontSize = 11.sp
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = apiKeyInput,
          onValueChange = {
            apiKeyInput = it
            onApiKeyChanged(it)
          },
          placeholder = { Text("Enter custom Gemini API key (optional)") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.warningGold,
            unfocusedBorderColor = colors.borderNormal,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary
          ),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("api_key_input")
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun ThemeOptionItem(
  name: String,
  color: Color,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val colors = LocalJarvisColors.current
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(colors.surfaceHud)
      .border(
        1.dp,
        if (isSelected) color else colors.borderNormal,
        RoundedCornerShape(6.dp)
      )
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 8.dp)
      .testTag("theme_btn_$name"),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(12.dp)
          .clip(CircleShape)
          .background(color)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = name,
        style = MaterialTheme.typography.labelSmall.copy(
          color = if (isSelected) color else colors.textMuted,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp
        )
      )
      if (isSelected) {
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(12.dp)
        )
      }
    }
  }
}
