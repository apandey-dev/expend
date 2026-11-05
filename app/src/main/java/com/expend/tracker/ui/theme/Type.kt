package com.expend.tracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.expend.tracker.R

val MaliFamily = FontFamily(
    Font(R.font.mali_regular, FontWeight.Normal),
    Font(R.font.mali_medium, FontWeight.Medium),
    Font(R.font.mali_semibold, FontWeight.SemiBold),
    Font(R.font.mali_bold, FontWeight.Bold)
)

val AppTypography = Typography(
    // Main screen titles: Mali SemiBold
    titleLarge = TextStyle(
        fontFamily = MaliFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    // Section headings: Mali Medium
    titleMedium = TextStyle(
        fontFamily = MaliFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    // Transaction names / headers: Mali Medium
    titleSmall = TextStyle(
        fontFamily = MaliFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    // Body text: Mali Regular
    bodyLarge = TextStyle(
        fontFamily = MaliFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MaliFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    // Supporting information / captions: Mali Regular
    bodySmall = TextStyle(
        fontFamily = MaliFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Buttons and labels: Mali Medium
    labelLarge = TextStyle(
        fontFamily = MaliFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MaliFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MaliFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)
