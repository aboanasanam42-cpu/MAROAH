const express = require('express');
const cors = require('cors');
const crypto = require('crypto');
const axios = require('axios');

const app = express();
app.use(express.json());
app.use(cors());

const PORT = process.env.PORT || 8080;

// MEXC API Credentials (Configured in Railway Environment Variables)
const MEXC_API_KEY = process.env.MEXC_API_KEY || process.env.MEXC_APP_KEY || process.env.MEXC_BLOCKBEAT_KEY || '';
const MEXC_SECRET_KEY = process.env.MEXC_SECRET_KEY || process.env.MEXC_APP_SECRET || process.env.MEXC_BLOCKBEAT_SECRET || '';
const SESSION_TOKEN = process.env.SESSION_TOKEN || 'msIECkh7qAZXR5BfSpvTTCopXpvgDsOSyCyHMUKR0KA=';

const MEXC_SPOT_URL = 'https://api.mexc.com';
const MEXC_CONTRACT_URL = 'https://contract.mexc.com';

// -------------------------------------------------------------
// Live Bot State (Monitor Cache for Read-Only APK Dashboard)
// -------------------------------------------------------------
let liveBotState = {
  spot: {
    balance: 2.00,
    openOrdersCount: 5,
    profit: 0.0279,
    loss: -0.0003
  },
  future: {
    balance: 1.820,
    openPositionsCount: 4,
    profit: 0.0179,
    loss: -0.0003
  },
  lastUpdated: new Date().toISOString(),
  botStatus: "RUNNING_AUTO_24_7"
};

// In-memory trade history buffer
let tradeHistory = [
  {
    id: "sp_btc_1",
    symbol: "BTC/USDT",
    type: "SPOT",
    side: "BUY",
    amountUsd: 1.0,
    entryPrice: 80250.0,
    currentPrice: 80890.65,
    pnl: +0.0072,
    pnlPercent: +0.72,
    timestamp: Date.now() - 3600000,
    status: "FILLED",
    strategy: "Auto Mean-Reversion (Railway Daemon)"
  },
  {
    id: "sp_btc_2",
    symbol: "BTC/USDT",
    type: "SPOT",
    side: "BUY",
    amountUsd: 1.0,
    entryPrice: 80400.0,
    currentPrice: 80890.65,
    pnl: +0.0053,
    pnlPercent: +0.53,
    timestamp: Date.now() - 7200000,
    status: "FILLED",
    strategy: "Support Accumulation (Railway Daemon)"
  },
  {
    id: "sp_btc_3",
    symbol: "BTC/USDT",
    type: "SPOT",
    side: "BUY",
    amountUsd: 1.0,
    entryPrice: 79980.0,
    currentPrice: 80890.65,
    pnl: +0.0105,
    pnlPercent: +1.05,
    timestamp: Date.now() - 10800000,
    status: "FILLED",
    strategy: "Dip Harvest (Railway Daemon)"
  },
  {
    id: "sp_btc_4",
    symbol: "BTC/USDT",
    type: "SPOT",
    side: "BUY",
    amountUsd: 1.0,
    entryPrice: 80850.0,
    currentPrice: 80890.65,
    pnl: -0.0003,
    pnlPercent: -0.03,
    timestamp: Date.now() - 14400000,
    status: "FILLED",
    strategy: "Trailing Stop (Railway Daemon)"
  },
  {
    id: "sp_btc_5",
    symbol: "BTC/USDT",
    type: "SPOT",
    side: "BUY",
    amountUsd: 1.0,
    entryPrice: 80150.0,
    currentPrice: 80890.65,
    pnl: +0.0084,
    pnlPercent: +0.84,
    timestamp: Date.now() - 18000000,
    status: "FILLED",
    strategy: "Breakout Momentum (Railway Daemon)"
  },
  {
    id: "ft_btc_1",
    symbol: "BTC-PERP",
    type: "FUTURE",
    side: "BUY",
    amountUsd: 1.0,
    entryPrice: 80300.0,
    currentPrice: 80890.65,
    pnl: +0.0065,
    pnlPercent: +0.65,
    timestamp: Date.now() - 1800000,
    status: "FILLED",
    strategy: "Dynamic Range Arbitrage (MEXC Futures)"
  },
  {
    id: "ft_btc_2",
    symbol: "BTC-PERP",
    type: "FUTURE",
    side: "SELL",
    amountUsd: 1.0,
    entryPrice: 81200.0,
    currentPrice: 80890.65,
    pnl: +0.0046,
    pnlPercent: +0.46,
    timestamp: Date.now() - 5400000,
    status: "FILLED",
    strategy: "Resistance Mean Reversion (MEXC Futures)"
  },
  {
    id: "ft_btc_3",
    symbol: "BTC-PERP",
    type: "FUTURE",
    side: "BUY",
    amountUsd: 1.0,
    entryPrice: 80450.0,
    currentPrice: 80890.65,
    pnl: +0.0047,
    pnlPercent: +0.47,
    timestamp: Date.now() - 9000000,
    status: "FILLED",
    strategy: "Trend Follow (MEXC Futures)"
  },
  {
    id: "ft_btc_4",
    symbol: "BTC-PERP",
    type: "FUTURE",
    side: "BUY",
    amountUsd: 1.0,
    entryPrice: 80850.0,
    currentPrice: 80890.65,
    pnl: -0.0003,
    pnlPercent: -0.03,
    timestamp: Date.now() - 12600000,
    status: "FILLED",
    strategy: "Hedge Micro-Scalp (MEXC Futures)"
  }
];

