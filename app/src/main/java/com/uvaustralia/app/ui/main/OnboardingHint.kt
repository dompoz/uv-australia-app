package com.uvaustralia.app.ui.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val HintColor = Color(0xFFfc03a5)

/**
 * Wraps [content] in an onboarding hint overlay — a rounded-rectangle outline that breathes
 * and a tap badge in the top-right corner — when [visible] is true.
 *
 * The hint enters with a fade+scale animation. Once visible, the outline gently pulses.
 * Tapping either the badge, the outline, or the content itself calls [onTap].
 *
 * @param visible            Whether the hint should be shown.
 * @param onTap              Called when the user interacts with the hint or the content.
 * @param cornerRadius       Corner radius applied to the outline rectangle.
 * @param outlineWidth       Stroke width of the outline.
 * @param padding            Padding between the content bounds and the outline on all sides.
 * @param outlineStartInset  Additional inset applied to the outline's start (left) edge only,
 *                           beyond [padding]. Use this to narrow the outline when content
 *                           has internal whitespace on the start side.
 * @param outlineEndInset    Additional inset applied to the outline's end (right) edge only.
 * @param content            The composable to annotate with the hint.
 */
@Composable
fun OnboardingHint(
    visible: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    outlineWidth: Dp = 3.dp,
    padding: Dp = 8.dp,
    outlineStartInset: Dp = 0.dp,
    outlineEndInset: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    var entryAlpha by remember { mutableFloatStateOf(0f) }
    var entryScale by remember { mutableFloatStateOf(0.92f) }

    LaunchedEffect(visible) {
        if (visible) {
            val steps = 20
            for (i in 1..steps) {
                val t = i.toFloat() / steps
                val eased = FastOutSlowInEasing.transform(t)
                entryAlpha = eased
                entryScale = 0.92f + 0.08f * eased
                delay(16L)
            }
        } else {
            entryAlpha = 0f
            entryScale = 0.92f
        }
    }

    val breathTransition = rememberInfiniteTransition(label = "onboarding_breath")
    val breathAlpha by breathTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "onboarding_breath_alpha",
    )
    val breathScale by breathTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "onboarding_breath_scale",
    )

    val interactionSource = remember { MutableInteractionSource() }
    val tapModifier = if (visible) {
        Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onTap)
    } else {
        Modifier
    }

    Box(modifier = modifier) {
        Box(modifier = tapModifier) {
            content()
        }

        if (visible) {
            val outlineAlpha = entryAlpha * breathAlpha
            val combinedScale = entryScale * breathScale

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .scale(combinedScale)
                    .alpha(outlineAlpha)
                    .padding(
                        start = padding + outlineStartInset,
                        end = padding + outlineEndInset,
                        top = padding,
                        bottom = padding,
                    )
                    .border(
                        width = outlineWidth,
                        color = HintColor,
                        shape = RoundedCornerShape(cornerRadius),
                    )
                    .clip(RoundedCornerShape(cornerRadius))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTap,
                    ),
            )

            // Badge anchored to the top-right corner of the outline (not the content)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = -(outlineEndInset) + 6.dp, y = (-6).dp)
                    .scale(entryScale)
                    .alpha(entryAlpha)
                    .size(28.dp)
                    .background(color = HintColor, shape = CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTap,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.TouchApp,
                    contentDescription = "Tap to interact",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}
