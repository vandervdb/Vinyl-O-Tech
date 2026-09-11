package org.vander.android.sample.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import org.vander.android.sample.R

// Downloadable Fonts (Google Play Services) — no font files bundled in the app.
// Certificates in res/values/font_certs.xml are Google's own standard Fonts
// Provider certs (from the official Android sample), not project-specific.
private val googleFontProvider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs,
    )

// Vinyl O'Tech brand typefaces (Claude Design canvas "Vinyl OTech - App Android"):
// Space Grotesk for hero headlines, DM Sans for body/labels.
private val SpaceGrotesk =
    FontFamily(
        Font(GoogleFont("Space Grotesk"), googleFontProvider, FontWeight.SemiBold),
        Font(GoogleFont("Space Grotesk"), googleFontProvider, FontWeight.Bold),
    )

private val DMSans =
    FontFamily(
        Font(GoogleFont("DM Sans"), googleFontProvider, FontWeight.Normal),
        Font(GoogleFont("DM Sans"), googleFontProvider, FontWeight.Medium),
        Font(GoogleFont("DM Sans"), googleFontProvider, FontWeight.Bold),
    )

// Set of Material typography styles to start with
val Typography =
    Typography(
        displayMedium =
            TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 52.sp,
                lineHeight = 48.sp,
                letterSpacing = (-2.1).sp,
            ),
        displaySmall =
            TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 44.sp,
                lineHeight = 41.sp,
                letterSpacing = (-1.5).sp,
            ),
        // Hero headline (e.g. "Ta discothèque, à ta façon.") — screen 01 "Connexion Spotify".
        headlineLarge =
            TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                lineHeight = 41.sp,
                letterSpacing = (-1.5).sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 34.sp,
                letterSpacing = (-1.1).sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.5).sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                letterSpacing = (-0.2).sp,
            ),
        // Snackbar title and action label (VinylSnackbar) — design kit: 15sp
        // Space Grotesk SemiBold. Material3's own default is Roboto, which would
        // break the brand typeface on every component reading this slot.
        titleSmall =
            TextStyle(
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = DMSans,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = DMSans,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                letterSpacing = 0.sp,
            ),
        // Disclaimer/caption text.
        bodySmall =
            TextStyle(
                fontFamily = DMSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                letterSpacing = 0.4.sp,
            ),
        // Button label text (Material3's Button() defaults its Text to labelLarge).
        labelLarge =
            TextStyle(
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = DMSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                letterSpacing = 0.sp,
            ),
        // Overline: never below 11sp, 0.18em tracking, uppercase.
        labelSmall =
            TextStyle(
                fontFamily = DMSans,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                letterSpacing = 2.0.sp,
            ),
    )
