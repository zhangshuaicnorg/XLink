package org.minecraft.xlink

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp

fun Modifier.shake(
    enabled: Boolean,
    onAnimationEnd: () -> Unit = {}
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    LaunchedEffect(enabled) {
        if (enabled) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    // 语法：目标值 at 时间点(ms) with 缓动曲线
                    0f at 0 using LinearOutSlowInEasing
                    25f at 50 using FastOutLinearInEasing
                    (-25f) at 100 using FastOutLinearInEasing
                    20f at 150 using FastOutLinearInEasing
                    (-20f) at 200 using FastOutLinearInEasing
                    15f at 250 using FastOutLinearInEasing
                    (-15f) at 300 using FastOutLinearInEasing
                    0f   at 400
                }
            )
            onAnimationEnd()
        }
    }
    this.offset(x = offsetX.value.dp)
}