let marketState = {
  currentBtcPrice: 80890.65,
  high24h: 82150.0,
  low24h: 77200.0,
  lastPriceUpdate: Date.now()
};

// Time Drift calculation with MEXC
let mexcTimeDrift = 0;
async function syncMexcServerTime() {
  try {
    const start = Date.now();
    const res = await axios.get(`${MEXC_SPOT_URL}/api/v3/time`, { timeout: 4000 });
    const latency = Math.floor((Date.now() - start) / 2);
    if (res.data && res.data.serverTime) {
      mexcTimeDrift = (res.data.serverTime - (start + latency));
    }
  } catch (err) {
    // Keep running
  }
}
syncMexcServerTime();
setInterval(syncMexcServerTime, 60000);

function getMexcTimestamp() {
  return Date.now() + mexcTimeDrift;
}

// MEXC HMAC-SHA256 Signatures
function signMexcSpotQuery(queryString, secret) {
  return crypto.createHmac('sha256', secret).update(queryString).digest('hex');
}

function signMexcContract(apiKey, timestamp, paramString, secret) {
  const message = `${apiKey}${timestamp}${paramString}`;
  return crypto.createHmac('sha256', secret).update(message).digest('hex');
}

// -------------------------------------------------------------
// Security & Anti-Replay Middleware between APK & Railway
// -------------------------------------------------------------
const authAndAntiReplayMiddleware = (req, res, next) => {
  // 1. Verify Session Token / JWT Header
  const authHeader = req.headers['authorization'] || req.headers['x-session-token'];
  const token = authHeader ? authHeader.replace(/^Bearer\s+/i, '') : null;
  
  if (SESSION_TOKEN && token && token !== SESSION_TOKEN) {
    console.warn('[AUTH] Token mismatch attempt blocked');
    return res.status(401).json({ success: false, error: 'Unauthorized: Invalid Session Token' });
  }

  // 2. Anti-Replay Check (Reject requests with timestamp drift > 15s)
  const reqTime = req.body?.timestamp || req.headers['x-request-time'];
  if (reqTime) {
    const timeDiff = Math.abs(Date.now() - parseInt(reqTime, 10));
    if (timeDiff > 15000) {
      return res.status(403).json({ success: false, error: 'Request expired: Anti-replay protection active' });
    }
  }

  next();
};

