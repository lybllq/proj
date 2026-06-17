const express = require("express");
const cors = require("cors");
const { open } = require("sqlite");
const sqlite3 = require("sqlite3");
const crypto = require("crypto");
const path = require("path");

const PORT = process.env.PORT || 3001;
const DB_PATH = path.join(__dirname, "deli_shared.db");

const DEFAULT_COUPONS = [
  { id: "c1", title: "Купон новичка", discount: 8, minAmount: 30 },
  { id: "c2", title: "Скидка 12 от 50", discount: 12, minAmount: 50 },
  { id: "c3", title: "Ночной купон", discount: 6, minAmount: 25 },
];

const DEFAULT_FOODS = [
  { id: "f1", name: "Фирменный боул с говядиной", category: "Кухня", price: 32, time: "26-32 мин", rating: 4.8, emoji: "🍱" },
  { id: "f2", name: "Острый бургер с курицей", category: "Фастфуд", price: 25, time: "18-25 мин", rating: 4.6, emoji: "🍔" },
  { id: "f3", name: "Суши с лососем", category: "Японская", price: 46, time: "30-40 мин", rating: 4.9, emoji: "🍣" },
  { id: "f4", name: "Паста с креветками", category: "Европейская", price: 42, time: "28-35 мин", rating: 4.7, emoji: "🍝" },
  { id: "f5", name: "Манговый десерт", category: "Десерты", price: 18, time: "15-20 мин", rating: 4.5, emoji: "🥭" }
];

let db;

