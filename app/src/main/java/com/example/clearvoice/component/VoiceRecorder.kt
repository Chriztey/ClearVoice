package com.example.clearvoice.component

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A composable that displays a voice recorder UI with a microphone button.
 * When the user taps the mic button, it will start speech-to-text (STT) using
 * Android's SpeechRecognizer API. If no speech is detected for more than 3 seconds
 * it will stop listening automatically. The recognized text is stored in a local
 * state variable but not sent anywhere; you can read it from the surrounding
 * composition if needed.
 */
@Composable
fun VoiceRecorderUI(
    tts: (String) -> Unit
) {
    val context = LocalContext.current
    // Whether we are currently recording/listening
    var isRecording by remember { mutableStateOf(false) }
    // Stores the latest recognized speech as text
    var recognizedText by remember { mutableStateOf("") }
    // Used to detect idle time during speech recognition
    var lastSpeechTime by remember { mutableStateOf(0L) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    // Create SpeechRecognizer and set up the listener once
    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context)
    }

    // Setup the recognition listener
    DisposableEffect(Unit) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                // Invoked when the SpeechRecognizer is ready. Reset lastSpeechTime.
                lastSpeechTime = System.currentTimeMillis()
            }
            override fun onBeginningOfSpeech() {
                // User started speaking; update lastSpeechTime
                lastSpeechTime = System.currentTimeMillis()
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                // Stop recording on error
                isRecording = false
            }
            override fun onResults(results: Bundle) {
                // Received final results. Stop recording and update text.
                isRecording = false
                val texts = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!texts.isNullOrEmpty()) {
                    recognizedText = texts[0]
                    tts(recognizedText)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                // Invoked with partial (interim) results. Update lastSpeechTime
                lastSpeechTime = System.currentTimeMillis()
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        onDispose {
            speechRecognizer.destroy()
        }
    }

    // Permission launcher for recording audio
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Start recording if permission is granted
            startListening(
                speechRecognizer,
                onStart = { isRecording = true; lastSpeechTime = System.currentTimeMillis() },
                onStop = { isRecording = false }
            )
        }
    }

    // Idle detection: periodically check if we've been idle for more than 3 seconds
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(isRecording) {
        if (isRecording) {
            coroutineScope.launch {
                while (isRecording) {
                    delay(500)
                    val idleTime = System.currentTimeMillis() - lastSpeechTime
                    if (idleTime > 3000) {
                        // Too long without speech: stop listening
                        speechRecognizer.stopListening()
                        isRecording = false
                    }
                }
            }
        }
    }

    // Animation for the microphone when recording
    val infiniteTransition = rememberInfiniteTransition()
    val waveSize by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    VoiceRecorderModernUI(
        isRecording = isRecording,
        onToggle = {
            if (isRecording) {
                speechRecognizer.stopListening()
                isRecording = false
            } else {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    startListening(
                        speechRecognizer,
                        onStart = { isRecording = true; lastSpeechTime = System.currentTimeMillis() },
                        onStop = { isRecording = false }
                    )
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    )


//    Column(
//        modifier = Modifier
//            .background(Color(0xFF101010))
//            .padding(16.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        // Wave animation while recording
//        if (isRecording) {
//            Canvas(modifier = Modifier.size(150.dp)) {
//                drawCircle(
//                    color = Color.Cyan.copy(alpha = 0.3f),
//                    radius = size.minDimension / 2 * waveSize
//                )
//                drawCircle(
//                    color = Color.Cyan,
//                    radius = size.minDimension / 4
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(40.dp))
//
//        // Mic button
//        Box(
//            modifier = Modifier
//                .size(70.dp)
//                .background(
//                    color = if (isRecording) Color.Red else Color.Cyan,
//                    shape = androidx.compose.foundation.shape.CircleShape
//                )
//                .clickable {
//                    if (isRecording) {
//                        // Stop recording
//                        speechRecognizer.stopListening()
//                        isRecording = false
//                    } else {
//                        // Not recording: check permission and start
//                        if (ContextCompat.checkSelfPermission(
//                                context,
//                                Manifest.permission.RECORD_AUDIO
//                            ) == PackageManager.PERMISSION_GRANTED
//                        ) {
//                            startListening(
//                                speechRecognizer,
//                                onStart = { isRecording = true; lastSpeechTime = System.currentTimeMillis() },
//                                onStop = { isRecording = false }
//                            )
//                        } else {
//                            // Request permission if not granted
//                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
//                        }
//                    }
//                },
//            contentAlignment = Alignment.Center
//        ) {
//            Text("🎤", style = MaterialTheme.typography.headlineSmall)
//        }
//    }
}

/**
 * Helper function to start listening with a SpeechRecognizer.
 * The caller should update `isRecording` state via onStart and onStop.
 */
private fun startListening(
    speechRecognizer: SpeechRecognizer,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    }
    try {
        onStart()
        speechRecognizer.startListening(intent)
    } catch (e: Exception) {
        // If start fails, ensure state is reset
        onStop()
    }
}