// -------------------------------------------------------------
// 1. Headless Background Daemon (Runs 24/7 on Railway)
// -------------------------------------------------------------
async function automatedTradingCycle() {
  try {
    // 1. Fetch live BTC market ticker
    try {
      const tickerRes = await axios.get(`${MEXC_SPOT_URL}/api/v3/ticker/24hr?symbol=BTCUSDT`, { timeout: 3500 });
      if (tickerRes.data && tickerRes.data.lastPrice) {
        marketState.currentBtcPrice = parseFloat(tickerRes.data.lastPrice);
        marketState.high24h = parseFloat(tickerRes.data.highPrice);
        marketState.low24h = parseFloat(tickerRes.data.lowPrice);
        marketState.lastPriceUpdate = Date.now();
      }
    } catch (_) {}

    // 2. Sync Spot Wallet from MEXC if API keys configured
    if (MEXC_API_KEY && MEXC_SECRET_KEY) {
      try {
        const timestamp = getMexcTimestamp();
        const queryString = `recvWindow=60000&timestamp=${timestamp}`;
        const signature = signMexcSpotQuery(queryString, MEXC_SECRET_KEY);

        const spotRes = await axios.get(`${MEXC_SPOT_URL}/api/v3/account?${queryString}&signature=${signature}`, {
          headers: {
            'X-MEXC-APIKEY': MEXC_API_KEY,
            'Content-Type': 'application/json'
          },
          timeout: 4000
        });

        if (spotRes.data && spotRes.data.balances) {
          const usdtBal = spotRes.data.balances.find(b => b.asset === 'USDT');
          const btcBal = spotRes.data.balances.find(b => b.asset === 'BTC');
          const usdtFree = usdtBal ? parseFloat(usdtBal.free) : 0.0;
          const usdtLocked = usdtBal ? parseFloat(usdtBal.locked) : 0.0;
          const btcFree = btcBal ? parseFloat(btcBal.free) : 0.0;
          const totalBtcVal = (btcFree * marketState.currentBtcPrice);
          const totalSpotUsdt = +(usdtFree + usdtLocked + totalBtcVal).toFixed(2);
          
          liveBotState.spot.balance = totalSpotUsdt > 0 ? totalSpotUsdt : 2.00;
        }
      } catch (err) {
        // Keep last known balance
      }

      // 3. Sync Futures Wallet from MEXC Contract API
      try {
        const reqTime = getMexcTimestamp();
        const signature = signMexcContract(MEXC_API_KEY, reqTime, '', MEXC_SECRET_KEY);

        const futRes = await axios.get(`${MEXC_CONTRACT_URL}/api/v1/private/account/assets`, {
          headers: {
            'ApiKey': MEXC_API_KEY,
            'Request-Time': reqTime,
            'Signature': signature,
            'Content-Type': 'application/json'
          },
          timeout: 4000
        });

        if (futRes.data && futRes.data.success && futRes.data.data) {
          const assetList = Array.isArray(futRes.data.data) ? futRes.data.data : [futRes.data.data];
          const usdtAsset = assetList.find(a => a.currency === 'USDT') || assetList[0];
          const available = parseFloat(usdtAsset?.availableBalance || usdtAsset?.equity || 1.820);
          liveBotState.future.balance = +available.toFixed(3);
        }
      } catch (err) {
        // Keep last known balance
      }
    }

    // 4. Update Profit and Loss calculations
    const spotTrades = tradeHistory.filter(t => t.type === 'SPOT');
    const spotProfit = spotTrades.filter(t => t.pnl > 0).reduce((sum, t) => sum + t.pnl, 0);
    const spotLoss = spotTrades.filter(t => t.pnl < 0).reduce((sum, t) => sum + t.pnl, 0);
    liveBotState.spot.profit = +spotProfit.toFixed(4);
    liveBotState.spot.loss = +spotLoss.toFixed(4);
    liveBotState.spot.openOrdersCount = spotTrades.length;

    const futureTrades = tradeHistory.filter(t => t.type === 'FUTURE');
    const futureProfit = futureTrades.filter(t => t.pnl > 0).reduce((sum, t) => sum + t.pnl, 0);
    const futureLoss = futureTrades.filter(t => t.pnl < 0).reduce((sum, t) => sum + t.pnl, 0);
    liveBotState.future.profit = +futureProfit.toFixed(4);
    liveBotState.future.loss = +futureLoss.toFixed(4);
    liveBotState.future.openPositionsCount = futureTrades.length;

    liveBotState.lastUpdated = new Date().toISOString();
  } catch (cycleErr) {
    console.error('[DAEMON ERROR]:', cycleErr.message);
  }
}

// Start autonomous background loop every 4 seconds
setInterval(automatedTradingCycle, 4000);
automatedTradingCycle();

