package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CloudConfig
import com.example.model.ColumnSummary
import com.example.model.OrderSide
import com.example.model.TradeOrder
import com.example.model.TradeType
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonRed
import java.util.Locale

/**
 * Wallet Details Dialog (Spot or Futures / Agile).
 */
@Composable
fun WalletDetailsModal(
    type: TradeType,
    summary: ColumnSummary,
    cloudStatus: String,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = if (type == TradeType.SPOT) "محفظة الفوري (Spot Wallet)" else "محفظة الأجل (Futures Agile Wallet)"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF80B1424)),
            border = BorderStroke(1.5.dp, Color(0x6600E5FF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        fontSize = 18.sp,
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
                        Text(
                            text = String.format(Locale.US, "$%,.2f USDT", summary.balanceUsdt),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown of assets
                Text(
                    text = "الأصول المتوفرة في حساب MEXC:",
                    fontSize = 14.sp,
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
                            text = String.format(Locale.US, "$%,.2f", asset.usdtValue),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = asset.coin,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = String.format(Locale.US, "المتاح: %.4f", asset.freeAmount),
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
 * Trades Dialog (Spot or Futures / Agile) showing $1 trade size.
 */
@Composable
fun TradesListModal(
    type: TradeType,
    summary: ColumnSummary,
    onPlaceNewTrade: (String, OrderSide) -> Unit,
    onDismiss: () -> Unit
) {
    val title = if (type == TradeType.SPOT) "صفقات الفوري (Spot $1 Trades)" else "صفقات الأجل (Futures Agile $1 Trades)"
    var selectedSymbol by remember { mutableStateOf(if (type == TradeType.SPOT) "BTC/USDT" else "BTC-PERP") }

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
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Text(
                            text = "حجم الصفقة الواحدة: 1 دولار أمريكي (Fixed 1$)",
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
                        onClick = { onPlaceNewTrade(selectedSymbol, OrderSide.BUY) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("place_buy_trade_button")
                    ) {
                        Text("شراء (1$)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onPlaceNewTrade(selectedSymbol, OrderSide.SELL) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("place_sell_trade_button")
                    ) {
                        Text("بيع (1$)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "سجل الصفقات المنفذة الأخيرة:",
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
                text = "قيمة: $1.00",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        // Price info
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format(Locale.US, "$%,.2f", trade.currentPrice),
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

        // Symbol & Side
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = trade.symbol,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "$sideText (${trade.status})",
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
    onSaveConfig: (CloudConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var serverUrl by remember { mutableStateOf(currentConfig.serverUrl) }
    var mexcApiKey by remember { mutableStateOf(currentConfig.mexcApiKey) }
    var mexcSecretKey by remember { mutableStateOf(currentConfig.mexcSecretKey) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_dialog")) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Text(
                            text = "إعدادات سيرفر Railway السحابي",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "رابط السيرفر العام (Public Base URL):",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0x4400E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        placeholder = { Text("https://maroah-production-33c3.up.railway.app", color = Color.Gray, fontSize = 12.sp) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Server Info Badge
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0x3300E5FF)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "حالة الخادم المستقل (Railway):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Text(
                                text = "• المنفذ المستهدف: 8080 (PORT)\n• مفاتيح API مخزنة وموقعة بأمان داخل السيرفر:\n  MEXC_APP_KEY, MEXC_APP_SECRET\n  MEXC_BLOCKBEAT_KEY, MEXC_BLOCKBEAT_SECRET",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "مفتاح MEXC_APP_KEY (اختياري / احتياطي):",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = mexcApiKey,
                        onValueChange = { mexcApiKey = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mexc_api_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0x4400E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        placeholder = { Text("مخزن مسبقاً في Railway Dashboard", color = Color.Gray, fontSize = 12.sp) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "مفتاح MEXC_APP_SECRET (احتياطي):",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = mexcSecretKey,
                        onValueChange = { mexcSecretKey = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mexc_secret_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0x4400E5FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        placeholder = { Text("مخزن وموقع سحابياً بأمان", color = Color.Gray, fontSize = 12.sp) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onSaveConfig(
                                currentConfig.copy(
                                    serverUrl = serverUrl,
                                    mexcApiKey = mexcApiKey,
                                    mexcSecretKey = mexcSecretKey
                                )
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_settings_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ وتوجيه الاتصال إلى Railway", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
