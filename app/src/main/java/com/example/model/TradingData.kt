package com.example.model

enum class TradeType { SPOT, FUTURE }
enum class OrderSide { BUY, SELL }

data class TradeOrder(
    val id: String,
    val symbol: String = "BTC/USDT",
    val type: TradeType,
    val side: OrderSide,
    val amountUsd: Double = 1.0,
    val entryPrice: Double,
    val currentPrice: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "FILLED",
    val strategy: String = "BTC Range Profit Capture"
)

data class WalletAsset(val coin: String, val freeAmount: Double, val lockedAmount: Double, val usdtValue: Double)

data class ColumnSummary(
    val balanceUsdt: Double,
    val profitValue: Double,
    val lossValue: Double,
    val activeTradesCount: Int,
    val assets: List<WalletAsset> = emptyList(),
    val trades: List<TradeOrder> = emptyList()
)

data class CloudConfig(
    val serverUrl: String = "https://maroah-production.up.railway.app",
    val fallbackUrl: String = "https://maroah.vercel.app",
    // Never embed an authentication secret in the APK. Server-side auth is authoritative.
    val sessionToken: String = "",
    val isLiveCloudMode: Boolean = true
)
