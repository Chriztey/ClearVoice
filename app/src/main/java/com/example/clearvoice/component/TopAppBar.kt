package com.example.clearvoice.component

import androidx.compose.foundation.Image

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TopAppBarScrollBehavior

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.clearvoice.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearVoiceTopAppBar(
    bg: Brush,
    // Pass this to Scaffold's nestedScroll() if you want collapsing effects
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
) {

        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .background(bg),
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,          // <-- key
                scrolledContainerColor = Color.Transparent,  // <-- key when scrolling
                titleContentColor = Color(0xFFE5E7EB)
            ),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth().background(bg),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically) {
                    Image(
                                painter = painterResource(id = R.drawable.clearvoice_logo), // your drawable file
                                contentDescription = "App logo",
                                modifier = Modifier.size(64.dp),
                                contentScale = ContentScale.Fit
                            )

                        Text(
                            text = "Assistive Communication",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF9CA3AF)
                            )
                        )

                }
            },

        )
    }

