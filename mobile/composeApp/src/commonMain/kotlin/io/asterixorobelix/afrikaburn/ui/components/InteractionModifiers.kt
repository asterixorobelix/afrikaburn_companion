package io.asterixorobelix.afrikaburn.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import io.asterixorobelix.afrikaburn.Dimens

/**
 * Animation constants for micro-interactions.
 * These values are tuned for subtle, delightful feedback without being distracting.
 */
private object InteractionConstants {
    /** Scale factor when pressed - subtle press effect */
    const val PRESSED_SCALE = 0.96f

    /** Normal scale factor */
    const val NORMAL_SCALE = 1f

    /** Scale factor for bounce effect at peak */
    const val BOUNCE_SCALE_PEAK = 1.05f

    /** Spring stiffness for press animations - medium-high for snappy response */
    const val SPRING_STIFFNESS_PRESS = Spring.StiffnessMediumLow

    /** Spring damping for press animations */
    const val SPRING_DAMPING_PRESS = Spring.DampingRatioMediumBouncy
}

/**
 * Modifier that scales down the component when pressed and springs back on release.
 * Creates a tactile feel for interactive elements like cards and buttons.
 *
 * @param enabled Whether the press animation is enabled
 * @param pressedScale The scale to animate to when pressed (default: 0.96)
 * @param onClick Optional callback to invoke when the component is tapped
 */
fun Modifier.pressableScale(
    enabled: Boolean = true,
    pressedScale: Float = InteractionConstants.PRESSED_SCALE,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else InteractionConstants.NORMAL_SCALE,
        animationSpec = spring(
            dampingRatio = InteractionConstants.SPRING_DAMPING_PRESS,
            stiffness = InteractionConstants.SPRING_STIFFNESS_PRESS
        ),
        label = "pressScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled, onClick) {
            if (enabled) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = {
                        onClick?.invoke()
                    }
                )
            }
        }
}

/**
 * Modifier that adds a bounce animation when clicked.
 * The component scales up briefly then returns to normal size with a spring animation.
 * Ideal for filter chips and toggle buttons.
 *
 * @param enabled Whether the bounce animation is enabled
 * @param onClick Callback to invoke when the component is clicked
 */
fun Modifier.bounceClick(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    var isAnimating by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) {
            InteractionConstants.BOUNCE_SCALE_PEAK
        } else {
            InteractionConstants.NORMAL_SCALE
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = {
            if (isAnimating) {
                isAnimating = false
            }
        },
        label = "bounceScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(enabled, onClick) {
            if (enabled) {
                detectTapGestures(
                    onTap = {
                        isAnimating = true
                        onClick()
                    }
                )
            }
        }
}

/**
 * Composable wrapper that applies a scale animation based on selection state.
 * Useful for chips and toggles that need visual feedback when selected.
 *
 * @param isSelected Whether the component is currently selected
 * @return The animated scale value to apply to the component
 */
@Composable
fun animateSelectionScale(isSelected: Boolean): Float {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) {
            InteractionConstants.BOUNCE_SCALE_PEAK
        } else {
            InteractionConstants.NORMAL_SCALE
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "selectionScale"
    )
    return scale
}

/**
 * Modifier that applies a scale animation based on pressed state.
 * This is a simpler version that doesn't handle click events,
 * useful when the click handling is done elsewhere (e.g., by Card's onClick).
 *
 * @param isPressed Whether the component is currently pressed
 * @param pressedScale The scale to animate to when pressed
 */
@Composable
fun Modifier.animatedScale(
    isPressed: Boolean,
    pressedScale: Float = InteractionConstants.PRESSED_SCALE
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else InteractionConstants.NORMAL_SCALE,
        animationSpec = spring(
            dampingRatio = InteractionConstants.SPRING_DAMPING_PRESS,
            stiffness = InteractionConstants.SPRING_STIFFNESS_PRESS
        ),
        label = "animatedScale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Creates a tween animation spec using the standard short duration from Dimens.
 * Useful for quick transitions like color changes.
 */
fun <T> shortDurationTween() = tween<T>(durationMillis = Dimens.animationDurationShort)

/**
 * Creates a tween animation spec using the standard medium duration from Dimens.
 * Useful for more noticeable transitions.
 */
fun <T> mediumDurationTween() = tween<T>(durationMillis = Dimens.animationDurationMedium)
