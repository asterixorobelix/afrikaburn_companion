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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.bitter_regular
import org.jetbrains.compose.resources.Font
import io.asterixorobelix.afrikaburn.Dimens.cornerRadiusLarge
import io.asterixorobelix.afrikaburn.Dimens.cornerRadiusMedium
import io.asterixorobelix.afrikaburn.Dimens.cornerRadiusSmall

@Suppress("MagicNumber")
val LightColors = lightColorScheme(
    primary = Color(0xFF8B5E34),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDDB5),
    onPrimaryContainer = Color(0xFF2E1500),
    secondary = Color(0xFF6F5B40),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFBDEBC),
    onSecondaryContainer = Color(0xFF271904),
    tertiary = Color(0xFF51643F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD4EABB),
    onTertiaryContainer = Color(0xFF0F2004),
    background = Color(0xFFFFF8F2),
    onBackground = Color(0xFF201A17),
    surface = Color(0xFFFFF8F2),
    onSurface = Color(0xFF201A17),
    surfaceVariant = Color(0xFFF3DFD0),
    onSurfaceVariant = Color(0xFF52443B),
    outline = Color(0xFF857469),
    outlineVariant = Color(0xFFD8C3B6),
    inverseSurface = Color(0xFF362F2B),
    inverseOnSurface = Color(0xFFFBEEE6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFEF1E9),
    surfaceContainer = Color(0xFFF8ECE3),
    surfaceContainerHigh = Color(0xFFF2E6DD),
    surfaceContainerHighest = Color(0xFFECE0D7),
    error = Color(0xFFC62828),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

@Suppress("MagicNumber")
val DarkColors = darkColorScheme(
    primary = Color(0xFFFFBA6E),
    onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF6A4320),
    onPrimaryContainer = Color(0xFFFFDDB5),
    secondary = Color(0xFFDFC2A2),
    onSecondary = Color(0xFF3F2D17),
    secondaryContainer = Color(0xFF57432B),
    onSecondaryContainer = Color(0xFFFBDEBC),
    tertiary = Color(0xFFB8CEA0),
    onTertiary = Color(0xFF243514),
    tertiaryContainer = Color(0xFF3A4C29),
    onTertiaryContainer = Color(0xFFD4EABB),
    background = Color(0xFF1A1511),
    onBackground = Color(0xFFF0DFD2),
    surface = Color(0xFF1A1511),
    onSurface = Color(0xFFF0DFD2),
    surfaceVariant = Color(0xFF52443B),
    onSurfaceVariant = Color(0xFFD8C3B6),
    outline = Color(0xFFA08D82),
    outlineVariant = Color(0xFF52443B),
    inverseSurface = Color(0xFFF0DFD2),
    inverseOnSurface = Color(0xFF362F2B),
    surfaceContainerLowest = Color(0xFF140F0B),
    surfaceContainerLow = Color(0xFF201A17),
    surfaceContainer = Color(0xFF251F1B),
    surfaceContainerHigh = Color(0xFF302925),
    surfaceContainerHighest = Color(0xFF3B3430),
    error = Color(0xFFEF9A9A),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
)

val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadiusSmall),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadiusMedium),
    large = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadiusLarge)
)

@Suppress("LongMethod")
@Composable
fun appTypography(): Typography {
    val bitterFamily = FontFamily(
        Font(Res.font.bitter_regular, weight = FontWeight.Normal),
        Font(Res.font.bitter_regular, weight = FontWeight.Medium),
    )
    return Typography(
        displayLarge = TextStyle(
            fontFamily = bitterFamily,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            fontWeight = FontWeight.Normal
        ),
        displayMedium = TextStyle(
            fontFamily = bitterFamily,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            fontWeight = FontWeight.Normal
        ),
        displaySmall = TextStyle(
            fontFamily = bitterFamily,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.Normal
        ),
        headlineLarge = TextStyle(
            fontFamily = bitterFamily,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Normal
        ),
        headlineMedium = TextStyle(
            fontFamily = bitterFamily,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Normal
        ),
        headlineSmall = TextStyle(
            fontFamily = bitterFamily,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Normal
        ),
        titleLarge = TextStyle(
            fontFamily = bitterFamily,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Medium
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
}

object Dimens {
    // Padding
    val paddingExtraSmall = 4.dp
    val paddingSmall = 8.dp
    val paddingMedium = 16.dp
    val paddingLarge = 24.dp
    val paddingExtraLarge = 32.dp

    // Spacing (for arrangement and gaps between elements)
    val spacingExtraSmall = 4.dp
    val spacingSmall = 8.dp
    val spacingMedium = 12.dp
    val spacingLarge = 16.dp
    val spacingExtraLarge = 24.dp
    val sectionSpacing = 20.dp

    // Icon sizes
    val iconSizeSmall = 16.dp
    val iconSizeMedium = 24.dp
    val iconSizeLarge = 48.dp
    val iconSizeExtraLarge = 64.dp
    val iconSizeHero = 96.dp

    // Sizes
    val cornerRadiusXSmall = 2.dp
    val cornerRadiusSmall = 4.dp
    val cornerRadiusMedium = 8.dp
    val cornerRadiusLarge = 16.dp
    val dropdownMaxHeight = 200.dp

    // Elevation
    val elevationSmall = 2.dp
    val elevationMedium = 4.dp
    val elevationNormal = 8.dp

    // Divider
    val dividerThickness = 1.dp

    // Animation durations (in milliseconds)
    const val animationDurationShort = 150
    const val animationDurationMedium = 300
    const val animationDurationLong = 450
    const val staggerDelayPerItem = 50

    // Skeleton loading dimensions
    val skeletonLineHeightSmall = 14.dp
    val skeletonLineHeightMedium = 18.dp
    val skeletonLineHeightLarge = 24.dp
    val skeletonBadgeHeight = 28.dp
    val skeletonBadgeWidth = 100.dp

    // Page indicator dimensions
    val indicatorDotSizeSmall = 8.dp
    val indicatorDotSizeLarge = 12.dp
    val indicatorSpacing = 10.dp

    // Image sizes
    val aboutImageSize = 160.dp
    val aboutImageSizeLarge = 200.dp

    // Card dimensions
    val cardContentPaddingHorizontal = 20.dp
    val cardContentPaddingVertical = 24.dp

    // Swipe hint dimensions
    val swipeHintIconSize = 20.dp

    // Directions screen dimensions
    val directionsHeroIconSize = 72.dp
    val directionsHeroContainerSize = 120.dp
    val directionsSectionIconSize = 28.dp
    val travelTimeCardMinWidth = 100.dp
    val noteCardIconSize = 40.dp
    val gpsRowHeight = 56.dp

    // Map screen dimensions
    val mapFabBottomPadding = 80.dp  // Clear MapLibre attribution
    val mapLegendTopPadding = 48.dp  // Clear scale bar
    val mapLegendToggleSize = 40.dp
    val fabSize = 56.dp
    val fabSizeSmall = 40.dp
}

@Composable
fun AppTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        typography = appTypography(),
        content = content
    )
}
