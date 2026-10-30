package org.minecraft.xlink

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/* ------------------------------------------------------------------ */
/*  缓动函数（对应 CSS 的 ease-in-out / ease-out）                       */
/* ------------------------------------------------------------------ */

private val EaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
private val EaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)

/* ------------------------------------------------------------------ */
/*  关键帧路径数据                                                      */
/* ------------------------------------------------------------------ */

private data class PathPoint(
    val fraction: Float, // 0f..1f，对应 CSS 百分比
    val col: Float,      // 网格列
    val row: Float,      // 网格行
)

private data class SquareSpec(
    val points: List<PathPoint>,
    val fadeInDelayMillis: Int,
)

private val SquareSpecs: List<SquareSpec> = listOf(
    SquareSpec(
        points = listOf(
            PathPoint(0f, 0f, 0f),
            PathPoint(0.08333f, 0f, 1f),
            PathPoint(1f, 0f, 1f),
        ),
        fadeInDelayMillis = 100,
    ),
    SquareSpec(
        points = listOf(
            PathPoint(0f, 0f, 1f),
            PathPoint(0.08333f, 0f, 2f),
            PathPoint(0.16667f, 1f, 2f),
            PathPoint(0.25f, 1f, 1f),
            PathPoint(0.83333f, 1f, 1f),
            PathPoint(0.91667f, 1f, 0f),
            PathPoint(1f, 0f, 0f),
        ),
        fadeInDelayMillis = 100,
    ),
    SquareSpec(
        points = listOf(
            PathPoint(0f, 1f, 1f),
            PathPoint(0.16667f, 1f, 1f),
            PathPoint(0.25f, 1f, 0f),
            PathPoint(0.33333f, 2f, 0f),
            PathPoint(0.41667f, 2f, 1f),
            PathPoint(0.66667f, 2f, 1f),
            PathPoint(0.75f, 2f, 2f),
            PathPoint(0.83333f, 1f, 2f),
            PathPoint(0.91667f, 1f, 1f),
            PathPoint(1f, 1f, 1f),
        ),
        fadeInDelayMillis = 200,
    ),
    SquareSpec(
        points = listOf(
            PathPoint(0f, 2f, 1f),
            PathPoint(0.33333f, 2f, 1f),
            PathPoint(0.41667f, 2f, 2f),
            PathPoint(0.5f, 3f, 2f),
            PathPoint(0.58333f, 3f, 1f),
            PathPoint(1f, 3f, 1f),
        ),
        fadeInDelayMillis = 300,
    ),
    SquareSpec(
        points = listOf(
            PathPoint(0f, 3f, 1f),
            PathPoint(0.5f, 3f, 1f),
            PathPoint(0.58333f, 3f, 0f),
            PathPoint(0.66667f, 2f, 0f),
            PathPoint(0.75f, 2f, 1f),
            PathPoint(1f, 2f, 1f),
        ),
        fadeInDelayMillis = 400,
    ),
)

/* ------------------------------------------------------------------ */
/*  原始 CSS 的比例（用于等比缩放）                                      */
/* ------------------------------------------------------------------ */

private const val BASE_SQUARE = 26f
private const val BASE_OFFSET = 30f
private const val BASE_WIDTH = BASE_SQUARE + 3f * BASE_OFFSET   // 116
private const val BASE_HEIGHT = BASE_SQUARE + 2f * BASE_OFFSET  // 86

/* ------------------------------------------------------------------ */
/*  公共组件                                                            */
/* ------------------------------------------------------------------ */

/**
 * 无进度加载指示器（5 个方块“贪吃蛇”式循环动画）。
 *
 * 尺寸自适应：组件会读取父级给的约束，
 * 在保持原始宽高比（116:86）的前提下等比缩放内部所有元素。
 * 若父级未给出确定的宽高（例如放在 Column 里没有固定高度），
 * 则回退到原始设计尺寸。
 */
@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    durationMillis: Int = 2400,
    startDelayMillis: Int = 200,
    fadeInDurationMillis: Int = 400,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current

        // 计算在当前约束下应该使用的尺寸
        val squareDp: Dp
        val offsetDp: Dp
        if (constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
            val scale = minOf(
                maxWidth.value / BASE_WIDTH,
                maxHeight.value / BASE_HEIGHT,
            ).coerceAtLeast(0f)
            squareDp = (BASE_SQUARE * scale).dp
            offsetDp = (BASE_OFFSET * scale).dp
        } else {
            squareDp = BASE_SQUARE.dp
            offsetDp = BASE_OFFSET.dp
        }

        val offsetPx = with(density) { offsetDp.toPx() }
        val transition = rememberInfiniteTransition(label = "loadingSpinner")

        // 用一个固定尺寸的 Box 承载所有方块，尺寸 = 3*offset + square × 2*offset + square
        Box(
            modifier = Modifier.size(
                width = offsetDp * 3f + squareDp,
                height = offsetDp * 2f + squareDp,
            ),
        ) {
            SquareSpecs.forEach { spec ->
                SpinnerSquare(
                    spec = spec,
                    squareSize = squareDp,
                    offsetPx = offsetPx,
                    durationMillis = durationMillis,
                    startDelayMillis = startDelayMillis,
                    fadeInDurationMillis = fadeInDurationMillis,
                    color = color,
                    transition = transition,
                )
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/*  单个方块                                                            */
/* ------------------------------------------------------------------ */

@Composable
private fun SpinnerSquare(
    spec: SquareSpec,
    squareSize: Dp,
    offsetPx: Float,
    durationMillis: Int,
    startDelayMillis: Int,
    fadeInDurationMillis: Int,
    color: Color,
    transition: InfiniteTransition,
) {
    val startPoint = spec.points.first()
    val endPoint = spec.points.last()

    val col by transition.animateFloat(
        initialValue = startPoint.col,
        targetValue = endPoint.col,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                this.durationMillis = durationMillis
                spec.points.forEach { p ->
                    p.col at (p.fraction * durationMillis).roundToInt() using EaseInOut
                }
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(startDelayMillis),
        ),
        label = "col",
    )

    val row by transition.animateFloat(
        initialValue = startPoint.row,
        targetValue = endPoint.row,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                this.durationMillis = durationMillis
                spec.points.forEach { p ->
                    p.row at (p.fraction * durationMillis).roundToInt() using EaseInOut
                }
            },
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(startDelayMillis),
        ),
        label = "row",
    )

    val fadeIn = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        fadeIn.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = fadeInDurationMillis,
                delayMillis = spec.fadeInDelayMillis,
                easing = EaseOut,
            ),
        )
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (col * offsetPx).roundToInt(),
                    y = (row * offsetPx).roundToInt(),
                )
            }
            .size(squareSize)
            .graphicsLayer {
                alpha = fadeIn.value
                val s = 0.75f + 0.25f * fadeIn.value
                scaleX = s
                scaleY = s
            }
            .background(color = color, shape = RoundedCornerShape(2.dp)),
    )
}