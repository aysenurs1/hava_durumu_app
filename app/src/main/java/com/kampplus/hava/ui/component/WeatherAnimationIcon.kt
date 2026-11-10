package com.kampplus.hava.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun WeatherAnimationIcon(
    visuals: WeatherVisuals,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val assetName = visuals.lottieAsset

    if (assetName != null) {
        val composition by rememberLottieComposition(LottieCompositionSpec.Asset(assetName))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )

        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            if (composition != null) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(size)
                )
            } else {
                // JSON henüz assets klasöründe yoksa veya yükleniyorsa geriye dönük statik ikon gösterilir
                Icon(
                    imageVector = visuals.icon,
                    contentDescription = contentDescription,
                    tint = visuals.iconTint,
                    modifier = Modifier.size(size * 0.6f)
                )
            }
        }
    } else {
        Icon(
            imageVector = visuals.icon,
            contentDescription = contentDescription,
            tint = visuals.iconTint,
            modifier = modifier.size(size * 0.6f)
        )
    }
}
