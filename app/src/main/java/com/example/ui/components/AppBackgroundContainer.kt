package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.UserPreferences
import java.io.File

@Composable
fun AppBackgroundContainer(
    preferences: UserPreferences,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val bgFile = if (preferences.customBgPath.isNotBlank()) File(preferences.customBgPath) else null
    val hasValidCustomBg = bgFile != null && bgFile.exists()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (hasValidCustomBg) {
            val contentScale = when (preferences.bgPosition) {
                "CENTER" -> ContentScale.Fit
                "TOP" -> ContentScale.FillWidth
                else -> ContentScale.Crop
            }

            // Custom Background Image
            AsyncImage(
                model = bgFile,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )

            // Protective Theme Overlay for maximum readability & contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.background.copy(
                            alpha = preferences.bgOverlayOpacity.coerceIn(0.2f, 0.95f)
                        )
                    )
            )
        }

        // App Content
        content()
    }
}
