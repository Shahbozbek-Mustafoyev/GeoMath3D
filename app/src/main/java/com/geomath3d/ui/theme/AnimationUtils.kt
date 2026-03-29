package com.geomath3d.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.IntOffset

// ── Shared animation specs ────────────────────────────────────────────────

val SpringSnappy = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness    = Spring.StiffnessMediumLow,
)

val SpringSmooth = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness    = Spring.StiffnessMedium,
)

val EaseOutQuart: Easing = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
val EaseInOutCubic: Easing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

// ── Screen transition ─────────────────────────────────────────────────────

fun screenEnterTransition(): EnterTransition =
    fadeIn(tween(220, easing = EaseOutQuart)) +
    slideIn(tween(280, easing = EaseOutQuart)) { IntOffset(0, 40) }

fun screenExitTransition(): ExitTransition =
    fadeOut(tween(160, easing = EaseInOutCubic)) +
    slideOut(tween(200, easing = EaseInOutCubic)) { IntOffset(0, -20) }

// ── Number counter animation ──────────────────────────────────────────────

@Composable
fun animateDoubleAsState(
    targetValue: Double,
    animationSpec: AnimationSpec<Float> = tween(400, easing = EaseOutQuart),
): Double {
    val animated by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = animationSpec,
        label = "counterAnim",
    )
    return animated.toDouble()
}

// ── Stagger delays ────────────────────────────────────────────────────────
fun staggerDelay(index: Int, base: Int = 60): Int = index * base
