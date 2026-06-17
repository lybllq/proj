const API_BASE_URL_KEY = "deli_shared_backend_url";
const DEFAULT_API_BASE_URL = "http://127.0.0.1:3001/api";

function normalizeBaseUrl(url) {
  const value = String(url || "").trim();
  if (!value) return DEFAULT_API_BASE_URL;
  return value.endsWith("/") ? value.slice(0, -1) : value;
}

function getApiBaseUrl() {
  const saved = localStorage.getItem(API_BASE_URL_KEY);
  return normalizeBaseUrl(saved || DEFAULT_API_BASE_URL);
}

async function request(path, options = {}) {
  const response = await fetch(`${getApiBaseUrl()}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
    ...options,
  });

  let payload = null;
  try {
    payload = await response.json();
  } catch (error) {
    payload = null;
  }

  if (!response.ok) {
    const message = payload && payload.message ? payload.message : `Ошибка запроса: ${response.status}`;
    throw new Error(message);
  }
  return payload;
}

window.ApiClient = {
  // Optional: call window.setSharedBackendUrl("http://<ip>:3001/api") for LAN testing.
  setSharedBackendUrl(url) {
    localStorage.setItem(API_BASE_URL_KEY, normalizeBaseUrl(url));
  },
  getSharedBackendUrl() {
    return getApiBaseUrl();
  },
  upsertUser(phone) {
    return request("/auth/upsert", {
      method: "POST",
      body: JSON.stringify({ phone }),
    });
  },
  async tryUpsertUser(phone) {
    try {
      return await this.upsertUser(phone);
    } catch (error) {
      // Older/simple backends may not implement /auth/upsert.
      // Ignore this failure to keep UI bootstrap working.
      if (String(error && error.message).includes("404")) {
        return null;
      }
      return null;
    }
  },
  register(phone, password, name = "") {
    return request("/auth/register", {
      method: "POST",
      body: JSON.stringify({ phone, password, name }),
    });
  },
  login(phone, password) {
    return request("/auth/login", {
      method: "POST",
      body: JSON.stringify({ phone, password }),
    });
  },
  getFoods() {
    return request("/foods");
  },
  getAddresses(userPhone) {
    return request(`/addresses?userPhone=${encodeURIComponent(userPhone)}`);
  },
  addAddress(data) {
    return request("/addresses", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
  setDefaultAddress(addressId, userPhone) {
    return request(`/addresses/${addressId}/default`, {
      method: "PATCH",
      body: JSON.stringify({ userPhone }),
    });
  },
  deleteAddress(addressId, userPhone) {
    return request(`/addresses/${addressId}?userPhone=${encodeURIComponent(userPhone)}`, {
      method: "DELETE",
    });
  },
  getCoupons(userPhone) {
    return request(`/coupons?userPhone=${encodeURIComponent(userPhone)}`);
  },
  getOrders(userPhone) {
    return request(`/orders?userPhone=${encodeURIComponent(userPhone)}`);
  },
  createOrder(data) {
    return request("/orders", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },
};
