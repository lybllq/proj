# Shared Backend

Этот каталог содержит общий backend и базу данных (SQLite) для веб-версии и Android `app`.

## Запуск

```bash
npm install
npm start
```

Порт по умолчанию: `3001`  
Файл базы данных: `backend/deli_shared.db`

## Основные API

- `POST /api/auth/login` вход в веб-версии
- `POST /api/auth/register` регистрация в веб-версии
- `POST /api/auth/upsert` синхронизация пользователя из app без пароля
- `GET /api/foods`
- `GET/POST/PUT/PATCH/DELETE /api/addresses...`
- `GET /api/coupons`
- `GET /api/orders`
- `POST /api/orders`
