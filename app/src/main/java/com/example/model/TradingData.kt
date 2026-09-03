package com.example.model

enum class TradeType {
    SPOT,
    FUTURE
}

enum class OrderSide {
    BUY,
    SELL
}

data class TradeOrder(
    val id: String,
    val symbol: String,
    val type: TradeType,
    val side: OrderSide,
    val amountUsd: Double = 1.0, // Fixed $1 USD as requested
    val entryPrice: Double,
    val currentPrice: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "FILLED"
)

data class WalletAsset(
    val coin: String,
    val freeAmount: Double,
    val lockedAmount: Double,
    val usdtValue: Double
)

data class ColumnSummary(
    val balanceUsdt: Double,
    val profitValue: Double,
    val lossValue: Double,
    val activeTradesCount: Int,
    val assets: List<WalletAsset> = emptyList(),
    val trades: List<TradeOrder> = emptyList()
)

data class CloudConfig(
    val serverUrl: String = "https://maroah-production-33c3.up.railway.app",
    val mexcApiKey: String = "",
    val mexcSecretKey: String = "",
    val isLiveCloudMode: Boolean = true
)
