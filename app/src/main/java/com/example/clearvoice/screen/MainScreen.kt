package com.example.clearvoice.screen

import WatsonMessage
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clearvoice.component.AISuggestionChoices
import com.example.clearvoice.component.ClearVoiceTopAppBar
import com.example.clearvoice.component.ConversationArea
import com.example.clearvoice.component.MessageInputBar
import com.example.clearvoice.component.VoiceRecorderUI
import com.example.clearvoice.viewModel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {

    val bg = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0C0F14),
            Color(0xFF111827),
            Color(0xFF0A0F1C)
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f * (0.2f), 1400f * (0.8f))
    )

    var conversationRequest by remember { mutableStateOf(listOf<WatsonMessage>()) }
    val context = LocalContext.current
    val aiSuggestions by viewModel.aiResponse.collectAsState()
    val loading by viewModel.loadingFetchAI.collectAsState()
    var conversation by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }
    var inputText by remember { mutableStateOf("") }


    val apiKey = "API-KEY-HERE"

    // Initialise TextToSpeech for reading bubbles aloud
    val tts = remember {
        TextToSpeech(context) { /* init callback not used here */ }
    }
    // Set the language once after initialization
    LaunchedEffect(tts) {
        tts.language = Locale.getDefault()
    }
    DisposableEffect(Unit) {
        onDispose { tts.shutdown() }
    }

    Scaffold(
        modifier = Modifier.background(bg),
        topBar = {
            ClearVoiceTopAppBar(
                bg = bg
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .background(bg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dialogue
                ConversationArea(
                    modifier = Modifier.weight(0.65f),
                    conversation = conversation,
                    onBubbleClick = { message ->
                        // Speak the clicked bubble using TTS
                        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "bubble")
                    })

                // AI suggestion chips. Tapping a suggestion appends it to the text field instead of sending it.
                if (aiSuggestions.isNotEmpty()) {

                    // When user clicks a choice, append to inputText (like your current behavior)
                    AISuggestionChoices(
                        suggestions = aiSuggestions,
                        onPick = { choice ->
                            inputText += if (inputText.isEmpty()) choice else " $choice"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }




                PhraseBank(
                    onPhraseClick = { phrase ->
                        inputText += if (inputText.isEmpty()) phrase else " $phrase"
                    }
                )

                MessageInputBar(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onSend = {
                        if (inputText.isNotBlank()) {
                            conversation = conversation + (inputText to true)
                            conversationRequest = conversationRequest + (WatsonMessage(
                                role = "user",
                                content = inputText
                            ))
                            viewModel.resetAISuggestion()
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                )


                VoiceRecorderUI { a ->
                    Log.d("Testing TTS", a)
                    // Append as other's message to chat
                    conversation = conversation + (a to false)
                    conversationRequest =
                        conversationRequest + (WatsonMessage(role = "other", content = a))
                    Log.d("Testing TTS", conversationRequest.toString())
                    viewModel.fetchData(
                        apiKey = apiKey,
                        conversRequest = conversationRequest,
                        speech = a
                    )
                }

            }

            if (loading) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)), // transparent black
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhraseBank(
    onPhraseClick: (String) -> Unit,
    phrases: List<String> = listOf("Hi!", "I need help", "Thank you", "How are you?", "Yes", "No"),
    modifier: Modifier = Modifier
) {
    val cyan = Color(0xFF22D3EE)
    val cyanDeep = Color(0xFF0891B2)
    val glass = Color.White.copy(alpha = 0.05f)
    val borderLight = Color.White.copy(alpha = 0.10f)

    val listState = rememberLazyListState()
    val fling = rememberSnapFlingBehavior(lazyListState = listState)
    val clipboard = LocalClipboardManager.current

    LazyRow(
        state = listState,
        flingBehavior = fling,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        items(phrases, key = { it }) { phrase ->
            val shape = RoundedCornerShape(22.dp)
            val interaction = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = 40.dp)
                    .background(glass, shape)
                    .border(
                        BorderStroke(1.dp, borderLight),
                        shape
                    )
                    .shadow(8.dp, shape, clip = false)
                    .clip(shape)
                    .combinedClickable(
                        interactionSource = interaction,
                        indication = rememberRipple(bounded = true),
                        onClick = { onPhraseClick(phrase) },
                        onLongClick = {
                            clipboard.setText(AnnotatedString(phrase))
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Minimal accent dot to tie into cyan palette
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(cyan, cyanDeep)),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = phrase,
                        color = Color(0xFFE5E7EB),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


