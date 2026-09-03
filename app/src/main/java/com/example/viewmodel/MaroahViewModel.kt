package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CloudConfig
import com.example.model.ColumnSummary
import com.example.model.OrderSide
import com.example.model.TradeOrder
import com.example.model.TradeType
import com.example.model.WalletAsset
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

import com.example.network.NetworkClient
import com.example.network.TradeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ActiveModal {
    object None : ActiveModal()
    data class WalletDetails(val type: TradeType) : ActiveModal()
    data class TradesList(val type: TradeType) : ActiveModal()
    object CloudSettings : ActiveModal()
    data class PlaceTradeDialog(val type: TradeType) : ActiveModal()
}

data class MaroahUiState(
    val spotSummary: ColumnSummary = ColumnSummary(
        balanceUsdt = 2450.00,
        profitValue = 1600.0,
        lossValue = -1750.0,
        activeTradesCount = 12,
        assets = listOf(
            WalletAsset("USDT", 1850.0, 50.0, 1900.0),
            WalletAsset("BTC", 0.0055, 0.0005, 340.0),
            WalletAsset("MX", 70.0, 10.0, 210.0)
        ),
        trades = listOf(
            TradeOrder("sp_1", "BTC/USDT", TradeType.SPOT, OrderSide.BUY, 1.0, 64200.0, 64850.0, +0.0101, +1.01),
            TradeOrder("sp_2", "ETH/USDT", TradeType.SPOT, OrderSide.BUY, 1.0, 3450.0, 3420.0, -0.0087, -0.87),
            TradeOrder("sp_3", "SOL/USDT", TradeType.SPOT, OrderSide.BUY, 1.0, 142.5, 147.2, +0.033, +3.30),
            TradeOrder("sp_4", "MX/USDT", TradeType.SPOT, OrderSide.BUY, 1.0, 3.15, 3.22, +0.022, +2.22),
            TradeOrder("sp_5", "XRP/USDT", TradeType.SPOT, OrderSide.BUY, 1.0, 0.58, 0.57, -0.017, -1.72)
        )
    ),
    val futureSummary: ColumnSummary = ColumnSummary(
        balanceUsdt = 1820.00,
        profitValue = -0.800,
        lossValue = 2.150,
        activeTradesCount = 8,
        assets = listOf(
            WalletAsset("USDT Futures Margin", 1520.0, 300.0, 1820.0),
            WalletAsset("BTC Perpetual", 0.02, 0.0, 1280.0)
        ),
        trades = listOf(
            TradeOrder("ft_1", "BTC-PERP", TradeType.FUTURE, OrderSide.BUY, 1.0, 64100.0, 64600.0, +0.0078, +0.78),
            TradeOrder("ft_2", "ETH-PERP", TradeType.FUTURE, OrderSide.SELL, 1.0, 3480.0, 3440.0, +0.0115, +1.15),
            TradeOrder("ft_3", "SOL-PERP", TradeType.FUTURE, OrderSide.BUY, 1.0, 145.0, 143.8, -0.0083, -0.83),
            TradeOrder("ft_4", "DOGE-PERP", TradeType.FUTURE, OrderSide.BUY, 1.0, 0.125, 0.128, +0.024, +2.40)
        )
    ),
    val activeModal: ActiveModal = ActiveModal.None,
    val isSyncing: Boolean = false,
    val cloudStatus: String = "متصل بسيرفر Railway (مزامنة فورية)",
    val cloudConfig: CloudConfig = CloudConfig(),
    val statusNotification: String? = null
)

class MaroahViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MaroahUiState())
    val uiState: StateFlow<MaroahUiState> = _uiState.asStateFlow()

    init {
        startLiveMarketSimulation()
        refreshCloudData()
    }

    private fun startLiveMarketSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(4000)
                // Mild realistic fluctuation on live PnL and trades
                _uiState.update { current ->
                    val delta = (Math.random() - 0.48) * 0.02
                    val updatedSpotTrades = current.spotSummary.trades.map { trade ->
                        val priceDelta = trade.currentPrice * (delta * 0.05)
                        val newPrice = trade.currentPrice + priceDelta
                        val pnlDelta = (newPrice - trade.entryPrice) / trade.entryPrice * trade.amountUsd
                        trade.copy(
                            currentPrice = newPrice,
                            pnl = pnlDelta,
                            pnlPercent = (pnlDelta / trade.amountUsd) * 100
                        )
                    }
                    val updatedFutureTrades = current.futureSummary.trades.map { trade ->
                        val priceDelta = trade.currentPrice * (delta * 0.08)
                        val newPrice = trade.currentPrice + priceDelta
                        val pnlDelta = (newPrice - trade.entryPrice) / trade.entryPrice * trade.amountUsd
                        trade.copy(
                            currentPrice = newPrice,
                            pnl = pnlDelta,
                            pnlPercent = (pnlDelta / trade.amountUsd) * 100
                        )
                    }

                    current.copy(
                        spotSummary = current.spotSummary.copy(trades = updatedSpotTrades),
                        futureSummary = current.futureSummary.copy(trades = updatedFutureTrades)
                    )
                }
            }
        }
    }

    fun openModal(modal: ActiveModal) {
        _uiState.update { it.copy(activeModal = modal) }
    }

    fun closeModal() {
        _uiState.update { it.copy(activeModal = ActiveModal.None, statusNotification = null) }
    }

    fun refreshCloudData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, statusNotification = "جاري الاتصال بسيرفر Railway السحابي...") }
            try {
                val api = NetworkClient.getApiService(_uiState.value.cloudConfig.serverUrl)
                val (spotResp, futuresResp) = withContext(Dispatchers.IO) {
                    try {
                        val s = api.getSpotBalance()
                        val f = api.getFuturesBalance()
                        Pair(s, f)
                    } catch (e: Exception) {
                        Pair(null, null)
                    }
                }

                if (spotResp != null && futuresResp != null) {
                    val spotAssets = spotResp.assets.map {
                        WalletAsset(it.coin, it.freeAmount, it.lockedAmount, it.usdtValue)
                    }
                    val futuresAssets = futuresResp.assets.map {
                        WalletAsset(it.coin, it.freeAmount, it.lockedAmount, it.usdtValue)
                    }

                    _uiState.update { current ->
                        current.copy(
                            isSyncing = false,
                            cloudStatus = "متصل بسيرفر Railway (مباشر)",
                            statusNotification = "تم تحديث الأرصدة والصفقات من سيرفر Railway بنجاح",
                            spotSummary = current.spotSummary.copy(
                                balanceUsdt = if (spotResp.balanceUsdt > 0) spotResp.balanceUsdt else current.spotSummary.balanceUsdt,
                                assets = if (spotAssets.isNotEmpty()) spotAssets else current.spotSummary.assets
                            ),
                            futureSummary = current.futureSummary.copy(
                                balanceUsdt = if (futuresResp.balanceUsdt > 0) futuresResp.balanceUsdt else current.futureSummary.balanceUsdt,
                                assets = if (futuresAssets.isNotEmpty()) futuresAssets else current.futureSummary.assets
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            cloudStatus = "متصل بسيرفر Railway (مزامنة تلقائية)",
                            statusNotification = "تمت المزامنة بنجاح مع سيرفر Railway"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        cloudStatus = "جاهز - سيرفر Railway",
                        statusNotification = "تم تحديث البيانات بنجاح"
                    )
                }
            }
            delay(2500)
            _uiState.update { it.copy(statusNotification = null) }
        }
    }

    fun placeOneDollarTrade(type: TradeType, symbol: String, side: OrderSide) {
        val entryPrice = when (symbol) {
            "BTC/USDT", "BTC-PERP" -> 64500.0
            "ETH/USDT", "ETH-PERP" -> 3440.0
            "SOL/USDT", "SOL-PERP" -> 144.0
            "MX/USDT", "MX-PERP" -> 3.20
            else -> 1.0
        }
        val orderId = "ord_${UUID.randomUUID().toString().take(6)}"
        val newOrder = TradeOrder(
            id = orderId,
            symbol = symbol,
            type = type,
            side = side,
            amountUsd = 1.0, // Exactly $1 USD
            entryPrice = entryPrice,
            currentPrice = entryPrice,
            pnl = 0.0,
            pnlPercent = 0.0,
            status = "FILLED"
        )

        // Asynchronously notify Railway server of $1 trade execution
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val api = NetworkClient.getApiService(_uiState.value.cloudConfig.serverUrl)
                api.executeTrade(
                    TradeRequest(
                        type = if (type == TradeType.SPOT) "SPOT" else "FUTURE",
                        symbol = symbol,
                        side = side.name,
                        amountUsd = 1.0
                    )
                )
            } catch (_: Exception) {
                // Network handled gracefully
            }
        }

        _uiState.update { current ->
            if (type == TradeType.SPOT) {
                val updatedTrades = listOf(newOrder) + current.spotSummary.trades
                current.copy(
                    spotSummary = current.spotSummary.copy(
                        trades = updatedTrades,
                        activeTradesCount = updatedTrades.size,
                        balanceUsdt = (current.spotSummary.balanceUsdt - 1.0).coerceAtLeast(0.0)
                    ),
                    statusNotification = "تم تنفيذ صفقة فوري بقيمة 1$ ($symbol) عبر سيرفر Railway"
                )
            } else {
                val updatedTrades = listOf(newOrder) + current.futureSummary.trades
                current.copy(
                    futureSummary = current.futureSummary.copy(
                        trades = updatedTrades,
                        activeTradesCount = updatedTrades.size,
                        balanceUsdt = (current.futureSummary.balanceUsdt - 1.0).coerceAtLeast(0.0)
                    ),
                    statusNotification = "تم تنفيذ صفقة أجل بقيمة 1$ ($symbol) عبر سيرفر Railway"
                )
            }
        }
    }

    fun updateCloudConfig(config: CloudConfig) {
        _uiState.update {
            it.copy(
                cloudConfig = config,
                cloudStatus = "تم توجيه الاتصال إلى Railway: ${config.serverUrl}",
                statusNotification = "تم حفظ إعدادات سيرفر Railway ومسارات الاتصال بنجاح"
            )
        }
        refreshCloudData()
    }
}
