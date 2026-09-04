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
    val symbol: String = "BTC/USDT", // Strictly Bitcoin vs USDT
    val type: TradeType,
    val side: OrderSide,
    val amountUsd: Double = 1.0, // Fixed $1 USD
    val entryPrice: Double,
    val currentPrice: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "FILLED",
    val strategy: String = "BTC Range Profit Capture"
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
    val serverUrl: String = "https://maroah-production.up.railway.app",
    val fallbackUrl: String = "https://maroah.vercel.app",
    val hummingbotGatewayUrl: String = "http://localhost:15888",
    val sessionToken: String = "msIECkh7qAZXR5BfSpvTTCopXpvgDsOSyCyHMUKR0KA=",
    val mexcApiKey: String = "",
    val mexcSecretKey: String = "",
    val brokerName: String = "Hummingbot (MEXC Official Broker)",
    val isLiveCloudMode: Boolean = true
)

data class MexcDiagnosticResult(
    val serverTimeMillis: Long = 0L,
    val broker: String = "Hummingbot",
    val mexcPublicApiPing: Boolean = false,
    val mexcLatencyMs: Long = 0L,
    val timeDriftMs: Long = 0L,
    val keysConfigured: Boolean = false,
    val mexcAuthValid: Boolean = false,
    val spotBalanceUsdt: Double = 0.0,
    val futuresBalanceUsdt: Double = 0.0,
    val hummingbotGatewayActive: Boolean = false,
    val message: String = ""
)
