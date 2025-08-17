package com.example.clearvoice.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun MessageInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Type your message…"
) {
    val cyan = Color(0xFF22D3EE)
    val cyanDeep = Color(0xFF0891B2)
    val border = Color.White.copy(alpha = 0.10f)
    val bg = Color.White.copy(alpha = 0.05f)

    val canSend = inputText.isNotBlank()
    val sendScale by animateFloatAsState(if (canSend) 1f else 0.98f, label = "sendScale")
    val sendAlpha by animateFloatAsState(if (canSend) 1f else 0.4f, label = "sendAlpha")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glassy rounded field
        TextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = { Text(placeholder, color = Color(0xFF9CA3AF)) },
            modifier = Modifier
                .weight(1f)
                .shadow(12.dp, RoundedCornerShape(20.dp), clip = false)
                .background(bg, RoundedCornerShape(20.dp))
                .border(1.dp, border, RoundedCornerShape(20.dp)),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = cyan,
                focusedTextColor = Color(0xFFE5E7EB),
                unfocusedTextColor = Color(0xFFE5E7EB)
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Type",
                    tint = Color(0xFF9CA3AF)
                )
            },
            trailingIcon = {
                if (inputText.isNotEmpty()) {
                    IconButton(onClick = { onInputChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() })
        )

        Spacer(Modifier.width(10.dp))

        // Gradient Send pill
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = sendScale
                    scaleY = sendScale
                    alpha = sendAlpha
                }
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (canSend) Brush.linearGradient(listOf(cyan, cyanDeep))
                    else Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1F2937)))
                )
                .clickable(enabled = canSend) { onSend() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
//                Spacer(Modifier.width(6.dp))
//                Text("Send", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