async function initDb() {
  db = await open({ filename: DB_PATH, driver: sqlite3.Database });
  await db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      phone TEXT PRIMARY KEY,
      password TEXT NOT NULL,
      name TEXT,
      created_at INTEGER NOT NULL
    );
    CREATE TABLE IF NOT EXISTS foods (
      id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      category TEXT NOT NULL,
      price REAL NOT NULL,
      time TEXT NOT NULL,
      rating REAL NOT NULL,
      emoji TEXT NOT NULL
    );
    CREATE TABLE IF NOT EXISTS addresses (
      id TEXT PRIMARY KEY,
      user_phone TEXT NOT NULL,
      receiver TEXT NOT NULL,
      phone TEXT NOT NULL,
      province TEXT NOT NULL,
      city TEXT NOT NULL,
      district TEXT NOT NULL,
      detail TEXT NOT NULL,
      full_address TEXT NOT NULL,
      label TEXT NOT NULL,
      is_default INTEGER NOT NULL DEFAULT 0,
      created_at INTEGER NOT NULL
    );
    CREATE TABLE IF NOT EXISTS coupons (
      id TEXT PRIMARY KEY,
      user_phone TEXT NOT NULL,
      title TEXT NOT NULL,
      discount REAL NOT NULL,
      min_amount REAL NOT NULL,
      status TEXT NOT NULL,
      created_at INTEGER NOT NULL
    );
    CREATE TABLE IF NOT EXISTS orders (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      order_no TEXT NOT NULL UNIQUE,
      user_phone TEXT NOT NULL,
      created_at INTEGER NOT NULL,
      status TEXT NOT NULL,
      address TEXT NOT NULL,
      receiver TEXT NOT NULL,
      receiver_phone TEXT NOT NULL,
      address_label TEXT NOT NULL,
      goods_amount REAL NOT NULL,
      delivery_fee REAL NOT NULL,
      coupon_discount REAL NOT NULL,
      payable REAL NOT NULL,
      pay_method TEXT NOT NULL
    );
    CREATE TABLE IF NOT EXISTS order_items (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      order_id INTEGER NOT NULL,
      food_id TEXT,
      name TEXT NOT NULL,
      qty INTEGER NOT NULL,
      price REAL NOT NULL,
      subtotal REAL NOT NULL
    );
  `);

  const foodCount = await db.get("SELECT COUNT(1) AS count FROM foods");
  if (foodCount.count === 0) {
    for (const food of DEFAULT_FOODS) {
      await db.run(
        "INSERT INTO foods (id, name, category, price, time, rating, emoji) VALUES (?, ?, ?, ?, ?, ?, ?)",
        [food.id, food.name, food.category, food.price, food.time, food.rating, food.emoji]
      );
    }
  }
}

async function ensureUserCoupons(userPhone) {
  const count = await db.get("SELECT COUNT(1) AS count FROM coupons WHERE user_phone = ?", [userPhone]);
  if (count.count > 0) return;

  const now = Date.now();
  for (const coupon of DEFAULT_COUPONS) {
    await db.run(
      "INSERT INTO coupons (id, user_phone, title, discount, min_amount, status, created_at) VALUES (?, ?, ?, ?, ?, 'unused', ?)",
      [`${coupon.id}_${userPhone}`, userPhone, coupon.title, coupon.discount, coupon.minAmount, now]
    );
  }
}

function addressDto(row) {
  return {
    id: row.id,
    userPhone: row.user_phone,
    receiver: row.receiver,
    phone: row.phone,
    province: row.province,
    city: row.city,
    district: row.district,
    detail: row.detail,
    fullAddress: row.full_address,
    label: row.label,
    isDefault: row.is_default === 1
  };
}

function couponDto(row) {
  return {
    id: row.id,
    title: row.title,
    discount: row.discount,
    minAmount: row.min_amount,
    status: row.status
  };
}

async function orderDto(row) {
  const items = await db.all(
    "SELECT food_id AS foodId, name, qty, price, subtotal FROM order_items WHERE order_id = ? ORDER BY id",
    [row.id]
  );
  return {
    id: row.id,
    orderNo: row.order_no,
    createdAt: row.created_at,
    status: row.status,
    address: row.address,
    receiver: row.receiver,
    phone: row.receiver_phone,
    addressLabel: row.address_label,
    goodsAmount: row.goods_amount,
    deliveryFee: row.delivery_fee,
    couponDiscount: row.coupon_discount,
    payable: row.payable,
    payMethod: row.pay_method,
    items
  };
}

async function main() {
  await initDb();
  const app = express();
  app.use(cors());
  app.use(express.json());

  app.get("/api/health", (_req, res) => res.json({ ok: true }));

  app.post("/api/auth/login", async (req, res) => {
    const { phone, password } = req.body || {};
    if (!phone || !password) return res.status(400).json({ message: "phone and password are required" });

    const user = await db.get("SELECT phone, password, name FROM users WHERE phone = ?", [phone]);
    if (!user) {
      return res.status(404).json({ message: "user not found, please register first" });
    }
    if (user.password !== password) {
      return res.status(401).json({ message: "invalid password" });
    }

    await ensureUserCoupons(phone);
    const profile = await db.get("SELECT phone, name FROM users WHERE phone = ?", [phone]);
    return res.json({ user: { phone: profile.phone, name: profile.name } });
  });

  app.post("/api/auth/register", async (req, res) => {
    const { phone, password, name } = req.body || {};
    if (!phone || !password) {
      return res.status(400).json({ message: "phone and password are required" });
    }

    const exists = await db.get("SELECT phone FROM users WHERE phone = ?", [phone]);
    if (exists) {
      return res.status(409).json({ message: "user already exists" });
    }

    const now = Date.now();
    await db.run(
      "INSERT INTO users (phone, password, name, created_at) VALUES (?, ?, ?, ?)",
      [phone, password, name || `Пользователь${phone.slice(-4)}`, now]
    );
    await ensureUserCoupons(phone);
    const profile = await db.get("SELECT phone, name FROM users WHERE phone = ?", [phone]);
    return res.json({ ok: true, user: { phone: profile.phone, name: profile.name } });
  });

  app.post("/api/auth/upsert", async (req, res) => {
    const { phone } = req.body || {};
    if (!phone) return res.status(400).json({ message: "phone is required" });

    const now = Date.now();
    const user = await db.get("SELECT phone, name FROM users WHERE phone = ?", [phone]);
    if (!user) {
      await db.run(
        "INSERT INTO users (phone, password, name, created_at) VALUES (?, ?, ?, ?)",
        [phone, "app_sync_password", `Пользователь${phone.slice(-4)}`, now]
      );
    }
    await ensureUserCoupons(phone);
    const profile = await db.get("SELECT phone, name FROM users WHERE phone = ?", [phone]);
    return res.json({ user: { phone: profile.phone, name: profile.name } });
  });

  app.get("/api/foods", async (_req, res) => {
    const foods = await db.all("SELECT id, name, category, price, time, rating, emoji FROM foods ORDER BY id");
    return res.json(foods);
  });

  app.get("/api/addresses", async (req, res) => {
    const { userPhone } = req.query;
    if (!userPhone) return res.status(400).json({ message: "userPhone is required" });
    const rows = await db.all(
      "SELECT * FROM addresses WHERE user_phone = ? ORDER BY is_default DESC, created_at DESC",
      [userPhone]
    );
    return res.json(rows.map(addressDto));
  });

  app.post("/api/addresses", async (req, res) => {
    const { userPhone, receiver, phone, province, city, district, detail, fullAddress, label, isDefault } = req.body || {};
    if (!userPhone || !receiver || !phone || !province || !city || !district || !detail || !fullAddress || !label) {
      return res.status(400).json({ message: "address fields are incomplete" });
    }

    const count = await db.get("SELECT COUNT(1) AS count FROM addresses WHERE user_phone = ?", [userPhone]);
    const shouldDefault = Boolean(isDefault) || count.count === 0;
    if (shouldDefault) await db.run("UPDATE addresses SET is_default = 0 WHERE user_phone = ?", [userPhone]);

    const id = crypto.randomUUID();
    await db.run(
      `INSERT INTO addresses
      (id, user_phone, receiver, phone, province, city, district, detail, full_address, label, is_default, created_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [id, userPhone, receiver, phone, province, city, district, detail, fullAddress, label, shouldDefault ? 1 : 0, Date.now()]
    );
    const row = await db.get("SELECT * FROM addresses WHERE id = ?", [id]);
    return res.json(addressDto(row));
  });

  app.put("/api/addresses/:id", async (req, res) => {
    const { id } = req.params;
    const { userPhone, receiver, phone, province, city, district, detail, fullAddress, label, isDefault } = req.body || {};
    if (!id || !userPhone || !receiver || !phone || !province || !city || !district || !detail || !fullAddress || !label) {
      return res.status(400).json({ message: "address fields are incomplete" });
    }

    const target = await db.get("SELECT id FROM addresses WHERE id = ? AND user_phone = ?", [id, userPhone]);
    if (!target) return res.status(404).json({ message: "address not found" });

    if (isDefault) await db.run("UPDATE addresses SET is_default = 0 WHERE user_phone = ?", [userPhone]);
    await db.run(
      `UPDATE addresses
       SET receiver=?, phone=?, province=?, city=?, district=?, detail=?, full_address=?, label=?, is_default=?
       WHERE id=? AND user_phone=?`,
      [receiver, phone, province, city, district, detail, fullAddress, label, isDefault ? 1 : 0, id, userPhone]
    );
    const row = await db.get("SELECT * FROM addresses WHERE id = ?", [id]);
    return res.json(addressDto(row));
  });

  app.patch("/api/addresses/:id/default", async (req, res) => {
    const { id } = req.params;
    const { userPhone } = req.body || {};
    if (!id || !userPhone) return res.status(400).json({ message: "userPhone and id are required" });
    const target = await db.get("SELECT id FROM addresses WHERE id = ? AND user_phone = ?", [id, userPhone]);
    if (!target) return res.status(404).json({ message: "address not found" });

    await db.run("UPDATE addresses SET is_default = 0 WHERE user_phone = ?", [userPhone]);
    await db.run("UPDATE addresses SET is_default = 1 WHERE id = ? AND user_phone = ?", [id, userPhone]);
    return res.json({ ok: true });
  });

  app.delete("/api/addresses/:id", async (req, res) => {
    const { id } = req.params;
    const { userPhone } = req.query;
    if (!id || !userPhone) return res.status(400).json({ message: "userPhone and id are required" });

    await db.run("DELETE FROM addresses WHERE id = ? AND user_phone = ?", [id, userPhone]);
    const defaultRow = await db.get("SELECT id FROM addresses WHERE user_phone = ? AND is_default = 1 LIMIT 1", [userPhone]);
    if (!defaultRow) {
      const first = await db.get("SELECT id FROM addresses WHERE user_phone = ? ORDER BY created_at DESC LIMIT 1", [userPhone]);
      if (first) await db.run("UPDATE addresses SET is_default = 1 WHERE id = ?", [first.id]);
    }
    return res.json({ ok: true });
  });

  app.get("/api/coupons", async (req, res) => {
    const { userPhone } = req.query;
    if (!userPhone) return res.status(400).json({ message: "userPhone is required" });

    await ensureUserCoupons(userPhone);
    const rows = await db.all("SELECT * FROM coupons WHERE user_phone = ? ORDER BY created_at ASC", [userPhone]);
    return res.json(rows.map(couponDto));
  });

  app.get("/api/orders", async (req, res) => {
    const { userPhone } = req.query;
    if (!userPhone) return res.status(400).json({ message: "userPhone is required" });
    const rows = await db.all("SELECT * FROM orders WHERE user_phone = ? ORDER BY created_at DESC", [userPhone]);
    const result = [];
    for (const row of rows) result.push(await orderDto(row));
    return res.json(result);
  });

  app.post("/api/orders", async (req, res) => {
    const {
      userPhone, items, goodsAmount, deliveryFee, couponId, couponDiscount, payable, payMethod,
      address, receiver, receiverPhone, addressLabel
    } = req.body || {};

    if (!userPhone || !Array.isArray(items) || items.length === 0 || !address || !receiver || !receiverPhone) {
      return res.status(400).json({ message: "order fields are incomplete" });
    }

    const now = Date.now();
    const orderNo = `DD${now}`;
    await db.run("BEGIN TRANSACTION");
    try {
      const result = await db.run(
        `INSERT INTO orders
         (order_no, user_phone, created_at, status, address, receiver, receiver_phone, address_label, goods_amount, delivery_fee, coupon_discount, payable, pay_method)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
        [
          orderNo, userPhone, now, "Ожидает доставки", address, receiver, receiverPhone, addressLabel || "",
          Number(goodsAmount || 0), Number(deliveryFee || 0), Number(couponDiscount || 0), Number(payable || 0), payMethod || "alipay"
        ]
      );

      for (const item of items) {
        await db.run(
          "INSERT INTO order_items (order_id, food_id, name, qty, price, subtotal) VALUES (?, ?, ?, ?, ?, ?)",
          [
            result.lastID,
            item.id || item.foodId || null,
            item.name,
            Number(item.qty || item.quantity || 0),
            Number(item.price || 0),
            Number(item.subtotal || (Number(item.price || 0) * Number(item.qty || item.quantity || 0)))
          ]
        );
      }

      if (couponId) {
        await db.run("UPDATE coupons SET status = 'used' WHERE id = ? AND user_phone = ? AND status = 'unused'", [couponId, userPhone]);
      }

      await db.run("COMMIT");
      return res.json({ ok: true, orderNo });
    } catch (error) {
      await db.run("ROLLBACK");
      return res.status(500).json({ message: error.message });
    }
  });

  app.listen(PORT, () => {
    console.log(`Shared backend running on http://localhost:${PORT}`);
  });
}

main().catch((error) => {
  console.error("Failed to start backend:", error);
  process.exit(1);
});
