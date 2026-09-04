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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CloudConfig
import com.example.model.ColumnSummary
import com.example.model.MexcDiagnosticResult
import com.example.model.OrderSide
import com.example.model.TradeOrder
import com.example.model.TradeType
import com.example.ui.theme.GlassBorderCyan
import com.example.ui.theme.GlassPanelBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonRed
import java.util.Locale

/**
 * Wallet Details Dialog (Spot or Futures / Agile) showing live MEXC reading.
 */
@Composable
fun WalletDetailsModal(
    type: TradeType,
    summary: ColumnSummary,
    cloudStatus: String,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = if (type == TradeType.SPOT) "محفظة الفوري (MEXC Spot Wallet)" else "محفظة الأجل (MEXC Futures Margin)"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF8091322)),
            border = BorderStroke(1.5.dp, Color(0x6600E5FF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_wallet_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Balance Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x3300F5FF), Color(0x220D1A30))
                            )
                        )
                        .border(BorderStroke(1.dp, Color(0x4400E5FF)), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "إجمالي الرصيد المقروء من المنصة",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val formattedBalance = if (type == TradeType.SPOT) {
                            String.format(Locale.US, "$%.2f USDT", summary.balanceUsdt)
                        } else {
                            String.format(Locale.US, "$%.3f USDT", summary.balanceUsdt)
                        }
                        Text(
                            text = formattedBalance,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown of assets
                Text(
                    text = "الأصول المتوفرة في حساب MEXC (BTC/USDT):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(8.dp))

                summary.assets.forEach { asset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x3312213D))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.US, "$%,.3f", asset.usdtValue),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = asset.coin,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = String.format(Locale.US, "المتاح: %.6f", asset.freeAmount),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CloudDone,
                        contentDescription = "Cloud Status",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cloudStatus,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRefresh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("refresh_wallet_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تحديث فوري من سيرفر Railway",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Trades Dialog (Spot or Futures / Agile) showing $1 trade size on BTC only.
 */
@Composable
fun TradesListModal(
    type: TradeType,
    summary: ColumnSummary,
    onPlaceNewTrade: (String, OrderSide) -> Unit,
    onDismiss: () -> Unit
) {
    val title = if (type == TradeType.SPOT) "صفقات الفوري (BTC/USDT - 1$)" else "صفقات الأجل (BTC-PERP - 1$)"
    val btcSymbol = if (type == TradeType.SPOT) "BTC/USDT" else "BTC-PERP"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF80A1220)),
            border = BorderStroke(1.5.dp, Color(0x6600E5FF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_trades_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "حجم الصفقة: 1 دولار | زوج: $btcSymbol",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick execute $1 Trade row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x3314223A))
                        .border(BorderStroke(1.dp, Color(0x3300E5FF)), RoundedCornerShape(14.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onPlaceNewTrade(btcSymbol, OrderSide.BUY) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("place_buy_trade_button")
                    ) {
                        Text("شراء (1$ BTC)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onPlaceNewTrade(btcSymbol, OrderSide.SELL) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("place_sell_trade_button")
                    ) {
                        Text("بيع (1$ BTC)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "سجل الصفقات المنفذة (الربح وتتبع الأسعار):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Trades List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    items(summary.trades) { trade ->
                        TradeItemRow(trade = trade)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TradeItemRow(trade: TradeOrder) {
    val isPositive = trade.pnl >= 0
    val pnlColor = if (isPositive) NeonGreen else NeonRed
    val sideColor = if (trade.side == OrderSide.BUY) NeonGreen else NeonRed
    val sideText = if (trade.side == OrderSide.BUY) "شراء" else "بيع"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x3013223A))
            .border(BorderStroke(1.dp, Color(0x224F7EAD)), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // PnL & Amount
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = String.format(Locale.US, "%+.4f USD", trade.pnl),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = pnlColor
            )
            Text(
                text = "حجم: 1.00$",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        // Price info
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(Locale.US, "$%,.1f", trade.currentPrice),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = String.format(Locale.US, "%+.2f%%", trade.pnlPercent),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = pnlColor
            )
        }

        // Symbol & Strategy
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = trade.symbol,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "$sideText (${trade.strategy})",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = sideColor
            )
        }
    }
}

/**
 * Cloud and API Keys Configuration Modal.
 */
