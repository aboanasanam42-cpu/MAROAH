package com.example.network

import com.example.model.OrderSide
import com.example.model.TradeType
import com.example.model.WalletAsset
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
    val server: String = ""
)

data class AssetDto(
    val coin: String = "",
    val freeAmount: Double = 0.0,
    val lockedAmount: Double = 0.0,
    val usdtValue: Double = 0.0
)

data class BalanceResponse(
    val source: String? = null,
    val balanceUsdt: Double = 0.0,
    val profitValue: Double = 0.0,
    val lossValue: Double = 0.0,
    val activeTradesCount: Int = 0,
    val assets: List<AssetDto> = emptyList()
)

data class TradeItemDto(
    val id: String = "",
    val symbol: String = "",
    val type: String = "SPOT",
    val side: String = "BUY",
    val amountUsd: Double = 1.0,
    val entryPrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val pnl: Double = 0.0,
    val pnlPercent: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "FILLED"
)

data class TradesResponse(
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
    val order: TradeItemDto? = null
)

interface MaroahApiService {
    @GET("api/health")
    suspend fun checkHealth(): HealthResponse

    @GET("api/balance")
    suspend fun getSpotBalance(): BalanceResponse

    @GET("api/futures/balance")
    suspend fun getFuturesBalance(): BalanceResponse

    @GET("api/trades")
    suspend fun getTrades(@Query("type") type: String? = null): TradesResponse

    @POST("api/trade")
    suspend fun executeTrade(@Body request: TradeRequest): TradeExecutionResponse
}

object NetworkClient {
    private const val DEFAULT_RAILWAY_URL = "https://maroah-production-33c3.up.railway.app/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private var currentBaseUrl = DEFAULT_RAILWAY_URL
    private var currentApi: MaroahApiService? = null

    @Synchronized
    fun getApiService(baseUrl: String = DEFAULT_RAILWAY_URL): MaroahApiService {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (currentApi == null || currentBaseUrl != normalizedUrl) {
            currentBaseUrl = normalizedUrl
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
