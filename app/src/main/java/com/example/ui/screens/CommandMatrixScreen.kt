package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.action.JarvisAction
import com.example.ui.components.SciFiCard
import com.example.ui.theme.LocalJarvisColors

data class ProtocolItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val action: JarvisAction
)

@Composable
fun CommandMatrixScreen(
  modifier: Modifier = Modifier,
  onActionTriggered: (JarvisAction) -> Unit,
  onRunDiagnostics: () -> Unit,
  onToggleTorch: () -> Unit
) {
  val colors = LocalJarvisColors.current

  var showWhatsAppDialog by remember { mutableStateOf(false) }
  var showSearchDialog by remember { mutableStateOf(false) }

  val protocols = remember {
    listOf(
      ProtocolItem(
        id = "diag",
        title = "DIAGNOSTIC SWEEP",
        subtitle = "Full hardware & defense check",
        icon = Icons.Default.Security,
        action = JarvisAction.RunDiagnostics
      ),
      ProtocolItem(
        id = "whatsapp",
        title = "WHATSAPP PROTOCOL",
        subtitle = "Encrypted communication stream",
        icon = Icons.Default.Send,
        action = JarvisAction.OpenApp("WhatsApp", "com.whatsapp")
      ),
      ProtocolItem(
        id = "youtube",
        title = "YOUTUBE INTEL",
        subtitle = "Global streaming network",
        icon = Icons.Default.PlayArrow,
        action = JarvisAction.OpenApp("YouTube")
      ),
      ProtocolItem(
        id = "camera",
        title = "OPTICAL MATRIX",
        subtitle = "High-definition camera feed",
        icon = Icons.Default.CameraAlt,
        action = JarvisAction.OpenApp("Camera")
      ),
      ProtocolItem(
        id = "maps",
        title = "SATELLITE MAPS",
        subtitle = "Orbital positioning & routing",
        icon = Icons.Default.Map,
        action = JarvisAction.OpenMaps(null)
      ),
      ProtocolItem(
        id = "search",
        title = "WEB RECONNAISSANCE",
        subtitle = "Global databank search query",
        icon = Icons.Default.Language,
        action = JarvisAction.WebSearch("Latest Science & Tech")
      ),
      ProtocolItem(
        id = "torch",
        title = "PHOTON EMITTER",
        subtitle = "Emergency high-intensity torch",
        icon = Icons.Default.FlashlightOn,
        action = JarvisAction.ToggleFlashlight
      ),
      ProtocolItem(
        id = "dialer",
        title = "COMMS DIALER",
        subtitle = "Direct vocal frequency link",
        icon = Icons.Default.Phone,
        action = JarvisAction.OpenApp("Phone")
      ),
      ProtocolItem(
        id = "alarm",
        title = "CHRONOMETER",
        subtitle = "Mission timers & alerts",
        icon = Icons.Default.Alarm,
        action = JarvisAction.OpenApp("Clock")
      ),
      ProtocolItem(
        id = "calc",
        title = "COMPUTE MATRIX",
        subtitle = "High-precision calculations",
        icon = Icons.Default.Calculate,
        action = JarvisAction.OpenApp("Calculator")
      ),
      ProtocolItem(
        id = "battery",
        title = "POWER SUBROUTINE",
        subtitle = "Arc reactor cell diagnostics",
        icon = Icons.Default.BatteryFull,
        action = JarvisAction.BatteryCheck
      ),
      ProtocolItem(
        id = "settings",
        title = "SYSTEM CONTROL",
        subtitle = "Android device control console",
        icon = Icons.Default.Settings,
        action = JarvisAction.OpenApp("Settings")
      )
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 10.dp)
  ) {
    Text(
      text = "TACTICAL COMMAND MATRIX",
      style = MaterialTheme.typography.titleMedium.copy(
        color = colors.textPrimary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp
      )
    )
    Text(
      text = "DIRECT HARDWARE & APPLICATION PROTOCOLS",
      style = MaterialTheme.typography.labelSmall.copy(
        color = colors.arcPrimary,
        fontSize = 9.sp
      )
    )

    Spacer(modifier = Modifier.height(14.dp))

    // WhatsApp Direct Composer Card
    SciFiCard(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { showWhatsAppDialog = true }
        .testTag("whatsapp_composer_card"),
      borderColor = colors.borderActive,
      cornerCut = 8.dp
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Send,
            contentDescription = null,
            tint = colors.healthyEmerald,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "COMPOSE WHATSAPP DISPATCH",
              style = MaterialTheme.typography.titleSmall.copy(
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            )
            Text(
              text = "Tap to pre-fill message or recipient",
              style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textMuted
              )
            )
          }
        }
        Text(
          text = "DISPATCH >",
          style = MaterialTheme.typography.labelSmall.copy(
            color = colors.healthyEmerald,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Grid of Command Tiles
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      contentPadding = PaddingValues(bottom = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(protocols.size) { index ->
        val item = protocols[index]
        CommandTile(
          item = item,
          onClick = {
            when (item.id) {
              "diag" -> onRunDiagnostics()
              "torch" -> onToggleTorch()
              "whatsapp" -> showWhatsAppDialog = true
              "search" -> showSearchDialog = true
              else -> onActionTriggered(item.action)
            }
          }
        )
      }
    }
  }

  // Dialog: WhatsApp Composer
  if (showWhatsAppDialog) {
    WhatsAppComposerDialog(
      onDismiss = { showWhatsAppDialog = false },
      onSend = { phone, message ->
        showWhatsAppDialog = false
        onActionTriggered(JarvisAction.SendWhatsApp(phone, message))
      }
    )
  }

  // Dialog: Web Recon Search
  if (showSearchDialog) {
    SearchQueryDialog(
      onDismiss = { showSearchDialog = false },
      onSearch = { query ->
        showSearchDialog = false
        onActionTriggered(JarvisAction.WebSearch(query))
      }
    )
  }
}

