package io.asterixorobelix.afrikaburn

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import io.asterixorobelix.afrikaburn.Dimens.cornerRadiusLarge
import io.asterixorobelix.afrikaburn.Dimens.cornerRadiusMedium
import io.asterixorobelix.afrikaburn.Dimens.cornerRadiusSmall

@Suppress("MagicNumber")
val LightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

// Desert nighttime optimized dark theme - designed for reduced eye strain
// in low-light conditions with better contrast for outdoor use
@Suppress("MagicNumber")
val DarkColors = darkColorScheme(
    primary = Color(0xFFFF9800),  // Warm orange for primary actions (like firelight)
    onPrimary = Color(0xFF3E2723),
    primaryContainer = Color(0xFF795548),
    onPrimaryContainer = Color(0xFFFFE0B2),
    secondary = Color(0xFFFFEB3B),  // Yellow for secondary (starlight)
    onSecondary = Color(0xFF3E2723),
    secondaryContainer = Color(0xFF5D4037),
    onSecondaryContainer = Color(0xFFFFF9C4),
    background = Color(0xFF000000),  // True black for OLED battery saving
    onBackground = Color(0xFFF5F5F5),  // High contrast text
    surface = Color(0xFF1A1A1A),  // Very dark surface
    onSurface = Color(0xFFF5F5F5),
    error = Color(0xFFFF5252),  // Bright red for visibility
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFCDD2),
    tertiary = Color(0xFF81C784),  // Green for safety/environmental features
    onTertiary = Color(0xFF1B5E20),
    tertiaryContainer = Color(0xFF2E7D32),
    onTertiaryContainer = Color(0xFFC8E6C9),
)

val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadiusSmall),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadiusMedium),
    large = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadiusLarge)
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 57.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.Normal
    ),
    displayMedium = TextStyle(
        fontSize = 45.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.Normal
    ),
    displaySmall = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Normal
    ),
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Normal
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Normal
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Normal
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Normal
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    )
)

object Dimens {
    // Padding
    val paddingExtraSmall = 4.dp
    val paddingSmall = 8.dp
    val paddingMedium = 16.dp
    val paddingLarge = 24.dp

    // Icon sizes
    val iconSizeSmall = 16.dp
    val iconSizeMedium = 24.dp
    val iconSizeLarge = 48.dp
    val iconSizeExtraLarge = 64.dp

    // Sizes
    val cornerRadiusXSmall = 2.dp
    val cornerRadiusSmall = 4.dp
    val cornerRadiusMedium = 8.dp
    val cornerRadiusLarge = 16.dp
    val dropdownMaxHeight = 200.dp
    val buttonHeightSmall = 40.dp
    val chipHeight = 24.dp

    // Elevation
    val elevationSmall = 2.dp
    val elevationNormal = 8.dp
}

@Composable
fun AppTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        typography = AppTypography,
        content = content
    )
}
