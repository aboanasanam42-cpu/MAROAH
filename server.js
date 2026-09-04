const express = require('express');
const cors = require('cors');
const crypto = require('crypto');
const axios = require('axios');

const app = express();
app.use(express.json({ limit: '256kb' }));
app.use(cors());

const PORT = Number(process.env.PORT || 8080);
const MEXC_API_KEY = process.env.MEXC_API_KEY || '';
const MEXC_SECRET_KEY = process.env.MEXC_SECRET_KEY || '';
const BLOCKBYTE_API_KEY = process.env.BLOCKBYTE_API_KEY || '';
const BLOCKBYTE_SECRET_KEY = process.env.BLOCKBYTE_SECRET_KEY || '';
const SESSION_TOKEN = process.env.SESSION_TOKEN || '';

const SPOT_URL = 'https://api.mexc.com';
const CONTRACT_URL = 'https://contract.mexc.com';
const SYMBOL = 'BTCUSDT';
const FUTURES_SYMBOL = 'BTC_USDT';
const INTERVAL = process.env.MAR0AH_INTERVAL || '5m';
const KLINE_LIMIT = 120;
const CACHE_TTL_MS = 4000;

let mexcTimeDrift = 0;
let cache = { expiresAt: 0, snapshot: null };
let lastCycleAt = null;
let lastError = null;

function signSpot(query, secret) {
  return crypto.createHmac('sha256', secret).update(query).digest('hex');
}

function signContract(apiKey, timestamp, params, secret) {
  return crypto.createHmac('sha256', secret).update(`${apiKey}${timestamp}${params}`).digest('hex');
}

function nowMexc() { return Date.now() + mexcTimeDrift; }

async function syncServerTime() {
  const start = Date.now();
  try {
    const { data } = await axios.get(`${SPOT_URL}/api/v3/time`, { timeout: 4000 });
    if (data?.serverTime) mexcTimeDrift = data.serverTime - (start + Math.floor((Date.now() - start) / 2));
  } catch (error) {
    lastError = `MEXC time sync: ${error.message}`;
  }
}

function ema(values, period) {
  if (values.length < period) return null;
  const multiplier = 2 / (period + 1);
  let result = values.slice(0, period).reduce((a, b) => a + b, 0) / period;
  for (let i = period; i < values.length; i++) result = (values[i] - result) * multiplier + result;
  return result;
}

function rsi(values, period = 14) {
  if (values.length <= period) return null;
  let gains = 0, losses = 0;
  for (let i = 1; i <= period; i++) {
    const delta = values[i] - values[i - 1];
    if (delta >= 0) gains += delta; else losses -= delta;
  }
  let avgGain = gains / period;
  let avgLoss = losses / period;
  for (let i = period + 1; i < values.length; i++) {
    const delta = values[i] - values[i - 1];
    const gain = Math.max(delta, 0);
    const loss = Math.max(-delta, 0);
    avgGain = ((avgGain * (period - 1)) + gain) / period;
    avgLoss = ((avgLoss * (period - 1)) + loss) / period;
  }
  if (avgLoss === 0) return 100;
  return 100 - (100 / (1 + avgGain / avgLoss));
}

function buildSignal(closes) {
  const e9 = ema(closes, 9);
  const e21 = ema(closes, 21);
  const r = rsi(closes, 14);
  let signal = 'NEUTRAL';
  let confidence = 0.5;
  if (e9 !== null && e21 !== null && r !== null) {
    if (e9 > e21 && r >= 50 && r < 70) { signal = 'LONG_BIAS'; confidence = Math.min(0.99, 0.55 + Math.abs(e9 - e21) / closes.at(-1) * 20); }
    else if (e9 < e21 && r <= 50 && r > 30) { signal = 'SHORT_BIAS'; confidence = Math.min(0.99, 0.55 + Math.abs(e9 - e21) / closes.at(-1) * 20); }
    else if (r >= 70) { signal = 'OVERBOUGHT'; confidence = 0.7; }
    else if (r <= 30) { signal = 'OVERSOLD'; confidence = 0.7; }
  }
  return { signal, confidence: +confidence.toFixed(4), ema9: e9 && +e9.toFixed(2), ema21: e21 && +e21.toFixed(2), rsi14: r && +r.toFixed(2) };
}

async function fetchMarketSnapshot() {
  const [tickerRes, klineRes] = await Promise.all([
    axios.get(`${SPOT_URL}/api/v3/ticker/24hr`, { params: { symbol: SYMBOL }, timeout: 5000 }),
    axios.get(`${SPOT_URL}/api/v3/klines`, { params: { symbol: SYMBOL, interval: INTERVAL, limit: KLINE_LIMIT }, timeout: 5000 })
  ]);
  const ticker = tickerRes.data;
  const rows = Array.isArray(klineRes.data) ? klineRes.data : [];
  const closes = rows.map(row => Number(row[4])).filter(Number.isFinite);
  const indicators = buildSignal(closes);
  return {
    symbol: SYMBOL,
    futuresSymbol: FUTURES_SYMBOL,
    interval: INTERVAL,
    price: Number(ticker.lastPrice),
    high24h: Number(ticker.highPrice),
    low24h: Number(ticker.lowPrice),
    volume24h: Number(ticker.volume),
    priceChangePercent24h: Number(ticker.priceChangePercent),
    indicators,
    candles: closes.length,
    source: 'mexc-public-api',
    updatedAt: new Date().toISOString()
  };
}