@Composable
fun CloudSettingsModal(
    currentConfig: CloudConfig,
    isTestingDiagnostic: Boolean = false,
    diagnosticResult: MexcDiagnosticResult? = null,
    onRunDiagnostic: () -> Unit = {},
    onSaveConfig: (CloudConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var serverUrl by remember { mutableStateOf(currentConfig.serverUrl) }
    var fallbackUrl by remember { mutableStateOf(currentConfig.fallbackUrl) }
    var hummingbotGatewayUrl by remember { mutableStateOf(currentConfig.hummingbotGatewayUrl) }
    var sessionToken by remember { mutableStateOf(currentConfig.sessionToken) }
    var mexcApiKey by remember { mutableStateOf(currentConfig.mexcApiKey) }
    var mexcSecretKey by remember { mutableStateOf(currentConfig.mexcSecretKey) }
    var showSecretKey by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF80B1424)),
            border = BorderStroke(1.5.dp, Color(0x6600E5FF))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                item {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_dialog")) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "إعدادات الربط والوسيط MEXC",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "وسيط الارتباط المعتمد: Hummingbot",
                                fontSize = 11.sp,
                                color = NeonGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notice Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2200E5FF))
                            .border(BorderStroke(1.dp, Color(0x3300E5FF)), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = "Security", tint = NeonGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "اتصال حقيقي 100% مع MEXC عبر خوارزميات الوسيط Hummingbot والسحابة المستقلة لضمان تنفيذ حقيقي للأوامر بمقدار 1$.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // MEXC API Credentials Section
                    Text(
                        text = "مفتاح MEXC API Key:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = mexcApiKey,
                        onValueChange = { mexcApiKey = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mexc_api_key_input"),
                        placeholder = { Text("أدخل MEXC API Key للتداول الحقيقي", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0x4400E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "المفتاح السري MEXC Secret Key:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = mexcSecretKey,
                        onValueChange = { mexcSecretKey = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mexc_secret_key_input"),
                        placeholder = { Text("أدخل MEXC Secret Key", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp) },
                        visualTransformation = if (showSecretKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showSecretKey = !showSecretKey }) {
                                Icon(
                                    if (showSecretKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Secret",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0x4400E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hummingbot Gateway URL
                    Text(
                        text = "رابط وسيط Hummingbot Gateway:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = hummingbotGatewayUrl,
                        onValueChange = { hummingbotGatewayUrl = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hummingbot_gateway_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0x4400E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "رابط سيرفر Railway السحابي (الرئيسي):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_url_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0x4400E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "رابط السيرفر الاحتياطي Vercel (Fallback):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = fallbackUrl,
                        onValueChange = { fallbackUrl = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fallback_url_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0x4400E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "رمز الجلسة المشفر (Session Token):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = sessionToken,
                        onValueChange = { sessionToken = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("session_token_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0x4400E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Diagnostic Test Button
                    OutlinedButton(
                        onClick = onRunDiagnostic,
                        enabled = !isTestingDiagnostic,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("run_diagnostic_btn"),
                        border = BorderStroke(1.2.dp, NeonGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isTestingDiagnostic) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = NeonGreen,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري فحص الاتصال الحقيقي مع MEXC...", color = NeonGreen, fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.Speed, contentDescription = "Test", tint = NeonGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚡ اختبار الاتصال الحقيقي مع MEXC و Hummingbot",
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Diagnostic Results Card if available
                    if (diagnosticResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0x3300E5FF)),
                            border = BorderStroke(1.dp, if (diagnosticResult.mexcAuthValid || diagnosticResult.mexcPublicApiPing) NeonGreen else NeonCyan)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (diagnosticResult.mexcAuthValid || diagnosticResult.mexcPublicApiPing) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = "Status",
                                        tint = if (diagnosticResult.mexcAuthValid || diagnosticResult.mexcPublicApiPing) NeonGreen else NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = diagnosticResult.message,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "الوسيط: ${diagnosticResult.broker} • الاستجابة: ${diagnosticResult.mexcLatencyMs}ms • الفارق الزمني: ${diagnosticResult.timeDriftMs}ms",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "الأرصدة الحقيقية: Spot: $${diagnosticResult.spotBalanceUsdt} USDT | Futures: $${diagnosticResult.futuresBalanceUsdt} USDT",
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Button
                    Button(
                        onClick = {
                            onSaveConfig(
                                currentConfig.copy(
                                    serverUrl = serverUrl.trim(),
                                    fallbackUrl = fallbackUrl.trim(),
                                    hummingbotGatewayUrl = hummingbotGatewayUrl.trim(),
                                    sessionToken = sessionToken.trim(),
                                    mexcApiKey = mexcApiKey.trim(),
                                    mexcSecretKey = mexcSecretKey.trim()
                                )
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_cloud_config_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حفظ ومزامنة فورية مع السحابة و MEXC",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
