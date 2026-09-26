package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.MessageEntity
import com.example.ui.theme.LocalJarvisColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TerminalLogItem(
  message: MessageEntity,
  onSpeakAgain: (String) -> Unit = {}
) {
  val colors = LocalJarvisColors.current
  val context = LocalContext.current
  val isMyraaa = message.sender == "MYRAAA"
  val isSystem = message.sender == "SYSTEM"

  val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  val timeString = timeFormatter.format(Date(message.timestamp))

  val borderColor = when {
    isMyraaa -> colors.borderNormal
    isSystem -> colors.warningGold.copy(alpha = 0.6f)
    else -> colors.accentSecondary.copy(alpha = 0.5f)
  }

  val bgColor = when {
    isMyraaa -> colors.surfaceCard.copy(alpha = 0.85f)
    isSystem -> colors.warningGold.copy(alpha = 0.1f)
    else -> colors.surfaceHud.copy(alpha = 0.7f)
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(bgColor)
      .border(1.dp, borderColor, RoundedCornerShape(8.dp))
      .padding(12.dp)
  ) {
    Column {
      // Header: Sender, Timestamp, and Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (isMyraaa) Icons.Default.SmartToy else Icons.Default.Terminal,
            contentDescription = null,
            tint = if (isMyraaa) colors.arcPrimary else if (isSystem) colors.warningGold else colors.accentSecondary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = when {
              isMyraaa -> "MYRAAA // AI"
              isSystem -> "SYSTEM // PROTOCOL"
              else -> "OPERATOR // COMMAND"
            },
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isMyraaa) colors.arcPrimary else if (isSystem) colors.warningGold else colors.accentSecondary,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "[$timeString]",
            style = MaterialTheme.typography.labelSmall.copy(
              color = colors.textMuted.copy(alpha = 0.6f),
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp
            )
          )
        }

        // Action Buttons: Speak & Copy
        Row {
          if (isMyraaa) {
            IconButton(
              onClick = { onSpeakAgain(message.text) },
              modifier = Modifier
                .size(28.dp)
                .testTag("speak_again_${message.id}")
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Speak Again",
                tint = colors.arcPrimary,
                modifier = Modifier.size(16.dp)
              )
            }
          }
          IconButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("MYRAAA Message", message.text)
              clipboard.setPrimaryClip(clip)
            },
            modifier = Modifier
              .size(28.dp)
              .testTag("copy_message_${message.id}")
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy Message",
              tint = colors.textMuted,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Main Message Text
      Text(
        text = message.text,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = colors.textPrimary,
          lineHeight = 20.sp
        )
      )

      // Action Tag Badge (if an action was performed)
      if (!message.actionType.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(colors.arcPrimary.copy(alpha = 0.15f))
            .border(1.dp, colors.arcPrimary.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "EXECUTED: ${message.actionType} ${if (!message.actionTarget.isNullOrBlank()) "-> ${message.actionTarget}" else ""}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = colors.arcPrimary,
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }
    }
  }
}
