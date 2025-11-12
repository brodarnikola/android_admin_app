package hr.sil.android.smartlockers.adminapp.compose.view.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import hr.sil.android.smartlockers.adminapp.R

// Set of Material typography styles to start with
//val Typography = Typography(
//    bodyLarge = TextStyle(
//        fontFamily = FontFamily.Default,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp,
//        lineHeight = 24.sp,
//        letterSpacing = 0.5.sp
//    )
//    /* Other default text styles to override
//    titleLarge = TextStyle(
//        fontFamily = FontFamily.Default,
//        fontWeight = FontWeight.Normal,
//        fontSize = 22.sp,
//        lineHeight = 28.sp,
//        letterSpacing = 0.sp
//    ),
//    labelSmall = TextStyle(
//        fontFamily = FontFamily.Default,
//        fontWeight = FontWeight.Medium,
//        fontSize = 11.sp,
//        lineHeight = 16.sp,
//        letterSpacing = 0.5.sp
//    )
//    */
//)


private val AppFontFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_bold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
)

private val defaultTypography = Typography()

val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    displayMedium = defaultTypography.displayMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = defaultTypography.displaySmall.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = defaultTypography.headlineLarge.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = defaultTypography.headlineMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = defaultTypography.headlineSmall.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = defaultTypography.titleLarge.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = defaultTypography.titleMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = defaultTypography.titleSmall.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = defaultTypography.bodyLarge.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = defaultTypography.bodyMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = defaultTypography.bodySmall.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = defaultTypography.labelLarge.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = defaultTypography.labelMedium.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = defaultTypography.labelSmall.copy(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)