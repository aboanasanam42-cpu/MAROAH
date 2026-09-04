package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class HealthResponse(
    val status: String = "",
    val server: String = "",
    val serverTimeMillis: Long = 0L,
    val serverTimeIso: String = ""
)

data class MarketIndicatorsDto(
    val signal: String = "NEUTRAL",
    val confidence: Double = 0.0,
    val ema9: Double? = null,
    val ema21: Double? = null,
    val rsi14: Double? = null
)

data class MarketSnapshotDto(
    val symbol: String = "BTCUSDT",
    val futuresSymbol: String = "BTC_USDT",
    val interval: String = "5m",
    val price: Double = 0.0,
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val volume24h: Double = 0.0,
    val priceChangePercent24h: Double = 0.0,
    val indicators: MarketIndicatorsDto = MarketIndicatorsDto(),
    val candles: Int = 0,
    val source: String = "",
    val updatedAt: String = ""
)

data class MarketResponse(val success: Boolean = false, val data: MarketSnapshotDto = MarketSnapshotDto())
data class SignalDataDto(
    val symbol: String = "BTCUSDT",
    val strategy: String = "EMA 9/21 + RSI 14",
    val signal: MarketIndicatorsDto = MarketIndicatorsDto(),
    val generatedAt: String = "",
    val note: String = ""
)
data class SignalResponse(val success: Boolean = false, val data: SignalDataDto = SignalDataDto())

data class MarketStateDto(
    val currentBtcPrice: Double = 0.0,
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val lastPriceUpdate: Long = 0L
)

data class CloudServerStatusDto(
    val status: String = "",
    val server: String = "",
    val version: String = "",
    val timestamp: String = "",
    val serverTimeMillis: Long = 0L,
    val pair: String = "",
    val marketState: MarketStateDto? = null
)

data class AssetDto(
    val coin: String = "",
    val freeAmount: Double = 0.0,
    val lockedAmount: Double = 0.0,
    val usdtValue: Double = 0.0
)

data class BalanceResponse(
    val source: String? = null,
    val serverTimeMillis: Long = 0L,
    val serverTimeIso: String = "",
    val balanceUsdt: Double? = null,
    val profitValue: Double? = null,
    val lossValue: Double? = null,
    val activeTradesCount: Int? = null,
    val assets: List<AssetDto> = emptyList()
)

data class TradeItemDto(
    val id: String = "",
    val symbol: String = "BTC/USDT",
    val type: String = "SPOT",
    val side: String = "BUY",
    val amountUsd: Double = 0.0,
    val entryPrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val pnl: Double = 0.0,
    val pnlPercent: Double = 0.0,
    val timestamp: Long = 0L,
    val status: String = "",
    val strategy: String? = null
)

data class TradesResponse(val serverTimeMillis: Long = 0L, val trades: List<TradeItemDto> = emptyList())
data class BotWalletStateDto(
    val balance: Double? = null,
    val openOrdersCount: Int? = null,
    val openPositionsCount: Int? = null,
    val profit: Double? = null,
    val loss: Double? = null
)
data class BotStatusDataDto(
    val spot: BotWalletStateDto = BotWalletStateDto(),
    val future: BotWalletStateDto = BotWalletStateDto(),
    val lastUpdated: String = "",
    val botStatus: String = "LIVE_TELEMETRY",
    val btcPrice: Double = 0.0,
    val signal: MarketIndicatorsDto = MarketIndicatorsDto(),
    val serverTimeMillis: Long = 0L
)
data class BotStatusResponse(val success: Boolean = false, val data: BotStatusDataDto = BotStatusDataDto())

data class TradeRequest(val type: String, val symbol: String, val side: String, val amountUsd: Double = 0.0, val timestamp: Long = System.currentTimeMillis())
data class TradeExecutionResponse(
    val success: Boolean = false,
    val message: String = "",
    val order: TradeItemDto? = null,
    val serverTimeMillis: Long = 0L,
    val serverTimeIso: String = "",
    val activeTradesCount: Int? = null,
    val profitValue: Double? = null,
    val lossValue: Double? = null,
    val realMexcExecuted: Boolean? = null
)

interface MaroahApiService {
    @GET("/") suspend fun getCloudStatus(): CloudServerStatusDto
    @GET("api/bot-status") suspend fun getBotStatus(): BotStatusResponse
    @GET("api/health") suspend fun checkHealth(): HealthResponse
    @GET("api/market") suspend fun getMarket(): MarketResponse
    @GET("api/signals") suspend fun getSignals(): SignalResponse
    @GET("api/balance/spot") suspend fun getSpotBalance(): BalanceResponse
    @GET("api/balance/futures") suspend fun getFuturesBalance(): BalanceResponse
    @GET("api/trades") suspend fun getTrades(@Query("type") type: String? = null): TradesResponse

    // Retained for source compatibility. The current cloud service does not submit orders.
    @POST("api/trade/place-order") suspend fun placeOrder(@Body request: TradeRequest): TradeExecutionResponse
    @POST("api/trade/spot") suspend fun executeSpotTrade(@Body request: TradeRequest): TradeExecutionResponse
    @POST("api/trade/futures") suspend fun executeFuturesTrade(@Body request: TradeRequest): TradeExecutionResponse
    @POST("api/trade") suspend fun executeTrade(@Body request: TradeRequest): TradeExecutionResponse
}

object NetworkClient {
    const val DEFAULT_RAILWAY_URL = "https://maroah-production.up.railway.app/"
    private const val DEFAULT_VERCEL_URL = ""

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    @Synchronized
    fun create(baseUrl: String): MaroahApiService = Retrofit.Builder()
        .baseUrl(if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/")
        .client(http)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(MaroahApiService::class.java)

    fun railway(): MaroahApiService = create(DEFAULT_RAILWAY_URL)
    fun vercel(): MaroahApiService? = DEFAULT_VERCEL_URL.takeIf { it.isNotBlank() }?.let(::create)
}

/**
 * Passive cloud failover: Railway is primary, optional Vercel endpoint is backup.
 * It reads live telemetry/signals only; it never embeds exchange credentials.
 */
class MaroahFailoverClient(
    private val primary: MaroahApiService = NetworkClient.railway(),
    private val backup: MaroahApiService? = NetworkClient.vercel()
) {
    suspend fun market(): MarketResponse = withFailover({ primary.getMarket() }, { backup?.getMarket() })
    suspend fun signals(): SignalResponse = withFailover({ primary.getSignals() }, { backup?.getSignals() })
    suspend fun botStatus(): BotStatusResponse = withFailover({ primary.getBotStatus() }, { backup?.getBotStatus() })
    suspend fun health(): HealthResponse = withFailover({ primary.checkHealth() }, { backup?.checkHealth() })
    suspend fun spotBalance(): BalanceResponse = withFailover({ primary.getSpotBalance() }, { backup?.getSpotBalance() })

    private suspend fun <T> withFailover(primaryCall: suspend () -> T, backupCall: (suspend () -> T)?): T {
        return try {
            primaryCall()
        } catch (primaryError: Throwable) {
            if (backupCall == null) throw primaryError
            backupCall()
        }
    }
}