@Composable
private fun CommandTile(
  item: ProtocolItem,
  onClick: () -> Unit
) {
  val colors = LocalJarvisColors.current

  Box(
    modifier = Modifier
      .clip(CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
      .background(colors.surfaceCard)
      .border(1.dp, colors.borderNormal, CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
      .clickable { onClick() }
      .padding(12.dp)
      .testTag("command_tile_${item.id}")
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = item.icon,
          contentDescription = item.title,
          tint = colors.arcPrimary,
          modifier = Modifier.size(20.dp)
        )
        Text(
          text = "ACT",
          style = MaterialTheme.typography.labelSmall.copy(
            color = colors.textMuted.copy(alpha = 0.5f),
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp
          )
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = item.title,
        style = MaterialTheme.typography.labelMedium.copy(
          color = colors.textPrimary,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          lineHeight = 14.sp
        )
      )
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = item.subtitle,
        style = MaterialTheme.typography.bodySmall.copy(
          color = colors.textMuted,
          fontSize = 11.sp,
          lineHeight = 14.sp
        ),
        maxLines = 2
      )
    }
  }
}

@Composable
private fun WhatsAppComposerDialog(
  onDismiss: () -> Unit,
  onSend: (String?, String) -> Unit
) {
  val colors = LocalJarvisColors.current
  var phone by remember { mutableStateOf("") }
  var message by remember { mutableStateOf("Hello from MYRAAA AI Assistant!") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "WHATSAPP TRANSMISSION PROTOCOL",
        style = MaterialTheme.typography.titleSmall.copy(
          color = colors.arcPrimary,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      )
    },
    text = {
      Column {
        Text(
          text = "Specify recipient phone number (optional with country code) and message content:",
          style = MaterialTheme.typography.bodySmall.copy(color = colors.textPrimary)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number (e.g. +91 9876543210)") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.arcPrimary,
            unfocusedBorderColor = colors.borderNormal,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("whatsapp_phone_input")
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = message,
          onValueChange = { message = it },
          label = { Text("Message Text") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.arcPrimary,
            unfocusedBorderColor = colors.borderNormal,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("whatsapp_msg_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSend(phone.ifBlank { null }, message) },
        colors = ButtonDefaults.buttonColors(containerColor = colors.arcPrimary),
        modifier = Modifier.testTag("whatsapp_send_btn")
      ) {
        Text("DISPATCH", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("CANCEL", color = colors.textMuted)
      }
    },
    containerColor = colors.surfaceCard
  )
}

@Composable
private fun SearchQueryDialog(
  onDismiss: () -> Unit,
  onSearch: (String) -> Unit
) {
  val colors = LocalJarvisColors.current
  var query by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "GLOBAL RECONNAISSANCE QUERY",
        style = MaterialTheme.typography.titleSmall.copy(
          color = colors.arcPrimary,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold
        )
      )
    },
    text = {
      Column {
        Text(
          text = "Enter query to search global databanks:",
          style = MaterialTheme.typography.bodySmall.copy(color = colors.textPrimary)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          placeholder = { Text("e.g. Latest Quantum Computing Advances") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.arcPrimary,
            unfocusedBorderColor = colors.borderNormal,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_query_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { if (query.isNotBlank()) onSearch(query) },
        colors = ButtonDefaults.buttonColors(containerColor = colors.arcPrimary),
        modifier = Modifier.testTag("search_exec_btn")
      ) {
        Text("SEARCH", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("ABORT", color = colors.textMuted)
      }
    },
    containerColor = colors.surfaceCard
  )
}
