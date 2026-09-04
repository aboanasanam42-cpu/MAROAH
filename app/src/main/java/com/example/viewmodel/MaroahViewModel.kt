package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CloudConfig
import com.example.model.ColumnSummary
import com.example.model.OrderSide
import com.example.model.TradeOrder
import com.example.model.TradeType
import com.example.model.WalletAsset
import com.example.network.NetworkClient
import com.example.network.TradeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

sealed interface ActiveModal {
    data object None : ActiveModal
    data class WalletDetails(val type: TradeType) : ActiveModal
    data class TradesList(val type: TradeType) : ActiveModal
    data class PlaceTradeDialog(val type: TradeType) : ActiveModal
    data object CloudSettings : ActiveModal
}

data class MaroahUiState(
    val spotSummary: ColumnSummary = ColumnSummary(
        balanceUsdt = 0.0, profitValue = 0.0, lossValue = 0.0, activeTradesCount = 0,
        assets = emptyList(), trades = emptyList()
    ),
    val futureSummary: ColumnSummary = ColumnSummary(
        balanceUsdt = 0.0, profitValue = 0.0, lossValue = 0.0, activeTradesCount = 0,
        assets = emptyList(), trades = emptyList()
    ),
    val activeModal: ActiveModal = ActiveModal.None,
    val isSyncing: Boolean = false,
    val isCloudConnected: Boolean = false,
    val cloudStatus: String = "جاري الاتصال بالسحابة...",
    val cloudConfig: CloudConfig = CloudConfig(
        serverUrl = NetworkClient.DEFAULT_RAILWAY_URL,
        fallbackUrl = "https://maroah.vercel.app",
        sessionToken = "",
        isLiveCloudMode = true
    ),
    val activeServerEndpoint: String = "Railway Primary",
    val statusNotification: String? = null,
    val unifiedUtcTime: String = "",
    val currentBtcPrice: Double = 0.0,
    val high24h: Double = 0.0,
    val low24h: Double = 0.0,
    val quantStrategyName: String = "EMA 9/21 + RSI 14"
)

class MaroahViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MaroahUiState())
    val uiState: StateFlow<MaroahUiState> = _uiState.asStateFlow()

    private val utcFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    init {
        startUnifiedClock()
        startContinuousCloudTelemetry()
    }

    private fun startUnifiedClock() {
        viewModelScope.launch {
            while (isActive) {
                _uiState.update { it.copy(unifiedUtcTime = utcFormatter.format(Date())) }
                delay(1000)
            }
        }
    }

    private fun startContinuousCloudTelemetry() {
        viewModelScope.launch {
            fetchCloudTelemetry(true)
            while (isActive) {
                delay(3500)
                fetchCloudTelemetry(false)
            }
        }
    }

    private suspend fun fetchCloudTelemetry(showLoadingIndicator: Boolean) {
        if (showLoadingIndicator) _uiState.update { it.copy(isSyncing = true) }
        try {
            val config = _uiState.value.cloudConfig
            withContext(Dispatchers.IO) {
                var usedUrl = config.serverUrl
                var api = NetworkClient.getApiService(usedUrl, config.sessionToken)
                var botStatusResp = try { api.getBotStatus() } catch (_: Exception) { null }
                if (botStatusResp == null && config.fallbackUrl.isNotBlank() && config.fallbackUrl != config.serverUrl) {
                    try {
                        usedUrl = config.fallbackUrl
                        api = NetworkClient.getApiService(usedUrl, config.sessionToken)
                        botStatusResp = api.getBotStatus()
                    } catch (_: Exception) { null }
                }
                val spotBalance = try { api.getSpotBalance() } catch (_: Exception) { null }
                val futuresBalance = try { api.getFuturesBalance() } catch (_: Exception) { null }
                val tradesResp = try { api.getTrades() } catch (_: Exception) { null }

                withContext(Dispatchers.Main) {
                    _uiState.update { current ->
                        val botData = botStatusResp?.data
                        val btcPrice = botData?.btcPrice?.takeIf { it > 0.0 }
                            ?: current.currentBtcPrice
                        val spotBalanceVal = botData?.spot?.balance
                            ?: spotBalance?.balanceUsdt ?: current.spotSummary.balanceUsdt
                        val futuresBalanceVal = botData?.future?.balance
                            ?: futuresBalance?.balanceUsdt ?: current.futureSummary.balanceUsdt
                        val spotAssets = spotBalance?.assets?.map {
                            WalletAsset(it.coin, it.freeAmount, it.lockedAmount, it.usdtValue)
                        } ?: current.spotSummary.assets
                        val futuresAssets = futuresBalance?.assets?.map {
                            WalletAsset(it.coin, it.freeAmount, it.lockedAmount, it.usdtValue)
                        } ?: current.futureSummary.assets
                        val allTrades: List<TradeOrder> = tradesResp?.trades?.map { dto ->
                            TradeOrder(
                                id = dto.id, symbol = dto.symbol,
                                type = if (dto.type == "SPOT") TradeType.SPOT else TradeType.FUTURE,
                                side = if (dto.side == "BUY") OrderSide.BUY else OrderSide.SELL,
                                amountUsd = dto.amountUsd, entryPrice = dto.entryPrice,
                                currentPrice = dto.currentPrice.takeIf { it > 0 } ?: btcPrice,
                                pnl = dto.pnl, pnlPercent = dto.pnlPercent, timestamp = dto.timestamp,
                                status = dto.status, strategy = dto.strategy ?: "EMA 9/21 + RSI 14"
                            )
                        } ?: emptyList()
                        val spotTrades = allTrades.filter { it.type == TradeType.SPOT }
                        val futuresTrades = allTrades.filter { it.type == TradeType.FUTURE }
                        val connected = botStatusResp != null || spotBalance != null || futuresBalance != null
                        current.copy(
                            isSyncing = false,
                            isCloudConnected = connected,
                            activeServerEndpoint = if (usedUrl.contains("vercel", true)) "Vercel Cloud Fallback" else "Railway Cloud Primary",
                            cloudStatus = if (connected) "السحابة متصلة • MEXC Live Telemetry" else "جاري إعادة الاتصال بالسحابة...",
                            currentBtcPrice = btcPrice,
                            spotSummary = current.spotSummary.copy(
                                balanceUsdt = spotBalanceVal,
                                profitValue = botData?.spot?.profit ?: spotBalance?.profitValue ?: 0.0,
                                lossValue = botData?.spot?.loss ?: spotBalance?.lossValue ?: 0.0,
                                activeTradesCount = spotTrades.size, assets = spotAssets, trades = spotTrades
                            ),
                            futureSummary = current.futureSummary.copy(
                                balanceUsdt = futuresBalanceVal,
                                profitValue = botData?.future?.profit ?: futuresBalance?.profitValue ?: 0.0,
                                lossValue = botData?.future?.loss ?: futuresBalance?.lossValue ?: 0.0,
                                activeTradesCount = futuresTrades.size, assets = futuresAssets, trades = futuresTrades
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(isSyncing = false, isCloudConnected = false, cloudStatus = "جاري إعادة الاتصال بالسحابة...") }
        }
    }

    fun openModal(modal: ActiveModal) { _uiState.update { it.copy(activeModal = modal) } }
    fun closeModal() { _uiState.update { it.copy(activeModal = ActiveModal.None, statusNotification = null) } }

    fun refreshCloudData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, statusNotification = "جاري تحديث بيانات MEXC...") }
            fetchCloudTelemetry(true)
            delay(1500)
            _uiState.update { it.copy(statusNotification = "تم تحديث البيانات من السحابة") }
            delay(2000)
            _uiState.update { it.copy(statusNotification = null) }
        }
    }

    fun placeOneDollarTrade(type: TradeType, symbol: String, side: OrderSide) {
        val requestedSymbol = if (type == TradeType.SPOT) "BTC/USDT" else "BTC-PERP"
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, statusNotification = "إرسال طلب التداول إلى السحابة...") }
            try {
                val config = _uiState.value.cloudConfig
                val request = TradeRequest(
                    type = if (type == TradeType.SPOT) "SPOT" else "FUTURE",
                    symbol = requestedSymbol, side = side.name, amountUsd = 1.0,
                    timestamp = System.currentTimeMillis()
                )
                val response = withContext(Dispatchers.IO) {
                    val api = NetworkClient.getApiService(config.serverUrl, config.sessionToken)
                    api.placeOrder(request)
                }
                _uiState.update { it.copy(isSyncing = false, statusNotification = response.message.ifEmpty { "تمت معالجة الطلب" }) }
                fetchCloudTelemetry(false)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSyncing = false, statusNotification = "تعذر معالجة طلب التداول: ${e.message ?: "خطأ غير معروف"}") }
            }
            delay(2500)
            _uiState.update { it.copy(statusNotification = null) }
        }
    }

    fun updateCloudConfig(config: CloudConfig) {
        _uiState.update { it.copy(cloudConfig = config, cloudStatus = "تم تحديث إعدادات السحابة") }
        refreshCloudData()
    }
}
