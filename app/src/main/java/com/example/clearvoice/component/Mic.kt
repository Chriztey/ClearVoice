package com.example.clearvoice.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun VoiceRecorderModernUI(
    isRecording: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle animated background gradient
    val bgAnim = rememberInfiniteTransition(label = "bg")
    val shift by bgAnim.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(6000, easing = LinearEasing),
            RepeatMode.Reverse
        ), label = "bgShift"
    )

    val bg = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0C0F14),
            Color(0xFF111827),
            Color(0xFF0A0F1C)
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f * (0.2f + shift), 1400f * (0.8f - shift))
    )

    // Pulsing scale/alpha for the glowing waves
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "pulseScale"
    )
    val pulseAlpha by pulseAnim.animateFloat(
        initialValue = 0.35f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "pulseAlpha"
    )

    // Smooth transition for recording state (button size/scale)
    val buttonScale by animateFloatAsState(
        targetValue = if (isRecording) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "btnScale"
    )

    // Colors
    val cyan = Color(0xFF22D3EE)
    val cyanDeep = Color(0xFF0891B2)
    val red = Color(0xFFEF4444)
    val redDeep = Color(0xFFB91C1C)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bg)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glassy card container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.04f)
            ),
            elevation = CardDefaults.cardElevation(8.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.06f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = if (isRecording) "Listening…" else "Tap to Speak",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color(0xFFE5E7EB),
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isRecording)
                        "Say something and I’ll transcribe it."
                    else
                        "Grant mic permission if asked.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF9CA3AF)
                    )
                )

                Spacer(Modifier.height(24.dp))

                // Animated wave/glow while recording
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                alpha = 1f
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer glow rings
                        Canvas(Modifier.fillMaxSize()) {
                            val r = size.minDimension / 2f
                            drawCircle(
                                color = (if (isRecording) red else cyan).copy(pulseAlpha * 0.4f),
                                radius = r * 0.95f
                            )
                            drawCircle(
                                color = (if (isRecording) red else cyan).copy(pulseAlpha * 0.25f),
                                radius = r * 0.7f
                            )
                            drawCircle(
                                color = (if (isRecording) red else cyan).copy(pulseAlpha * 0.18f),
                                radius = r * 0.45f
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                } else {
                    Spacer(Modifier.height(48.dp))
                }

                // Mic button
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer {
                            scaleX = buttonScale
                            scaleY = buttonScale
                        }
                        .background(
                            brush = Brush.linearGradient(
                                if (isRecording) listOf(red, redDeep) else listOf(cyan, cyanDeep)
                            ),
                            shape = CircleShape
                        )
                        .shadow(16.dp, CircleShape, clip = false)
                        .clickable(
                            indication = rememberRipple(bounded = true, radius = 56.dp),
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    // Inner pill to get a soft/neumorphic feel
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                Color.White.copy(alpha = 0.08f),
                                shape = CircleShape
                            )
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.12f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (isRecording) "⏺" else "🎤",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Tiny helper row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(
                                if (isRecording) red else cyan,
                                CircleShape
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) "Recording…" else "Ready",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFA3A3A3)
                        )
                    )
                }
            }
        }
    }
}
