const state = {
  user: localStorage.getItem("deli_user"),
  activeCategory: "Все",
  keyword: "",
  cart: JSON.parse(localStorage.getItem("deli_cart") || "{}"),
  profileTab: "orders",
  profileAddressLabel: "Дом",
  foods: [],
  addresses: [],
  coupons: [],
  orders: [],
};

if (!state.user) {
  window.location.href = "./index.html";
}

const userName = document.getElementById("userName");
const categoryTags = document.getElementById("categoryTags");
const restaurantList = document.getElementById("restaurantList");
const searchInput = document.getElementById("searchInput");
const cartItems = document.getElementById("cartItems");
const cartTotal = document.getElementById("cartTotal");
const cartBadge = document.getElementById("cartBadge");
const logoutBtn = document.getElementById("logoutBtn");
const checkoutBtn = document.getElementById("checkoutBtn");
const profilePanel = document.getElementById("profilePanel");
const profileMenuList = document.querySelector(".menu-list");

function persistCart() {
  localStorage.setItem("deli_cart", JSON.stringify(state.cart));
}

function getCategories() {
  return ["Все", ...new Set(state.foods.map((item) => item.category))];
}

function getOrders() {
  return state.orders;
}

function getDefaultAddress() {
  return state.addresses.find((item) => item.isDefault) || state.addresses[0] || null;
}

