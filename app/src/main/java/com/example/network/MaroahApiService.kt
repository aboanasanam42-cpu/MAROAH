package com.example.network

import com.squareup.moshi.Json
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
    val balanceUsdt: Double = 0.0,
    val profitValue: Double = 0.0,
    val lossValue: Double = 0.0,
    val activeTradesCount: Int = 0,
    val assets: List<AssetDto> = emptyList()
)

data class TradeItemDto(
    val id: String = "",
    val symbol: String = "BTC/USDT",
    val type: String = "SPOT",
    val side: String = "BUY",
    val amountUsd: Double = 1.0,
    val entryPrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val pnl: Double = 0.0,
    val pnlPercent: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "FILLED",
    val strategy: String? = null
)

data class TradesResponse(
    val serverTimeMillis: Long = 0L,
    val trades: List<TradeItemDto> = emptyList()
)

data class TradeRequest(
    val type: String,
    val symbol: String,
    val side: String,
    val amountUsd: Double = 1.0
)

data class TradeExecutionResponse(
    val success: Boolean = false,
    val message: String = "",
    val order: TradeItemDto? = null,
    val serverTimeMillis: Long = 0L,
    val serverTimeIso: String = "",
    val activeTradesCount: Int? = null,
    val profitValue: Double? = null,
    val lossValue: Double? = null
)

interface MaroahApiService {
    @GET("/")
    suspend fun getCloudStatus(): CloudServerStatusDto

    @GET("api/health")
    suspend fun checkHealth(): HealthResponse

    @GET("api/balance/spot")
    suspend fun getSpotBalance(): BalanceResponse

    @GET("api/balance/futures")
    suspend fun getFuturesBalance(): BalanceResponse

    @GET("api/trades")
    suspend fun getTrades(@Query("type") type: String? = null): TradesResponse

    @POST("api/trade/spot")
    suspend fun executeSpotTrade(@Body request: TradeRequest): TradeExecutionResponse

    @POST("api/trade/futures")
    suspend fun executeFuturesTrade(@Body request: TradeRequest): TradeExecutionResponse

    @POST("api/trade")
    suspend fun executeTrade(@Body request: TradeRequest): TradeExecutionResponse
}

object NetworkClient {
    const val DEFAULT_RAILWAY_URL = "https://maroah-production-33c3.up.railway.app/"
    const val DEFAULT_SESSION_TOKEN = "msIECkh7qAZXR5BfSpvTTCopXpvgDsOSyCyHMUKR0KA="

    private var currentSessionToken = DEFAULT_SESSION_TOKEN

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("x-session-token", currentSessionToken)
                .header("Authorization", "Bearer $currentSessionToken")
                .header("Accept", "application/json")
            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private var currentBaseUrl = DEFAULT_RAILWAY_URL
    private var currentApi: MaroahApiService? = null

    @Synchronized
    fun getApiService(
        baseUrl: String = DEFAULT_RAILWAY_URL,
        sessionToken: String = currentSessionToken
    ): MaroahApiService {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (currentApi == null || currentBaseUrl != normalizedUrl || currentSessionToken != sessionToken) {
            currentBaseUrl = normalizedUrl
            currentSessionToken = sessionToken
            currentApi = Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(MaroahApiService::class.java)
        }
        return currentApi!!
    }
}