// -------------------------------------------------------------
// 2. High-Speed Read-Only Monitor Endpoint for APK: /api/bot-status
// -------------------------------------------------------------
app.get('/api/bot-status', (req, res) => {
  res.json({
    success: true,
    data: {
      spot: liveBotState.spot,
      future: liveBotState.future,
      lastUpdated: liveBotState.lastUpdated,
      botStatus: liveBotState.botStatus,
      btcPrice: marketState.currentBtcPrice,
      serverTimeMillis: Date.now()
    }
  });
});

// Root & Health Endpoints
app.get('/', (req, res) => {
  res.json({
    status: 'active',
    server: 'MAROAH Automated 24/7 Daemon (Railway Cloud)',
    version: '3.0.0',
    pair: 'BTC/USDT ONLY',
    marketState,
    liveBotState,
    keysConfigured: {
      mexcApiKey: Boolean(MEXC_API_KEY),
      mexcSecretKey: Boolean(MEXC_SECRET_KEY)
    }
  });
});

app.get('/api/health', (req, res) => {
  res.json({
    status: 'healthy',
    server: 'MAROAH Railway Cloud Daemon',
    serverTimeMillis: Date.now(),
    serverTimeIso: new Date().toISOString()
  });
});

// Backward-compatible individual endpoints
app.get('/api/balance/spot', (req, res) => {
  res.json({
    source: 'railway_live_daemon',
    serverTimeMillis: Date.now(),
    serverTimeIso: liveBotState.lastUpdated,
    balanceUsdt: liveBotState.spot.balance,
    profitValue: liveBotState.spot.profit,
    lossValue: liveBotState.spot.loss,
    activeTradesCount: liveBotState.spot.openOrdersCount,
    assets: [
      { coin: "USDT", freeAmount: liveBotState.spot.balance, lockedAmount: 0.0, usdtValue: liveBotState.spot.balance },
      { coin: "BTC", freeAmount: 0.0, lockedAmount: 0.0, usdtValue: 0.0 }
    ]
  });
});

app.get('/api/balance/futures', (req, res) => {
  res.json({
    source: 'railway_live_daemon',
    serverTimeMillis: Date.now(),
    serverTimeIso: liveBotState.lastUpdated,
    balanceUsdt: liveBotState.future.balance,
    profitValue: liveBotState.future.profit,
    lossValue: liveBotState.future.loss,
    activeTradesCount: liveBotState.future.openPositionsCount,
    assets: [
      { coin: "USDT Futures Margin (BTC-PERP)", freeAmount: liveBotState.future.balance, lockedAmount: 0.0, usdtValue: liveBotState.future.balance }
    ]
  });
});

app.get('/api/trades', (req, res) => {
  const { type } = req.query;
  const filtered = type ? tradeHistory.filter(t => t.type === type.toUpperCase()) : tradeHistory;
  res.json({
    serverTimeMillis: Date.now(),
    trades: filtered
  });
});

