package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TradeType
import com.example.ui.components.CloudSettingsModal
import com.example.ui.components.FooterCreditsPanel
import com.example.ui.components.GlassBadge
import com.example.ui.components.Maroah3DHeader
import com.example.ui.components.TradingCategoryHeader
import com.example.ui.components.TradesListModal
import com.example.ui.components.TranslucentActionButton
import com.example.ui.components.VerticalNeonDivider
import com.example.ui.components.WalletDetailsModal
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassPanelBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonRed
import com.example.viewmodel.ActiveModal
import com.example.viewmodel.MaroahViewModel

@Composable
fun MaroahScreen(
    viewModel: MaroahViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Futuristic 3D Studio & Desk Atmosphere
        AtmosphereCanvas(modifier = Modifier.fillMaxSize())

        // Top Status Bar Controls (Settings & Cloud indicator)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cloud Status pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x55091428))
                    .border(BorderStroke(1.dp, Color(0x3300F5FF)), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isSyncing) NeonMagenta else NeonGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (uiState.isSyncing) "مزامنة سحابية..." else "Railway: متصل",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }

            // Action icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.refreshCloudData() },
                    modifier = Modifier.testTag("quick_refresh_btn")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Sync Cloud",
                        tint = NeonCyan
                    )
                }

                IconButton(
                    onClick = { viewModel.openModal(ActiveModal.CloudSettings) },
                    modifier = Modifier.testTag("cloud_settings_btn")
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Cloud Settings",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Main Glassmorphism Upright Panel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 42.dp, bottom = 8.dp, start = 12.dp, end = 12.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // The Upright Glass Panel Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x301E3558),
                                    GlassPanelBg,
                                    Color(0x3812213A)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.8.dp,
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xAA70B8FF),
                                        GlassBorderCyan,
                                        Color(0x4400E5FF),
                                        Color(0x884D94DB)
                                    )
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Header: MAROAH 3D Glowing Text
                        Maroah3DHeader()

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dual Column Layout with Center Vertical Glowing Divider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Column (Spot Trading / الفوري)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                TradingCategoryHeader(
                                    englishTitle = "spot",
                                    arabicTitle = "الفوري"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                TranslucentActionButton(
                                    text = "المحفظة",
                                    onClick = { viewModel.openModal(ActiveModal.WalletDetails(TradeType.SPOT)) },
                                    testTag = "spot_wallet_btn"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                TranslucentActionButton(
                                    text = "الصفقات",
                                    onClick = { viewModel.openModal(ActiveModal.TradesList(TradeType.SPOT)) },
                                    testTag = "spot_trades_btn"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Bottom Badges Row for Spot
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    GlassBadge(
                                        title = "خَسارة",
                                        value = "-1,750",
                                        borderColor = NeonGreen,
                                        valueColor = NeonRed,
                                        modifier = Modifier.weight(1f)
                                    )
                                    GlassBadge(
                                        title = "ربح",
                                        value = "+1,600",
                                        borderColor = NeonRed,
                                        valueColor = NeonGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Center Vertical Glowing Neon Divider
                            VerticalNeonDivider(
                                modifier = Modifier
                                    .height(340.dp)
                                    .padding(horizontal = 4.dp)
                            )

                            // Right Column (Futures Trading / الأجل)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                TradingCategoryHeader(
                                    englishTitle = "Future",
                                    arabicTitle = "الأجل"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                TranslucentActionButton(
                                    text = "المحفظة",
                                    onClick = { viewModel.openModal(ActiveModal.WalletDetails(TradeType.FUTURE)) },
                                    testTag = "future_wallet_btn"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                TranslucentActionButton(
                                    text = "الصفقات",
                                    onClick = { viewModel.openModal(ActiveModal.TradesList(TradeType.FUTURE)) },
                                    testTag = "future_trades_btn"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Bottom Badges Row for Futures
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    GlassBadge(
                                        title = "خَسارة",
                                        value = "+2.150",
                                        borderColor = NeonGreen,
                                        valueColor = NeonGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                    GlassBadge(
                                        title = "ربح",
                                        value = "-0.800",
                                        borderColor = NeonRed,
                                        valueColor = NeonRed,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Footer Base Panel
                FooterCreditsPanel(
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Notification Banner
        AnimatedVisibility(
            visible = uiState.statusNotification != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 70.dp, start = 20.dp, end = 20.dp)
        ) {
            Surface(
                color = Color(0xF00B172E),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.2.dp, NeonCyan),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = uiState.statusNotification.orEmpty(),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Modals / Dialogs
        when (val modal = uiState.activeModal) {
            is ActiveModal.WalletDetails -> {
                val summary = if (modal.type == TradeType.SPOT) uiState.spotSummary else uiState.futureSummary
                WalletDetailsModal(
                    type = modal.type,
                    summary = summary,
                    cloudStatus = uiState.cloudStatus,
                    onRefresh = { viewModel.refreshCloudData() },
                    onDismiss = { viewModel.closeModal() }
                )
            }
            is ActiveModal.TradesList -> {
                val summary = if (modal.type == TradeType.SPOT) uiState.spotSummary else uiState.futureSummary
                TradesListModal(
                    type = modal.type,
                    summary = summary,
                    onPlaceNewTrade = { symbol, side -> viewModel.placeOneDollarTrade(modal.type, symbol, side) },
                    onDismiss = { viewModel.closeModal() }
                )
            }
            is ActiveModal.CloudSettings -> {
                CloudSettingsModal(
                    currentConfig = uiState.cloudConfig,
                    onSaveConfig = { config -> viewModel.updateCloudConfig(config) },
                    onDismiss = { viewModel.closeModal() }
                )
            }
            else -> Unit
        }
    }
}

/**
 * Custom Canvas rendering the 3D futuristic desk atmosphere,
 * holographic grid lines, and soft cyan/magenta light blooms.
 */
@Composable
fun AtmosphereCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Top-center soft cyan ambient bloom
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonCyan.copy(alpha = 0.15f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.15f),
                radius = w * 0.7f
            ),
            center = Offset(w * 0.5f, h * 0.15f),
            radius = w * 0.7f
        )

        // Right side soft magenta bloom (matching the octane render in reference image)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonMagenta.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(w * 0.9f, h * 0.4f),
                radius = w * 0.6f
            ),
            center = Offset(w * 0.9f, h * 0.4f),
            radius = w * 0.6f
        )

        // Bottom desk reflection glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x330A2A54), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.92f),
                radius = w * 0.8f
            ),
            center = Offset(w * 0.5f, h * 0.92f),
            radius = w * 0.8f
        )

        // Subtle holographic grid lines in background
        val gridStep = 48.dp.toPx()
        var y = h * 0.05f
        while (y < h * 0.85f) {
            drawLine(
                color = Color(0x0C00E5FF),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
            y += gridStep
        }

        var x = 0f
        while (x < w) {
            drawLine(
                color = Color(0x0A00E5FF),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f
            )
            x += gridStep
        }
    }
}
