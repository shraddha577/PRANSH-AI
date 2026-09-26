package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalJarvisColors

@Composable
fun SciFiCard(
  modifier: Modifier = Modifier,
  borderColor: Color? = null,
  backgroundColor: Color? = null,
  cornerCut: Dp = 10.dp,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val colors = LocalJarvisColors.current
  val strokeColor = borderColor ?: colors.borderNormal
  val bgColor = backgroundColor ?: colors.surfaceCard.copy(alpha = 0.85f)
  val shape = CutCornerShape(
    topStart = cornerCut,
    topEnd = 0.dp,
    bottomEnd = cornerCut,
    bottomStart = 0.dp
  )

  val clickableModifier = if (onClick != null) {
    Modifier.clickable { onClick() }
  } else {
    Modifier
  }

  Box(
    modifier = modifier
      .clip(shape)
      .background(bgColor)
      .border(BorderStroke(1.dp, strokeColor), shape)
      .then(clickableModifier)
      .padding(14.dp)
  ) {
    content()
  }
}
