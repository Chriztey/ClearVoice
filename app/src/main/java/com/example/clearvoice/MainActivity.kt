package com.example.clearvoice

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.clearvoice.screen.MainScreen
import com.example.clearvoice.ui.theme.ClearVoiceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                // Handle permission result if needed
            }
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        enableEdgeToEdge()
        setContent {
            ClearVoiceTheme {
                MainScreen()
            }
        }
    }
}