function formatTime(ts) {
  const d = new Date(ts);
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function getCurrentCoupon() {
  const couponId = localStorage.getItem("deli_selected_coupon");
  if (!couponId) return null;
  return state.coupons.find((item) => item.id === couponId && item.status === "unused") || null;
}

function showView(viewId) {
  document.querySelectorAll(".view").forEach((view) => view.classList.remove("active"));
  document.querySelectorAll(".tab-item").forEach((tab) => tab.classList.remove("active"));
  document.getElementById(viewId).classList.add("active");
  document.querySelector(`.tab-item[data-view="${viewId}"]`).classList.add("active");
}

function renderCategories() {
  const categories = getCategories();
  categoryTags.innerHTML = categories
    .map(
      (item) => `
      <button class="tag ${state.activeCategory === item ? "active" : ""}" data-tag="${item}">
        ${item}
      </button>
    `
    )
    .join("");
}

function filteredFoods() {
  return state.foods.filter((item) => {
    const byCategory = state.activeCategory === "Все" || item.category === state.activeCategory;
    const byKeyword = item.name.toLowerCase().includes(state.keyword.trim().toLowerCase());
    return byCategory && byKeyword;
  });
}

function renderFoods() {
  const list = filteredFoods();

  if (!list.length) {
    restaurantList.innerHTML = '<p class="empty">Подходящих блюд не найдено</p>';
    return;
  }

  restaurantList.innerHTML = list
    .map((item) => {
      return `
      <article class="shop-card">
        <div class="shop-thumb">${item.emoji}</div>
        <div class="shop-meta">
          <strong>${item.name}</strong>
          <p>${item.category} · ⭐ ${item.rating} · ${item.time}</p>
          <p class="price">¥${item.price}</p>
        </div>
        <button class="icon-btn" data-add="${item.id}">+</button>
      </article>
    `;
    })
    .join("");
}

function getCartEntries() {
  return Object.entries(state.cart).filter(([, qty]) => qty > 0);
}

function getFoodById(id) {
  return state.foods.find((item) => item.id === id);
}

function renderCart() {
  const entries = getCartEntries();

  if (!entries.length) {
    cartItems.innerHTML = '<p class="empty">Корзина пуста, выберите блюда на главной</p>';
    cartTotal.textContent = "¥0";
    cartBadge.textContent = "0";
    return;
  }

  let total = 0;
  let totalQty = 0;
  cartItems.innerHTML = entries
    .map(([id, qty]) => {
      const food = getFoodById(id);
      if (!food) return "";
      const sub = Number(food.price) * qty;
      total += sub;
      totalQty += qty;
      return `
      <div class="cart-row">
        <div>
          <strong>${food.name}</strong>
          <p class="hint">¥${food.price} / порция</p>
        </div>
        <div class="qty">
          <button data-minus="${id}">-</button>
          <span>${qty}</span>
          <button data-plus="${id}">+</button>
        </div>
        <strong>¥${sub}</strong>
      </div>
    `;
    })
    .join("");

  cartTotal.textContent = `¥${total}`;
  cartBadge.textContent = String(totalQty);
}

function addToCart(id) {
  state.cart[id] = (state.cart[id] || 0) + 1;
  persistCart();
  renderCart();
}

function reduceFromCart(id) {
  if (!state.cart[id]) return;
  state.cart[id] -= 1;
  if (state.cart[id] <= 0) {
    delete state.cart[id];
  }
  persistCart();
  renderCart();
}

function renderProfilePanel() {
  if (!profilePanel) return;

  if (state.profileTab === "orders") {
    const orders = getOrders();
    if (!orders.length) {
      profilePanel.innerHTML = '<p class="empty">Заказов пока нет, оформите первый заказ на главной</p>';
      return;
    }

    profilePanel.innerHTML = orders
      .map((order) => {
        return `
          <article class="order-item">
            <div>
              <strong>Заказ № ${order.orderNo}</strong>
              <p class="hint">${formatTime(order.createdAt)} · ${order.status}</p>
              <p class="hint">${order.address}</p>
            </div>
            <strong>¥${order.payable}</strong>
          </article>
        `;
      })
      .join("");
    return;
  }

  if (state.profileTab === "address") {
    const addresses = state.addresses;
    profilePanel.innerHTML = `
      <div class="address-manage-grid">
        <section class="panel">
          <h3 class="payment-title">Добавить адрес</h3>
          <form id="profileAddressForm" class="address-form">
            <label>
              Получатель
              <input id="profileReceiverInput" type="text" placeholder="Введите имя получателя" required />
            </label>
            <label>
              Номер телефона
              <input id="profilePhoneInput" type="tel" placeholder="Введите номер телефона" required />
            </label>
            <div class="address-area-row">
              <label>
                Регион
                <input id="profileProvinceInput" type="text" placeholder="Регион" required />
              </label>
              <label>
                Город
                <input id="profileCityInput" type="text" placeholder="Город" required />
              </label>
              <label>
                Район
                <input id="profileDistrictInput" type="text" placeholder="Район" required />
              </label>
            </div>
            <label>
              Подробный адрес
              <textarea id="profileDetailInput" placeholder="Улица, дом, квартира и т.д." required></textarea>
            </label>
            <div class="label-group">
              <span>Метка адреса</span>
              <div class="label-buttons">
                <button type="button" class="label-btn ${state.profileAddressLabel === "Дом" ? "active" : ""}" data-profile-label="Дом">Дом</button>
                <button type="button" class="label-btn ${state.profileAddressLabel === "Работа" ? "active" : ""}" data-profile-label="Работа">Работа</button>
                <button type="button" class="label-btn ${state.profileAddressLabel === "Учеба" ? "active" : ""}" data-profile-label="Учеба">Учеба</button>
              </div>
            </div>
            <label class="switch-row">
              <span>Сделать адресом по умолчанию</span>
              <input id="profileDefaultAddressInput" type="checkbox" />
            </label>
            <button type="submit" class="btn btn-primary">Сохранить адрес</button>
          </form>
        </section>

        <section class="panel">
          <h3 class="payment-title">Мои адреса</h3>
          <div class="profile-list">
            ${
              addresses.length
                ? addresses
                    .map((item) => {
                      return `
                        <article class="address-item">
                          <div>
                            <strong>${item.fullAddress}</strong>
                            <p class="hint">${item.receiver} · ${item.phone} · [${item.label}]</p>
                            ${item.isDefault ? '<span class="state-tag">По умолчанию</span>' : ""}
                          </div>
                          <div class="action-row">
                            ${item.isDefault ? "" : `<button class="btn btn-light" data-set-default="${item.id}">Сделать основным</button>`}
                            <button class="btn btn-light" data-delete-address="${item.id}">Удалить</button>
                          </div>
                        </article>
                      `;
                    })
                    .join("")
                : '<p class="empty">Адресов пока нет, добавьте первый адрес</p>'
            }
          </div>
        </section>
      </div>
    `;
    return;
  }

  if (state.profileTab === "coupon") {
    const current = getCurrentCoupon();
    const coupons = state.coupons;
    profilePanel.innerHTML = `
      <p class="hint">Сейчас выбран: ${current ? `${current.title} (-¥${current.discount})` : "Не выбран"}</p>
      <div class="profile-list">
        ${coupons
          .map((item) => {
            const disabled = item.status !== "unused";
            return `
              <article class="coupon-item ${disabled ? "disabled" : ""}">
                <div>
                  <strong>${item.title}</strong>
                  <p class="hint">Действует от ¥${item.minAmount} · скидка ¥${item.discount}</p>
                </div>
                <div class="action-row">
                  <span class="state-tag">${item.status === "unused" ? "Доступен" : "Использован"}</span>
                  ${
                    disabled
                      ? ""
                      : `<button class="btn btn-light" data-use-coupon="${item.id}">${current && current.id === item.id ? "Выбран" : "Выбрать"}</button>`
                  }
                </div>
              </article>
            `;
          })
          .join("")}
      </div>
    `;
    return;
  }

  profilePanel.innerHTML = `
    <section class="service-box">
      <h3>Служба поддержки</h3>
      <p class="hint">Если возникла задержка доставки или проблема с блюдом, отправьте обращение.</p>
      <p class="hint">Горячая линия: 400-800-2026 (09:00-22:00)</p>
      <form id="serviceForm" class="service-form">
        <textarea id="serviceText" placeholder="Опишите проблему, мы свяжемся с вами как можно быстрее" required></textarea>
        <button type="submit" class="btn btn-primary">Отправить</button>
      </form>
    </section>
  `;
}

async function refreshProfileData() {
  const [addresses, coupons, orders] = await Promise.all([
    ApiClient.getAddresses(state.user),
    ApiClient.getCoupons(state.user),
    ApiClient.getOrders(state.user),
  ]);
  state.addresses = addresses;
  state.coupons = coupons;
  state.orders = orders;
}

logoutBtn.addEventListener("click", () => {
  localStorage.removeItem("deli_user");
  localStorage.removeItem("deli_user_name");
  localStorage.removeItem("deli_cart");
  localStorage.removeItem("deli_selected_coupon");
  sessionStorage.removeItem("deli_checkout");
  window.location.href = "./index.html";
});

searchInput.addEventListener("input", (event) => {
  state.keyword = event.target.value;
  renderFoods();
});

categoryTags.addEventListener("click", (event) => {
  const button = event.target.closest("button[data-tag]");
  if (!button) return;
  state.activeCategory = button.dataset.tag;
  renderCategories();
  renderFoods();
});

restaurantList.addEventListener("click", (event) => {
  const btn = event.target.closest("button[data-add]");
  if (!btn) return;
  addToCart(btn.dataset.add);
});

cartItems.addEventListener("click", (event) => {
  const plus = event.target.closest("button[data-plus]");
  const minus = event.target.closest("button[data-minus]");

  if (plus) addToCart(plus.dataset.plus);
  if (minus) reduceFromCart(minus.dataset.minus);
});

document.querySelector(".tabbar").addEventListener("click", (event) => {
  const button = event.target.closest(".tab-item");
  if (!button) return;
  showView(button.dataset.view);
});

if (profileMenuList) {
  profileMenuList.addEventListener("click", async (event) => {
    const btn = event.target.closest("button[data-profile-tab]");
    if (!btn) return;
    profileMenuList.querySelectorAll("button").forEach((item) => item.classList.remove("active"));
    btn.classList.add("active");
    state.profileTab = btn.dataset.profileTab;
    if (state.profileTab !== "service") {
      await refreshProfileData();
    }
    renderProfilePanel();
  });
}

if (profilePanel) {
  profilePanel.addEventListener("submit", async (event) => {
    if (event.target.id === "profileAddressForm") {
      event.preventDefault();
      const receiver = document.getElementById("profileReceiverInput").value.trim();
      const phone = document.getElementById("profilePhoneInput").value.trim();
      const province = document.getElementById("profileProvinceInput").value.trim();
      const city = document.getElementById("profileCityInput").value.trim();
      const district = document.getElementById("profileDistrictInput").value.trim();
      const detail = document.getElementById("profileDetailInput").value.trim();
      const makeDefault = document.getElementById("profileDefaultAddressInput").checked;
      if (!receiver || !phone || !province || !city || !district || !detail) return;

      await ApiClient.addAddress({
        userPhone: state.user,
        receiver,
        phone,
        province,
        city,
        district,
        detail,
        fullAddress: `${province}${city}${district}${detail}`,
        label: state.profileAddressLabel,
        isDefault: makeDefault,
      });

      state.profileAddressLabel = "Дом";
      await refreshProfileData();
      renderProfilePanel();
      return;
    }

    if (event.target.id === "serviceForm") {
      event.preventDefault();
      const text = document.getElementById("serviceText").value.trim();
      if (!text) return;
      alert("Обращение отправлено. Поддержка свяжется с вами в ближайшее время.");
      event.target.reset();
    }
  });

  profilePanel.addEventListener("click", async (event) => {
    const profileLabelBtn = event.target.closest("button[data-profile-label]");
    const setDefaultBtn = event.target.closest("button[data-set-default]");
    const deleteAddressBtn = event.target.closest("button[data-delete-address]");
    const useCouponBtn = event.target.closest("button[data-use-coupon]");

    if (profileLabelBtn) {
      state.profileAddressLabel = profileLabelBtn.dataset.profileLabel;
      profilePanel.querySelectorAll("button[data-profile-label]").forEach((item) => item.classList.remove("active"));
      profileLabelBtn.classList.add("active");
      return;
    }

    if (setDefaultBtn) {
      const id = setDefaultBtn.dataset.setDefault;
      await ApiClient.setDefaultAddress(id, state.user);
      await refreshProfileData();
      renderProfilePanel();
      return;
    }

    if (deleteAddressBtn) {
      const id = deleteAddressBtn.dataset.deleteAddress;
      await ApiClient.deleteAddress(id, state.user);
      await refreshProfileData();
      renderProfilePanel();
      return;
    }

    if (useCouponBtn) {
      localStorage.setItem("deli_selected_coupon", useCouponBtn.dataset.useCoupon);
      renderProfilePanel();
    }
  });
}

checkoutBtn.addEventListener("click", () => {
  const entries = getCartEntries();
  if (!entries.length) {
    alert("Корзина пуста. Сначала добавьте товары.");
    return;
  }

  const items = entries
    .map(([id, qty]) => {
      const food = getFoodById(id);
      if (!food) return null;
      return {
        id,
        name: food.name,
        qty,
        price: Number(food.price),
        subtotal: Number(food.price) * qty,
      };
    })
    .filter(Boolean);

  const amount = items.reduce((sum, item) => sum + item.subtotal, 0);
  const address = getDefaultAddress();
  const coupon = getCurrentCoupon();
  sessionStorage.setItem(
    "deli_checkout",
    JSON.stringify({
      items,
      amount,
      deliveryFee: 4,
      addressId: address ? address.id : "",
      address: address ? address.fullAddress : "Добавьте адрес в разделе управления адресами",
      coupon,
      eta: "Ориентировочно 28 минут",
      createdAt: Date.now(),
    })
  );
  window.location.href = "./payment.html";
});

async function bootstrap() {
  try {
    await ApiClient.tryUpsertUser(state.user);
    const userTail = state.user.slice(-4).padStart(state.user.length, "*");
    userName.textContent = `Пользователь ${userTail}`;

    state.foods = await ApiClient.getFoods();
    await refreshProfileData();

    renderCategories();
    renderFoods();
    renderCart();
    renderProfilePanel();
  } catch (error) {
    alert(`Ошибка инициализации: ${error.message}`);
  }
}

bootstrap();
