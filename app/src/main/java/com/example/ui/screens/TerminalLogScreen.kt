package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.db.MessageEntity
import com.example.ui.components.TerminalLogItem
import com.example.ui.theme.LocalJarvisColors

@Composable
fun TerminalLogScreen(
  modifier: Modifier = Modifier,
  messages: List<MessageEntity>,
  isListening: Boolean,
  onSendMessage: (String) -> Unit,
  onMicTapped: () -> Unit,
  onSpeakAgain: (String) -> Unit,
  onClearLogs: () -> Unit
) {
  val colors = LocalJarvisColors.current
  var textInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  // Auto-scroll to bottom on new message
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Header Row: Title & Clear History
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "MISSION LOG & TELETYPE",
          style = MaterialTheme.typography.titleMedium.copy(
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
          )
        )
        Text(
          text = "${messages.size} DIRECTIVES RECORDED IN LOCAL MEMORY",
          style = MaterialTheme.typography.labelSmall.copy(
            color = colors.arcPrimary,
            fontSize = 9.sp
          )
        )
      }

      IconButton(
        onClick = onClearLogs,
        modifier = Modifier.testTag("clear_logs_button")
      ) {
        Icon(
          imageVector = Icons.Default.DeleteSweep,
          contentDescription = "Clear History",
          tint = colors.textMuted
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Messages Stream
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      if (messages.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "NO TELEMETRY LOGGED.\nSPEAK OR TRANSMIT A COMMAND TO INITIATE.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = colors.textMuted,
              fontFamily = FontFamily.Monospace
            )
          )
        }
      } else {
        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize()
        ) {
          items(messages, key = { it.id }) { message ->
            TerminalLogItem(
              message = message,
              onSpeakAgain = onSpeakAgain
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Command Input Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(colors.surfaceCard)
        .border(1.dp, colors.borderNormal, RoundedCornerShape(24.dp))
        .padding(horizontal = 6.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Voice dictation mic button
      IconButton(
        onClick = onMicTapped,
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(if (isListening) colors.arcPrimary else Color.Transparent)
          .testTag("terminal_mic_button")
      ) {
        Icon(
          imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
          contentDescription = "Dictate Speech",
          tint = if (isListening) Color.Black else colors.arcPrimary,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      OutlinedTextField(
        value = textInput,
        onValueChange = { textInput = it },
        placeholder = {
          Text(
            text = if (isListening) "Listening to voice input…" else "Command MYRAAA (English / Hindi)…",
            style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
          )
        },
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = Color.Transparent,
          unfocusedBorderColor = Color.Transparent,
          focusedTextColor = colors.textPrimary,
          unfocusedTextColor = colors.textPrimary,
          cursorColor = colors.arcPrimary
        ),
        singleLine = true,
        modifier = Modifier
          .weight(1f)
          .testTag("command_text_input")
      )

      // Send Button
      IconButton(
        onClick = {
          val query = textInput.trim()
          if (query.isNotEmpty()) {
            onSendMessage(query)
            textInput = ""
          }
        },
        enabled = textInput.isNotBlank(),
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(if (textInput.isNotBlank()) colors.arcPrimary else colors.surfaceHud)
          .testTag("terminal_send_button")
      ) {
        Icon(
          imageVector = Icons.Default.Send,
          contentDescription = "Transmit Command",
          tint = if (textInput.isNotBlank()) Color.Black else colors.textMuted,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
