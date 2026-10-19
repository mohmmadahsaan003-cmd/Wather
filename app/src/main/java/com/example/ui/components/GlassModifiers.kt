package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassEffect(
    cornerRadius: Dp = 24.dp,
    alpha: Float = 0.22f,
    borderAlpha: Float = 0.25f,
    elevation: Dp = 8.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = Color.Black.copy(alpha = 0.4f),
        spotColor = Color.Black.copy(alpha = 0.4f)
    )
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha + 0.08f),
                Color.White.copy(alpha = alpha)
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = borderAlpha + 0.15f),
                Color.White.copy(alpha = borderAlpha * 0.4f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )

fun Modifier.glassPill(
    alpha: Float = 0.25f,
    borderAlpha: Float = 0.3f
): Modifier = this
    .clip(RoundedCornerShape(percent = 50))
    .background(Color.White.copy(alpha = alpha))
    .border(
        width = 1.dp,
        color = Color.White.copy(alpha = borderAlpha),
        shape = RoundedCornerShape(percent = 50)
    )
