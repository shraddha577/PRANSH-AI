package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.action.JarvisAction
import com.example.data.db.MessageEntity
import com.example.ui.components.ArcReactorView
import com.example.ui.components.AudioWaveformBar
import com.example.ui.components.ReactorState
import com.example.ui.components.SciFiCard
import com.example.ui.theme.LocalJarvisColors

@Composable
fun ArcReactorScreen(
  modifier: Modifier = Modifier,
  reactorState: ReactorState,
  audioAmplitude: Float,
  statusText: String,
  latestMessage: MessageEntity?,
  isListening: Boolean,
  isSpeaking: Boolean,
  onMicTapped: () -> Unit,
  onStopSpeaking: () -> Unit,
  onActionTriggered: (JarvisAction) -> Unit,
  onRunDiagnostics: () -> Unit
) {
  val colors = LocalJarvisColors.current
  val scrollState = rememberScrollState()

  // Pulsing outer glow for the voice command button
  val infiniteTransition = rememberInfiniteTransition(label = "VoiceButtonPulse")
  val buttonPulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 700, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "PulseScale"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // 1. Status Bar Banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(6.dp))
        .background(colors.surfaceHud)
        .border(1.dp, colors.borderNormal, RoundedCornerShape(6.dp))
        .padding(horizontal = 12.dp, vertical = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        val stateIndicatorColor = when (reactorState) {
          ReactorState.LISTENING -> colors.arcPrimary
          ReactorState.PROCESSING -> colors.warningGold
          ReactorState.SPEAKING -> colors.accentSecondary
          ReactorState.IDLE -> colors.healthyEmerald
        }
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(stateIndicatorColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = statusText.uppercase(),
          style = MaterialTheme.typography.labelSmall.copy(
            color = colors.textPrimary,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          ),
          textAlign = TextAlign.Center,
          maxLines = 1
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. 3D Holographic Arc Reactor (Interactive Touch Gestures)
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.padding(vertical = 8.dp)
    ) {
      ArcReactorView(
        size = 280.dp,
        reactorState = reactorState,
        audioAmplitude = audioAmplitude,
        onCoreTapped = onMicTapped
      )
    }

    Text(
      text = "DRAG TO ROTATE 3D CORE // TAP TO COMMAND",
      style = MaterialTheme.typography.labelSmall.copy(
        color = colors.textMuted.copy(alpha = 0.7f),
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp,
        letterSpacing = 1.sp
      )
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 3. Audio Equalizer Waveform Bar
    AudioWaveformBar(
      isActive = isListening || isSpeaking || reactorState == ReactorState.PROCESSING,
      amplitude = audioAmplitude,
      height = 30.dp,
      barCount = 28
    )

    Spacer(modifier = Modifier.height(12.dp))

    // 4. Latest Intel / Transmission Card (if available)
    if (latestMessage != null) {
      SciFiCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (latestMessage.sender == "MYRAAA") colors.borderActive else colors.borderNormal,
        cornerCut = 8.dp
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (latestMessage.sender == "MYRAAA") "LATEST TRANSMISSION // MYRAAA" else "OPERATOR DIRECTIVE",
              style = MaterialTheme.typography.labelSmall.copy(
                color = colors.arcPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            )
            if (isSpeaking) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(colors.alertCrimson.copy(alpha = 0.2f))
                  .border(1.dp, colors.alertCrimson, RoundedCornerShape(4.dp))
                  .clickable { onStopSpeaking() }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Mute Voice",
                    tint = colors.alertCrimson,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "MUTE",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = colors.alertCrimson,
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Bold
                    )
                  )
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = latestMessage.text,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = colors.textPrimary,
              lineHeight = 18.sp
            ),
            maxLines = 4
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 5. Quick Protocol Action Chips (Horizontal Scroll)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      QuickProtocolChip(
        label = "DIAGNOSTIC SCAN",
        icon = Icons.Default.Security,
        onClick = onRunDiagnostics
      )
      QuickProtocolChip(
        label = "WHATSAPP PROTOCOL",
        icon = Icons.Default.Send,
        onClick = { onActionTriggered(JarvisAction.OpenApp("WhatsApp", "com.whatsapp")) }
      )
      QuickProtocolChip(
        label = "OPEN YOUTUBE",
        icon = Icons.Default.PlayArrow,
        onClick = { onActionTriggered(JarvisAction.OpenApp("YouTube")) }
      )
      QuickProtocolChip(
        label = "OPTICAL CAMERA",
        icon = Icons.Default.CameraAlt,
        onClick = { onActionTriggered(JarvisAction.OpenApp("Camera")) }
      )
      QuickProtocolChip(
        label = "SATELLITE MAPS",
        icon = Icons.Default.Map,
        onClick = { onActionTriggered(JarvisAction.OpenMaps(null)) }
      )
      QuickProtocolChip(
        label = "GLOBAL RECON",
        icon = Icons.Default.Language,
        onClick = { onActionTriggered(JarvisAction.WebSearch("AI Technology News")) }
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 6. Central Voice Trigger Button with Hologram Glow Ring
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.padding(bottom = 12.dp)
    ) {
      // Pulsing outer ripple ring when listening
      if (isListening) {
        Box(
          modifier = Modifier
            .size((72.dp.value * buttonPulseScale).dp)
            .clip(CircleShape)
            .background(colors.arcGlow.copy(alpha = 0.4f))
        )
      }

      // Main Voice Action Button
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(CircleShape)
          .background(if (isListening) colors.arcPrimary else colors.surfaceCard)
          .border(
            2.dp,
            if (isListening) colors.textPrimary else colors.borderActive,
            CircleShape
          )
          .clickable { onMicTapped() }
          .testTag("voice_command_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
          contentDescription = if (isListening) "Stop Listening" else "Start Voice Command",
          tint = if (isListening) Color.Black else colors.arcPrimary,
          modifier = Modifier.size(32.dp)
        )
      }
    }

    Text(
      text = if (isListening) "JARVIS LISTENING… SPEAK NOW" else "TAP MIC TO ENGAGE MYRAAA",
      style = MaterialTheme.typography.labelSmall.copy(
        color = if (isListening) colors.arcPrimary else colors.textMuted,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
    )
  }
}

@Composable
private fun QuickProtocolChip(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit
) {
  val colors = LocalJarvisColors.current
  Box(
    modifier = Modifier
      .clip(CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
      .background(colors.surfaceCard)
      .border(1.dp, colors.borderNormal, CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
      .clickable { onClick() }
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = colors.arcPrimary,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
          color = colors.textPrimary,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold
        )
      )
    }
  }
}
