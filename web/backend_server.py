import json
import os
import time
import uuid
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse


HOST = "127.0.0.1"
PORT = 3001
DATA_DIR = os.path.join(os.path.dirname(__file__), "data")
STORE_FILE = os.path.join(DATA_DIR, "store.json")


DEFAULT_FOODS = [
    {"id": "f1", "name": "Рис с тушеной говядиной", "category": "Основные блюда", "emoji": "🍛", "rating": 4.8, "time": "25 мин", "price": 26},
    {"id": "f2", "name": "Комбо с куриным бургером", "category": "Фастфуд", "emoji": "🍔", "rating": 4.6, "time": "20 мин", "price": 22},
    {"id": "f3", "name": "Лапша с мраморной говядиной", "category": "Лапша", "emoji": "🍜", "rating": 4.7, "time": "24 мин", "price": 19},
    {"id": "f4", "name": "Фруктовый салат", "category": "Легкие блюда", "emoji": "🥗", "rating": 4.5, "time": "18 мин", "price": 16},
    {"id": "f5", "name": "Пицца с сыром", "category": "Европейская кухня", "emoji": "🍕", "rating": 4.4, "time": "32 мин", "price": 39},
    {"id": "f6", "name": "Жареный рис с креветками", "category": "Основные блюда", "emoji": "🍤", "rating": 4.7, "time": "26 мин", "price": 23},
]


def ensure_store():
    os.makedirs(DATA_DIR, exist_ok=True)
    if not os.path.exists(STORE_FILE):
        seed = {
            "users": [],
            "foods": DEFAULT_FOODS,
            "addresses": [],
            "coupons": [],
            "orders": [],
        }
        with open(STORE_FILE, "w", encoding="utf-8") as f:
            json.dump(seed, f, ensure_ascii=False, indent=2)


def load_store():
    ensure_store()
    with open(STORE_FILE, "r", encoding="utf-8") as f:
        return json.load(f)


def save_store(data):
    with open(STORE_FILE, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)


def read_json_body(handler):
    length = int(handler.headers.get("Content-Length", "0"))
    if length <= 0:
        return {}
    raw = handler.rfile.read(length).decode("utf-8")
    if not raw:
        return {}
    return json.loads(raw)


def ensure_user_coupons(data, user_phone):
    existing = [c for c in data["coupons"] if c["userPhone"] == user_phone]
    if existing:
        return
    defaults = [
        {
            "id": f"cpn-{uuid.uuid4().hex[:8]}",
            "userPhone": user_phone,
            "title": "Скидка для новичков",
            "minAmount": 20,
            "discount": 5,
            "status": "unused",
        },
        {
            "id": f"cpn-{uuid.uuid4().hex[:8]}",
            "userPhone": user_phone,
            "title": "Скидка 10 при заказе от 50",
            "minAmount": 50,
            "discount": 10,
            "status": "unused",
        },
    ]
    data["coupons"].extend(defaults)


