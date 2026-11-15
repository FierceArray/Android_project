package com.findme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.findme.ui.theme.AppDimens

@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    var sweepTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sweepTriggered = true
    }

    val shimmerTranslate by animateFloatAsState(
        targetValue = if (sweepTriggered) 1400f else -400f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = LinearEasing
        ),
        finishedListener = {
            sweepTriggered = !sweepTriggered
        },
        label = "skeleton_shimmer_sweep"
    )

    val baseColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f)
    val highlightColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(x = shimmerTranslate - 250f, y = shimmerTranslate - 250f),
        end = Offset(x = shimmerTranslate, y = shimmerTranslate)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppDimens.radiusLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(AppDimens.paddingMedium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(AppDimens.iconTouchTarget)
                        .clip(CircleShape)
                        .background(shimmerBrush)
                )

                Spacer(modifier = Modifier.width(AppDimens.paddingMedium))

                Column {
                    Box(
                        modifier = Modifier
                            .height(AppDimens.skeletonHeight)
                            .width(AppDimens.skeletonWidthLong)
                            .clip(RoundedCornerShape(AppDimens.radiusSmall))
                            .background(shimmerBrush)
                    )

                    Spacer(modifier = Modifier.height(AppDimens.paddingSmall))

                    Box(
                        modifier = Modifier
                            .height(AppDimens.iconSmall)
                            .width(AppDimens.skeletonWidthShort)
                            .clip(RoundedCornerShape(AppDimens.radiusSmall))
                            .background(shimmerBrush)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.paddingLarge))

            Box(
                modifier = Modifier
                    .height(AppDimens.iconTouchTarget)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AppDimens.radiusExtraLarge))
                    .background(shimmerBrush)
            )
        }
    }
}