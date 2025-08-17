package com.example.clearvoice.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationArea(
    conversation: List<Pair<String, Boolean>>, // (message, isUser)
    onBubbleClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cyan = Color(0xFF22D3EE)
    val cyanDeep = Color(0xFF0891B2)
    val slateglass = Color.White.copy(alpha = 0.05f)
    val borderLight = Color.White.copy(alpha = 0.10f)

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom whenever a new item is added
    LaunchedEffect(conversation.size) {
        if (conversation.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(conversation.lastIndex)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            count = conversation.size,
        ) { index ->
            val (message, isUser) = conversation[index]


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
                // Optional status dot / avatar stub for assistant
                if (!isUser) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp)
                            .background(
                                Brush.linearGradient(listOf(cyanDeep, cyan)),
                                shape = CircleShape
                            )
                            .border(1.dp, borderLight, CircleShape)
                            .shadow(6.dp, CircleShape, clip = false)
                    )
                }

                // Chat bubble
                val bubbleShape = RoundedCornerShape(
                    topStart = 22.dp, topEnd = 22.dp,
                    bottomEnd = if (isUser) 6.dp else 22.dp,
                    bottomStart = if (isUser) 22.dp else 6.dp
                )

                val userBrush = Brush.linearGradient(listOf(cyan, cyanDeep))
                val asstBrush = Brush.linearGradient(
                    listOf(Color(0xFF1A2232), Color(0xFF101826))
                )

                val bg = if (isUser)
                    userBrush
                else
                    asstBrush

                val textColor = if (isUser) Color.White else Color(0xFFE5E7EB)

                // Ripple + combined clicks
                val interaction = remember { MutableInteractionSource() }

                // Max bubble width for nice wrapping
                Box(
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .background(
                            color = Color.Transparent,
                            shape = bubbleShape
                        )
                        .shadow(12.dp, bubbleShape, clip = false)
                        .border(1.dp, borderLight, bubbleShape)
                        .background(
                            brush = bg,
                            shape = bubbleShape
                        )
                        .clip(bubbleShape)
                        .clickable(
                            interactionSource = interaction,
                            indication = rememberRipple(bounded = true)
                        ) { onBubbleClick(message) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Selectable text inside bubble
                    SelectionContainer {
                        Text(
                            text = message,
                            color = textColor,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                // Align “user avatar stub” on the right if you want symmetry
                if (isUser) {
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}
