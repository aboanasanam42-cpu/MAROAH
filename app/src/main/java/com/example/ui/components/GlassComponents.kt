package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FooterSlateDark
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassPanelBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonRed

/**
 * 3D Crystal Header for "MAROAH" with layered glowing light reflections.
 */
@Composable
fun Maroah3DHeader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "crystal_shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow layer
        Text(
            text = "MAROAH",
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 6.sp,
            fontFamily = FontFamily.SansSerif,
            color = NeonCyan.copy(alpha = 0.45f * shimmerAlpha),
            modifier = Modifier.padding(top = 2.dp)
        )

        // 3D Bevel/Depth Shadow layer
        Text(
            text = "MAROAH",
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 6.sp,
            fontFamily = FontFamily.SansSerif,
            color = Color(0x99003366),
            modifier = Modifier.padding(top = 3.dp, start = 2.dp)
        )

        // Crystal face layer with cyan-white shimmer
        Text(
            text = "MAROAH",
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 6.sp,
            fontFamily = FontFamily.SansSerif,
            color = Color.White.copy(alpha = 0.95f),
            modifier = Modifier.drawBehind {
                // Top crystal highlight reflection
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NeonCyan.copy(alpha = 0.8f * shimmerAlpha),
                            Color.White.copy(alpha = 0.9f),
                            NeonCyan.copy(alpha = 0.8f * shimmerAlpha),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, size.height * 0.2f),
                    end = Offset(size.width, size.height * 0.2f),
                    strokeWidth = 2.dp.toPx()
                )
            }
        )
    }
}

/**
 * Vertical Glowing Neon Light Strip Divider (Center Capsule).
 */
@Composable
fun VerticalNeonDivider(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .width(18.dp)
            .fillMaxHeight()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glass capsule tube
        Box(
            modifier = Modifier
                .width(14.dp)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x3300F5FF),
                            Color(0x2213233F),
                            Color(0x3300F5FF)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        1.2.dp,
                        Brush.verticalGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.7f),
                                Color(0x3300E5FF),
                                NeonCyan.copy(alpha = 0.7f)
                            )
                        )
                    ),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner vibrant glowing light rod
            Box(
                modifier = Modifier
                    .width(4.5.dp)
                    .fillMaxHeight(0.96f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = pulseAlpha),
                                Color(0xFF80F9FF).copy(alpha = pulseAlpha),
                                Color.White.copy(alpha = pulseAlpha)
                            )
                        )
                    )
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(3.dp),
                        ambientColor = NeonCyan,
                        spotColor = NeonCyan
                    )
            )
        }
    }
}

/**
 * Top Frosted Glass Category Box (e.g., "spot" / "الفوري" or "Future" / "الأجل").
 */
@Composable
fun TradingCategoryHeader(
    englishTitle: String,
    arabicTitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x3328446B),
                        Color(0x2015243F),
                        Color(0x301E3355)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.6.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x9988DFFF),
                            Color(0x4000C8FF),
                            Color(0x6600E5FF)
                        )
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = englishTitle,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = arabicTitle,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Dark Translucent Rounded Action Button (e.g. "المحفظة" or "الصفقات").
 */
@Composable
fun TranslucentActionButton(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xCC1A2B47),
                        Color(0xEE0E182A),
                        Color(0xDD15233D)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.4.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x8870A5D6),
                            Color(0x442C4C74),
                            Color(0x775080B0)
                        )
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = NeonCyan),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Subtle top glass light reflection line
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.9f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x66FFFFFF),
                            Color.Transparent
                        )
                    )
                )
        )

        Text(
            text = text,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Glass Badge for Profit ("ربح") or Loss ("خَسارة") with distinct colored borders and values.
 */
@Composable
fun GlassBadge(
    title: String,
    value: String,
    borderColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xDD15233D),
                        Color(0xEE0C1424)
                    )
                )
            )
            .border(
                BorderStroke(2.dp, borderColor),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Solid Slate Blue / Frosted Glass Footer Panel with exact specified credits.
 */
@Composable
fun FooterCreditsPanel(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF212213D),
                        FooterSlateDark,
                        Color(0xF50A1426)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x885B88BA),
                            Color(0x333A5A80),
                            Color(0x664A709A)
                        )
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "تصميم وبرمجة الدكتور/ مالك الرميمة",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "هاتف 771134103",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        // Sparkle icon in the corner matching the reference image
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Decor",
            tint = Color.White.copy(alpha = 0.45f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 4.dp)
                .size(18.dp)
        )
    }
}
