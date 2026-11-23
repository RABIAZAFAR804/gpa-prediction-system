package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class SyntheticaColors(
    val canvas: Color,
    val cardSurface: Color,
    val glassSurface: Color,
    val executiveDark: Color,
    val executiveSecondary: Color,
    val royalBlue: Color,
    val electricBlue: Color,
    val teal: Color,
    val purple: Color,
    val riskLow: Color,
    val riskModerate: Color,
    val riskHigh: Color,
    val mutedText: Color,
    val subtleStroke: Color
)

val LocalSyntheticaColors = staticCompositionLocalOf {
    SyntheticaColors(
        canvas = CanvasLight,
        cardSurface = SurfaceCard,
        glassSurface = Color(0xB8FFFFFF),
        executiveDark = NavyAnchor,
        executiveSecondary = NavyAtmosphere,
        royalBlue = RoyalBlue,
        electricBlue = ElectricBlue,
        teal = TealAccent,
        purple = PurpleAccent,
        riskLow = RiskLowGreen,
        riskModerate = RiskModerateAmber,
        riskHigh = RiskHighRed,
        mutedText = MutedSlate,
        subtleStroke = Color(0x66FFFFFF)
    )
}

private val LightColorScheme = lightColorScheme(
    primary = NavyAnchor,
    onPrimary = Color.White,
    primaryContainer = NavyAtmosphere,
    onPrimaryContainer = Color(0xFFDBE1FF),
    secondary = RoyalBlue,
    onSecondary = Color.White,
    secondaryContainer = SurfaceContainer,
    onSecondaryContainer = RoyalBlueDeep,
    tertiary = TealAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = TealDeep,
    background = CanvasLight,
    onBackground = ObsidianText,
    surface = SurfaceCard,
    onSurface = ObsidianText,
    surfaceVariant = SurfaceContainerLow,
    onSurfaceVariant = MutedSlate,
    outline = OutlineSubtle,
    outlineVariant = Color(0xFFE2E8F0),
    error = RiskHighRed,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = NavyAnchor,
    primaryContainer = NavyAtmosphere,
    onPrimaryContainer = Color(0xFFDBE1FF),
    secondary = RoyalBlue,
    onSecondary = Color.White,
    secondaryContainer = NavyDarkElevated,
    onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = TealAccent,
    onTertiary = NavyAnchor,
    tertiaryContainer = Color(0xFF134E4A),
    onTertiaryContainer = Color(0xFF99F6E4),
    background = NavyDarkCanvas,
    onBackground = Color(0xFFF1F5F9),
    surface = NavyDarkCard,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = NavyDarkElevated,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    error = Color(0xFFF87171),
    onError = NavyAnchor,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

val SyntheticaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun SyntheticaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        SyntheticaColors(
            canvas = NavyDarkCanvas,
            cardSurface = NavyDarkCard,
            glassSurface = Color(0xCC101A35),
            executiveDark = Color(0xFF070D1D),
            executiveSecondary = NavyAtmosphere,
            royalBlue = RoyalBlue,
            electricBlue = ElectricBlue,
            teal = TealAccent,
            purple = PurpleAccent,
            riskLow = Color(0xFF22C55E),
            riskModerate = RiskModerateAmber,
            riskHigh = Color(0xFFF87171),
            mutedText = Color(0xFF94A3B8),
            subtleStroke = Color(0x3360A5FA)
        )
    } else {
        SyntheticaColors(
            canvas = CanvasLight,
            cardSurface = SurfaceCard,
            glassSurface = Color(0xB8FFFFFF),
            executiveDark = NavyAnchor,
            executiveSecondary = NavyAtmosphere,
            royalBlue = RoyalBlue,
            electricBlue = ElectricBlue,
            teal = TealAccent,
            purple = PurpleAccent,
            riskLow = RiskLowGreen,
            riskModerate = RiskModerateAmber,
            riskHigh = RiskHighRed,
            mutedText = MutedSlate,
            subtleStroke = Color(0x99FFFFFF)
        )
    }

    CompositionLocalProvider(LocalSyntheticaColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SyntheticaTypography,
            shapes = SyntheticaShapes,
            content = content
        )
    }
}
