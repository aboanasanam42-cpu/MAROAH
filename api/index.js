import crypto from 'crypto';
import axios from 'axios';

// جلب المفاتيح المتطابقة بدقة مع متغيرات Vercel لديك
const BASE_URL = process.env.MEXC_BASE_URL || 'https://api.mexc.com';
const API_KEY = process.env.MEXC_APP_KEY || process.env.MEXC_BLOCKBEAT_KEY || process.env.MEXC_API_KEY;
const SECRET_KEY = process.env.MEXC_APP_SECRET || process.env.MEXC_BLOCKBEAT_SECRET || process.env.MEXC_SECRET_KEY;

// التأكد من توفر المفاتيح في البيئة
function validateCredentials() {
  if (!API_KEY || !SECRET_KEY) {
    throw new Error('مفاتيح MEXC غير متوفرة في بيئة Vercel. تأكد من وجود MEXC_APP_KEY و MEXC_APP_SECRET.');
  }
}

// إنشاء التوقيع المشفر HMAC-SHA256
function sign(queryString) {
  return crypto
    .createHmac('sha256', SECRET_KEY)
    .update(queryString)
    .digest('hex');
}

// 1. قراءة بيانات ورصيد الحساب المتاح
export async function getAccountInfo() {
  validateCredentials();
  const timestamp = Date.now();
  const queryString = `timestamp=${timestamp}`;
  const signature = sign(queryString);

  const response = await axios.get(`${BASE_URL}/api/v3/account?${queryString}&signature=${signature}`, {
    headers: {
      'X-MEXC-APIKEY': API_KEY,
      'Content-Type': 'application/json',
    },
  });

  const activeBalances = response.data.balances.filter(
    (b) => parseFloat(b.free) > 0 || parseFloat(b.locked) > 0
  );

  return { full: response.data, nonZero: activeBalances };
}

// 2. قراءة السعر الحالي لزوج تداول
export async function getTickerPrice(symbol = 'BTCUSDT') {
  const response = await axios.get(`${BASE_URL}/api/v3/ticker/price`, {
    params: { symbol: symbol.toUpperCase() },
  });
  return response.data;
}

// 3. تنفيذ أمر تداول فوري (شراء / بيع)
export async function placeOrder(symbol, side, type, quantity, price = null) {
  validateCredentials();
  const timestamp = Date.now();
  const queryParams = {
    symbol: symbol.toUpperCase(),
    side: side.toUpperCase(),
    type: type.toUpperCase(),
    quantity: quantity,
    timestamp: timestamp,
  };

  if (type.toUpperCase() === 'LIMIT' && price) {
    queryParams.price = price;
  }

  const queryString = new URLSearchParams(queryParams).toString();
  const signature = sign(queryString);

  const response = await axios.post(
    `${BASE_URL}/api/v3/order?${queryString}&signature=${signature}`,
    null,
    {
      headers: {
        'X-MEXC-APIKEY': API_KEY,
        'Content-Type': 'application/json',
      },
    }
  );

  return response.data;
}

// Handler المخصص لمنصة Vercel لمعالجة الطلبات الواردة
export default async function handler(req, res) {
  try {
    const { action, symbol = 'BTCUSDT', side, type, quantity, price } = req.query;

    switch (action) {
      case 'balance': {
        const balances = await getAccountInfo();
        return res.status(200).json({ success: true, balances: balances.nonZero });
      }

      case 'price': {
        const ticker = await getTickerPrice(symbol);
        return res.status(200).json({ success: true, data: ticker });
      }

      case 'order': {
        if (!side || !type || !quantity) {
          return res.status(400).json({ error: 'side, type, and quantity are required.' });
        }
        const orderResult = await placeOrder(symbol, side, type, quantity, price);
        return res.status(200).json({ success: true, order: orderResult });
      }

      default:
        return res.status(200).json({
          status: 'ready',
          active_key: API_KEY ? `${API_KEY.slice(0, 6)}...` : 'not_found',
          routes: {
            check_balance: '/api?action=balance',
            check_price: '/api?action=price&symbol=BTCUSDT',
            make_order: '/api?action=order&symbol=BTCUSDT&side=BUY&type=MARKET&quantity=0.001',
          },
        });
    }
  } catch (error) {
    return res.status(500).json({
      success: false,
      error: error.response?.data || error.message,
    });
  }
}