async function fetchSpotAccount() {
  if (!MEXC_API_KEY || !MEXC_SECRET_KEY) return { configured: false, balances: [], totalUsdt: null };
  const timestamp = nowMexc();
  const query = `recvWindow=60000&timestamp=${timestamp}`;
  const signature = signSpot(query, MEXC_SECRET_KEY);
  const { data } = await axios.get(`${SPOT_URL}/api/v3/account?${query}&signature=${signature}`, {
    headers: { 'X-MEXC-APIKEY': MEXC_API_KEY, Accept: 'application/json' }, timeout: 5000
  });
  const balances = (data.balances || []).filter(b => Number(b.free) !== 0 || Number(b.locked) !== 0);
  const usdt = balances.find(b => b.asset === 'USDT');
  return { configured: true, balances, totalUsdt: usdt ? Number(usdt.free) + Number(usdt.locked) : 0 };
}

async function fetchFuturesAccount() {
  if (!MEXC_API_KEY || !MEXC_SECRET_KEY) return { configured: false, assets: [] };
  const timestamp = nowMexc();
  const signature = signContract(MEXC_API_KEY, timestamp, '', MEXC_SECRET_KEY);
  const { data } = await axios.get(`${CONTRACT_URL}/api/v1/private/account/assets`, {
    headers: { ApiKey: MEXC_API_KEY, 'Request-Time': timestamp, Signature: signature, Accept: 'application/json' }, timeout: 5000
  });
  return { configured: true, assets: Array.isArray(data?.data) ? data.data : (data?.data ? [data.data] : []) };
}

async function refresh() {
  if (Date.now() < cache.expiresAt && cache.snapshot) return cache.snapshot;
  try {
    const market = await fetchMarketSnapshot();
    let spot = { configured: false, balances: [], totalUsdt: null };
    let futures = { configured: false, assets: [] };
    if (MEXC_API_KEY && MEXC_SECRET_KEY) {
      [spot, futures] = await Promise.allSettled([fetchSpotAccount(), fetchFuturesAccount()]).then(results => results.map(r => r.status === 'fulfilled' ? r.value : { configured: true, error: r.reason?.message || 'account request failed' }));
    }
    const snapshot = { market, spot, futures, execution: { enabled: false, reason: 'Automated order submission is not implemented in this service.' }, credentials: { mexcConfigured: Boolean(MEXC_API_KEY && MEXC_SECRET_KEY), blockbyteConfigured: Boolean(BLOCKBYTE_API_KEY && BLOCKBYTE_SECRET_KEY) }, updatedAt: new Date().toISOString() };
    cache = { expiresAt: Date.now() + CACHE_TTL_MS, snapshot };
    lastCycleAt = snapshot.updatedAt;
    lastError = null;
    return snapshot;
  } catch (error) {
    lastError = error.message;
    throw error;
  }
}

function requireSession(req, res, next) {
  if (!SESSION_TOKEN) return next();
  const supplied = (req.headers.authorization || req.headers['x-session-token'] || '').replace(/^Bearer\s+/i, '');
  if (!supplied || supplied !== SESSION_TOKEN) return res.status(401).json({ success: false, error: 'Unauthorized' });
  next();
}

app.get('/api/health', (req, res) => res.json({ status: 'healthy', service: 'MAROAH Cloud Telemetry', serverTimeMillis: Date.now(), lastCycleAt, lastError }));
app.get('/', (req, res) => res.json({ service: 'MAROAH Algorithmic Trading Telemetry', version: '4.0.0', pair: 'BTC/USDT', liveData: true, execution: false, endpoints: ['/api/bot-status', '/api/market', '/api/signals', '/api/account', '/api/balance/spot', '/api/balance/futures'] }));

app.get('/api/bot-status', requireSession, async (req, res) => {
  try {
    const snapshot = await refresh();
    res.json({ success: true, data: { spot: { balance: snapshot.spot.totalUsdt, openOrdersCount: null, profit: null, loss: null }, future: { balance: null, openPositionsCount: null, profit: null, loss: null }, lastUpdated: snapshot.updatedAt, botStatus: 'LIVE_TELEMETRY', btcPrice: snapshot.market.price, signal: snapshot.market.indicators, serverTimeMillis: Date.now() } });
  } catch (error) { res.status(502).json({ success: false, error: error.message }); }
});

app.get('/api/market', requireSession, async (req, res) => {
  try { res.json({ success: true, data: (await refresh()).market }); }
  catch (error) { res.status(502).json({ success: false, error: error.message }); }
});

app.get('/api/signals', requireSession, async (req, res) => {
  try { const s = await refresh(); res.json({ success: true, data: { symbol: SYMBOL, strategy: 'EMA 9/21 + RSI 14', signal: s.market.indicators, generatedAt: s.updatedAt, note: 'Signal/telemetry only; no order is submitted.' } }); }
  catch (error) { res.status(502).json({ success: false, error: error.message }); }
});

app.get('/api/account', requireSession, async (req, res) => {
  try { const s = await refresh(); res.json({ success: true, data: s.spot }); }
  catch (error) { res.status(502).json({ success: false, error: error.message }); }
});
app.get('/api/balance/spot', requireSession, async (req, res) => {
  try { const s = await refresh(); res.json({ source: 'mexc-live-api', serverTimeMillis: Date.now(), balanceUsdt: s.spot.totalUsdt, assets: s.spot.balances || [] }); }
  catch (error) { res.status(502).json({ success: false, error: error.message }); }
});
app.get('/api/balance/futures', requireSession, async (req, res) => {
  try { const s = await refresh(); res.json({ source: 'mexc-live-contract-api', serverTimeMillis: Date.now(), assets: s.futures.assets || [] }); }
  catch (error) { res.status(502).json({ success: false, error: error.message }); }
});

setInterval(() => syncServerTime(), 60000);
setInterval(() => refresh().catch(() => undefined), 10000);
syncServerTime();
refresh().catch(() => undefined);

app.listen(PORT, () => console.log(`[MAROAH] telemetry service listening on ${PORT}`));
