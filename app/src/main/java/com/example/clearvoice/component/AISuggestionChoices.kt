package com.example.clearvoice.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AISuggestionChoices(
    suggestions: List<String>,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) return

    val cyan = Color(0xFF22D3EE)
    val cyanDeep = Color(0xFF0891B2)
    val glass = Color.White.copy(alpha = 0.05f)
    val border = Color.White.copy(alpha = 0.10f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        suggestions.take(9).forEachIndexed { i, text ->
            val indexLabel = (i + 1).toString()
            SuggestionCard(
                indexLabel = indexLabel,
                text = text,
                glass = glass,
                border = border,
                accent = Brush.linearGradient(listOf(cyan, cyanDeep)),
                onClick = { onPick(text) }
            )
        }
    }



}

@Composable
private fun SuggestionCard(
    indexLabel: String,
    text: String,
    glass: Color,
    border: Color,
    accent: Brush,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "scale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(10.dp, shape, clip = false)
            .background(glass, shape)
            .border(1.dp, border, shape)
            .clip(shape)
            .clickable(
                interactionSource = interaction,
                indication = rememberRipple(bounded = true)
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Number badge (1..9)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(Color.Transparent, CircleShape)
                    .border(1.dp, border, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(3.dp)
                        .background(brush = accent, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        indexLabel,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Text (wrap nicely on small screens)
            Text(
                text = text,
                color = Color(0xFFE5E7EB),
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 20.sp),
                modifier = Modifier.weight(1f)
            )

            // Minimal chevron
            Text("›", color = Color(0xFF9CA3AF), style = MaterialTheme.typography.titleMedium)
        }
    }
}
