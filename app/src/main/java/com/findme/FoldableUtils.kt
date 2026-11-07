package com.findme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

enum class FoldPosture {
    NORMAL,
    BOOK,
    TABLETOP
}

data class FoldUiInfo(
    val posture: FoldPosture = FoldPosture.NORMAL,
    val isSeparating: Boolean = false
)

fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Composable
fun rememberFoldUiInfo(activity: Activity?): FoldUiInfo {
    val foldFlow = remember(activity) {
        if (activity == null) {
            flowOf(null)
        } else {
            WindowInfoTracker.getOrCreate(activity)
                .windowLayoutInfo(activity)
                .map { info ->
                    info.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull()
                }
        }
    }
    val fold by foldFlow.collectAsStateWithLifecycle(initialValue = null)

    val halfOpened = fold?.state == FoldingFeature.State.HALF_OPENED
    val isTabletop = halfOpened && fold?.orientation == FoldingFeature.Orientation.HORIZONTAL
    val isBook = halfOpened && fold?.orientation == FoldingFeature.Orientation.VERTICAL

    val posture = when {
        isTabletop -> FoldPosture.TABLETOP
        isBook -> FoldPosture.BOOK
        else -> FoldPosture.NORMAL
    }

    return FoldUiInfo(
        posture = posture,
        isSeparating = fold?.isSeparating == true
    )
}