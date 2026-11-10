package com.kampplus.hava.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object WeatherUiTokens {
    val skyGradient = listOf(
        Color(0xFF1E3C72),
        Color(0xFF2A5298),
        Color(0xFF1B2A47)
    )
    val glassFill = Color.White.copy(alpha = 0.15f)
    val glassBorder = Color.White.copy(alpha = 0.25f)
    val glassShape = RoundedCornerShape(22.dp)
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = WeatherUiTokens.glassShape,
        colors = CardDefaults.cardColors(containerColor = WeatherUiTokens.glassFill),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, WeatherUiTokens.glassBorder),
        content = { content() }
    )
}