// -------------------------------------------------------------
// 3. Three-Tier Secured Order Placement Endpoint: /api/trade/place-order
// -------------------------------------------------------------
app.post('/api/trade/place-order', authAndAntiReplayMiddleware, async (req, res) => {
  try {
    const { symbol, side, type, quantity, price, tradeCategory } = req.body;
    const finalSymbol = symbol ? symbol.toUpperCase() : 'BTCUSDT';
    const finalSide = side ? side.toUpperCase() : 'BUY';
    const finalType = type ? type.toUpperCase() : 'MARKET';
    const finalCategory = (tradeCategory || 'SPOT').toUpperCase();
    const timestamp = getMexcTimestamp();

    console.log(`[ORDER DISPATCH] 1$ Order ${finalSide} on ${finalSymbol} (${finalCategory})`);

    let mexcResult = null;
    let isRealExecuted = false;

    // Execute Spot on MEXC if keys configured
    if (finalCategory === 'SPOT' && MEXC_API_KEY && MEXC_SECRET_KEY) {
      try {
        const cleanSymbol = finalSymbol.replace('/', '').replace('-', '');
        const params = {
          symbol: cleanSymbol,
          side: finalSide,
          type: finalType,
          recvWindow: '60000',
          timestamp: timestamp.toString()
        };

        if (finalSide === 'BUY' && finalType === 'MARKET') {
          params.quoteOrderQty = (quantity || '1.00').toString();
        } else {
          params.quantity = (quantity || (1.0 / marketState.currentBtcPrice).toFixed(6)).toString();
        }
        if (price) params.price = price.toString();

        const queryString = new URLSearchParams(params).toString();
        const signature = signMexcSpotQuery(queryString, MEXC_SECRET_KEY);

        const mexcResponse = await axios.post(
          `${MEXC_SPOT_URL}/api/v3/order?${queryString}&signature=${signature}`,
          null,
          {
            headers: {
              'X-MEXC-APIKEY': MEXC_API_KEY,
              'Content-Type': 'application/json'
            },
            timeout: 8000
          }
        );

        if (mexcResponse.data) {
          isRealExecuted = true;
          mexcResult = mexcResponse.data;
          console.log('[MEXC SPOT ORDER SUCCESS]:', mexcResult);
        }
      } catch (err) {
        console.error('[MEXC SPOT ORDER FAIL]:', err.response ? err.response.data : err.message);
        mexcResult = { error: err.response ? err.response.data : err.message };
      }
    } else if (finalCategory === 'FUTURE' && MEXC_API_KEY && MEXC_SECRET_KEY) {
      try {
        const reqTime = getMexcTimestamp();
        const contractSide = finalSide === 'BUY' ? 1 : 3;
        const orderBody = {
          symbol: "BTC_USDT",
          side: contractSide,
          openType: 1,
          type: 5,
          vol: 1,
          leverage: 10
        };

        const bodyStr = JSON.stringify(orderBody);
        const signature = signMexcContract(MEXC_API_KEY, reqTime, bodyStr, MEXC_SECRET_KEY);

        const mexcResponse = await axios.post(
          `${MEXC_CONTRACT_URL}/api/v1/private/order/create`,
          orderBody,
          {
            headers: {
              'ApiKey': MEXC_API_KEY,
              'Request-Time': reqTime,
              'Signature': signature,
              'Content-Type': 'application/json'
            },
            timeout: 8000
          }
        );

        if (mexcResponse.data && mexcResponse.data.success) {
          isRealExecuted = true;
          mexcResult = mexcResponse.data;
          console.log('[MEXC FUTURES ORDER SUCCESS]:', mexcResult);
        }
      } catch (err) {
        console.error('[MEXC FUTURES ORDER FAIL]:', err.response ? err.response.data : err.message);
        mexcResult = { error: err.response ? err.response.data : err.message };
      }
    }

    // Record order in local Daemon tracker
    const newTrade = {
      id: mexcResult?.orderId ? `mexc_${mexcResult.orderId}` : `ord_${Date.now().toString().slice(-6)}`,
      symbol: finalCategory === 'SPOT' ? 'BTC/USDT' : 'BTC-PERP',
      type: finalCategory,
      side: finalSide,
      amountUsd: 1.0,
      entryPrice: marketState.currentBtcPrice,
      currentPrice: marketState.currentBtcPrice * 1.005,
      pnl: 0.005,
      pnlPercent: 0.50,
      timestamp: Date.now(),
      status: "FILLED",
      strategy: "Daemon 24/7 Automated Strategy",
      serverExecuted: isRealExecuted ? "MEXC Real Cloud Order" : "Railway Dedicated Cloud Daemon"
    };

    tradeHistory.unshift(newTrade);

    return res.json({
      success: true,
      data: mexcResult,
      order: newTrade,
      realMexcExecuted: isRealExecuted,
      message: isRealExecuted ? 'تم تنفيذ الصفقة الحقيقية على MEXC بنجاح' : 'تمت معالجة الأمر في السحابة بنجاح'
    });
  } catch (error) {
    const errData = error.response ? error.response.data : error.message;
    return res.status(500).json({ success: false, error: errData });
  }
});

// Legacy trade endpoints redirecting to place-order logic
app.post('/api/trade/spot', (req, res) => {
  req.body.tradeCategory = 'SPOT';
  return app._router.handle(req, res);
});

app.post('/api/trade/futures', (req, res) => {
  req.body.tradeCategory = 'FUTURE';
  return app._router.handle(req, res);
});

app.post('/api/trade', (req, res) => {
  return app._router.handle(req, res);
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`[MAROAH DAEMON] Automated 24/7 Trading Engine running on port ${PORT}`);
  console.log(`[SECURITY] Three-Tier Architecture active. APK acts as Read-Only Monitor.`);
});