class Handler(BaseHTTPRequestHandler):
    def _send(self, code=200, payload=None):
        body = json.dumps(payload if payload is not None else {}, ensure_ascii=False).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, PATCH, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()
        self.wfile.write(body)

    def do_OPTIONS(self):
        self._send(200, {"ok": True})

    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path
        query = parse_qs(parsed.query)
        data = load_store()

        if path == "/api/foods":
            return self._send(200, data["foods"])

        if path == "/api/addresses":
            user_phone = (query.get("userPhone") or [""])[0]
            addresses = [a for a in data["addresses"] if a["userPhone"] == user_phone]
            return self._send(200, addresses)

        if path == "/api/coupons":
            user_phone = (query.get("userPhone") or [""])[0]
            ensure_user_coupons(data, user_phone)
            save_store(data)
            coupons = [c for c in data["coupons"] if c["userPhone"] == user_phone]
            return self._send(200, coupons)

        if path == "/api/orders":
            user_phone = (query.get("userPhone") or [""])[0]
            orders = [o for o in data["orders"] if o["userPhone"] == user_phone]
            orders.sort(key=lambda item: item.get("createdAt", 0), reverse=True)
            return self._send(200, orders)

        return self._send(404, {"message": "Not found"})

    def do_POST(self):
        parsed = urlparse(self.path)
        path = parsed.path
        data = load_store()

        if path == "/api/auth/register":
            body = read_json_body(self)
            phone = str(body.get("phone", "")).strip()
            password = str(body.get("password", "")).strip()
            name = str(body.get("name", "")).strip()
            if not phone or not password:
                return self._send(400, {"message": "Телефон и пароль обязательны"})
            exists = any(u["phone"] == phone for u in data["users"])
            if exists:
                return self._send(400, {"message": "Этот номер уже зарегистрирован"})
            user = {"phone": phone, "password": password, "name": name}
            data["users"].append(user)
            ensure_user_coupons(data, phone)
            save_store(data)
            return self._send(201, {"message": "Регистрация успешна", "user": {"phone": phone, "name": name}})

        if path == "/api/auth/login":
            body = read_json_body(self)
            phone = str(body.get("phone", "")).strip()
            password = str(body.get("password", "")).strip()
            user = next((u for u in data["users"] if u["phone"] == phone and u["password"] == password), None)
            if not user:
                return self._send(401, {"message": "Неверный номер телефона или пароль"})
            return self._send(200, {"message": "Вход выполнен", "user": {"phone": user["phone"], "name": user.get("name", "")}})

        if path == "/api/addresses":
            body = read_json_body(self)
            user_phone = str(body.get("userPhone", "")).strip()
            if not user_phone:
                return self._send(400, {"message": "Отсутствует userPhone"})
            address = {
                "id": f"addr-{uuid.uuid4().hex[:8]}",
                "userPhone": user_phone,
                "receiver": str(body.get("receiver", "")).strip(),
                "phone": str(body.get("phone", "")).strip(),
                "province": str(body.get("province", "")).strip(),
                "city": str(body.get("city", "")).strip(),
                "district": str(body.get("district", "")).strip(),
                "detail": str(body.get("detail", "")).strip(),
                "fullAddress": str(body.get("fullAddress", "")).strip(),
                "label": str(body.get("label", "Дом")).strip() or "Дом",
                "isDefault": bool(body.get("isDefault", False)),
            }
            user_addresses = [a for a in data["addresses"] if a["userPhone"] == user_phone]
            if not user_addresses:
                address["isDefault"] = True
            if address["isDefault"]:
                for item in data["addresses"]:
                    if item["userPhone"] == user_phone:
                        item["isDefault"] = False
            data["addresses"].append(address)
            save_store(data)
            return self._send(201, address)

        if path == "/api/orders":
            body = read_json_body(self)
            user_phone = str(body.get("userPhone", "")).strip()
            if not user_phone:
                return self._send(400, {"message": "Отсутствует userPhone"})
            now = int(time.time() * 1000)
            order = {
                "id": f"ord-{uuid.uuid4().hex[:10]}",
                "orderNo": f"{now}{uuid.uuid4().hex[:4]}",
                "userPhone": user_phone,
                "items": body.get("items", []),
                "goodsAmount": body.get("goodsAmount", 0),
                "deliveryFee": body.get("deliveryFee", 0),
                "couponId": body.get("couponId"),
                "couponDiscount": body.get("couponDiscount", 0),
                "payable": body.get("payable", 0),
                "payMethod": body.get("payMethod", "alipay"),
                "addressId": body.get("addressId", ""),
                "address": body.get("address", ""),
                "receiver": body.get("receiver", ""),
                "receiverPhone": body.get("receiverPhone", ""),
                "addressLabel": body.get("addressLabel", "Дом"),
                "status": "Оплачен",
                "createdAt": now,
            }
            data["orders"].append(order)
            coupon_id = body.get("couponId")
            if coupon_id:
                for c in data["coupons"]:
                    if c["id"] == coupon_id and c["userPhone"] == user_phone:
                        c["status"] = "used"
                        break
            save_store(data)
            return self._send(201, {"message": "Заказ успешно создан", "orderNo": order["orderNo"], "order": order})

        return self._send(404, {"message": "Not found"})

    def do_PATCH(self):
        parsed = urlparse(self.path)
        path = parsed.path
        data = load_store()
        body = read_json_body(self)

        if path.startswith("/api/addresses/") and path.endswith("/default"):
            parts = path.split("/")
            if len(parts) < 5:
                return self._send(400, {"message": "Некорректный параметр адреса"})
            address_id = parts[3]
            user_phone = str(body.get("userPhone", "")).strip()
            target = next((a for a in data["addresses"] if a["id"] == address_id and a["userPhone"] == user_phone), None)
            if not target:
                return self._send(404, {"message": "Адрес не найден"})
            for item in data["addresses"]:
                if item["userPhone"] == user_phone:
                    item["isDefault"] = item["id"] == address_id
            save_store(data)
            return self._send(200, {"message": "Адрес по умолчанию обновлен"})

        return self._send(404, {"message": "Not found"})

    def do_DELETE(self):
        parsed = urlparse(self.path)
        path = parsed.path
        query = parse_qs(parsed.query)
        data = load_store()

        if path.startswith("/api/addresses/"):
            parts = path.split("/")
            if len(parts) < 4:
                return self._send(400, {"message": "Некорректный параметр адреса"})
            address_id = parts[3]
            user_phone = (query.get("userPhone") or [""])[0]
            remaining = [a for a in data["addresses"] if not (a["id"] == address_id and a["userPhone"] == user_phone)]
            if len(remaining) == len(data["addresses"]):
                return self._send(404, {"message": "Адрес не найден"})
            data["addresses"] = remaining
            user_addresses = [a for a in data["addresses"] if a["userPhone"] == user_phone]
            if user_addresses and not any(a.get("isDefault") for a in user_addresses):
                user_addresses[0]["isDefault"] = True
            save_store(data)
            return self._send(200, {"message": "Адрес удален"})

        return self._send(404, {"message": "Not found"})

    def log_message(self, format, *args):
        # Keep console output concise in IDE.
        return


if __name__ == "__main__":
    ensure_store()
    server = ThreadingHTTPServer((HOST, PORT), Handler)
    print(f"Backend API running on http://{HOST}:{PORT}/api")
    server.serve_forever()
