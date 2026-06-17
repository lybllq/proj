const user = localStorage.getItem("deli_user");
if (!user) window.location.href = "./index.html";

const checkoutData = JSON.parse(sessionStorage.getItem("deli_checkout") || "null");
if (!checkoutData || !Array.isArray(checkoutData.items) || !checkoutData.items.length) {
  window.location.href = "./app.html";
}

const state = {
  addresses: [],
  selectedAddressId: checkoutData.addressId || "",
  selectedLabel: "Дом",
};

const orderListEl = document.getElementById("paymentOrderList");
const addressEl = document.getElementById("paymentAddress");
const addressSelectEl = document.getElementById("paymentAddressSelect");
const etaEl = document.getElementById("paymentEta");
const goodsAmountEl = document.getElementById("paymentGoodsAmount");
const deliveryFeeEl = document.getElementById("paymentDeliveryFee");
const couponAmountEl = document.getElementById("paymentCouponAmount");
const totalAmountEl = document.getElementById("paymentTotalAmount");
const payNowBtn = document.getElementById("payNowBtn");
const backToCartBtn = document.getElementById("backToCartBtn");
const methods = document.querySelectorAll(".pay-method");
const openAddressModalBtn = document.getElementById("openAddressModalBtn");
const closeAddressModalBtn = document.getElementById("closeAddressModalBtn");
const addressModal = document.getElementById("addressModal");
const addressDetailForm = document.getElementById("addressDetailForm");
const labelButtons = document.querySelectorAll(".label-btn");
const defaultAddressInput = document.getElementById("defaultAddressInput");

const goodsAmount = Number(checkoutData.amount || 0);
const deliveryFee = Number(checkoutData.deliveryFee || 0);
const coupon = checkoutData.coupon || null;
const couponDiscount = coupon && goodsAmount >= Number(coupon.minAmount || 0) ? Number(coupon.discount || 0) : 0;
const payable = Math.max(0, goodsAmount + deliveryFee - couponDiscount);

function formatPrice(value) {
  return `¥${value}`;
}

function getSelectedAddress() {
  return state.addresses.find((item) => item.id === state.selectedAddressId) || state.addresses[0] || null;
}

function renderAddressOptions() {
  if (!state.addresses.length) {
    addressSelectEl.innerHTML = '<option value="">Нет адресов, сначала добавьте адрес</option>';
    addressSelectEl.disabled = true;
    payNowBtn.disabled = true;
    payNowBtn.textContent = "Сначала добавьте адрес";
    return;
  }

  const fallbackAddress = state.addresses.find((item) => item.isDefault) || state.addresses[0];
  if (!state.selectedAddressId || !state.addresses.some((item) => item.id === state.selectedAddressId)) {
    state.selectedAddressId = fallbackAddress.id;
  }

  addressSelectEl.disabled = false;
  addressSelectEl.innerHTML = state.addresses
    .map((item) => {
      const text = `${item.fullAddress} · ${item.receiver} ${item.phone} [${item.label}]`;
      return `<option value="${item.id}">${text}${item.isDefault ? " (по умолчанию)" : ""}</option>`;
    })
    .join("");
  addressSelectEl.value = state.selectedAddressId;
}

function renderOrderItems() {
  orderListEl.innerHTML = checkoutData.items
    .map((item) => {
      return `
      <article class="payment-item">
        <div>
          <strong>${item.name}</strong>
          <p class="hint">¥${item.price} / порция</p>
        </div>
        <div class="payment-item-right">
          <span>x${item.qty}</span>
          <strong>${formatPrice(item.subtotal)}</strong>
        </div>
      </article>
    `;
    })
    .join("");
}

function renderSummary() {
  const selectedAddress = getSelectedAddress();
  addressEl.textContent = selectedAddress ? selectedAddress.fullAddress : "Адрес не выбран";
  etaEl.textContent = checkoutData.eta || "Ориентировочно 30 минут";
  goodsAmountEl.textContent = formatPrice(goodsAmount);
  deliveryFeeEl.textContent = formatPrice(deliveryFee);
  couponAmountEl.textContent = `- ${formatPrice(couponDiscount)}`;
  totalAmountEl.textContent = formatPrice(payable);
  if (selectedAddress) {
    payNowBtn.disabled = false;
    payNowBtn.textContent = `Подтвердить оплату ${formatPrice(payable)}`;
  }
}

function openAddressModal() {
  addressModal.classList.remove("hidden");
}

function closeAddressModal() {
  addressModal.classList.add("hidden");
  addressDetailForm.reset();
  state.selectedLabel = "Дом";
  labelButtons.forEach((btn) => btn.classList.toggle("active", btn.dataset.label === "Дом"));
}

methods.forEach((item) => {
  item.addEventListener("click", () => {
    methods.forEach((m) => m.classList.remove("active"));
    item.classList.add("active");
    const radio = item.querySelector("input[type='radio']");
    if (radio) radio.checked = true;
  });
});

labelButtons.forEach((btn) => {
  btn.addEventListener("click", () => {
    state.selectedLabel = btn.dataset.label;
    labelButtons.forEach((item) => item.classList.remove("active"));
    btn.classList.add("active");
  });
});

openAddressModalBtn.addEventListener("click", openAddressModal);
closeAddressModalBtn.addEventListener("click", closeAddressModal);
addressModal.addEventListener("click", (event) => {
  if (event.target === addressModal) closeAddressModal();
});

async function refreshAddresses() {
  state.addresses = await ApiClient.getAddresses(user);
}

addressDetailForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const receiver = document.getElementById("receiverInput").value.trim();
  const phone = document.getElementById("phoneInput").value.trim();
  const province = document.getElementById("provinceInput").value.trim();
  const city = document.getElementById("cityInput").value.trim();
  const district = document.getElementById("districtInput").value.trim();
  const detail = document.getElementById("detailInput").value.trim();
  const isDefaultChecked = defaultAddressInput.checked || state.addresses.length === 0;

  if (!receiver || !phone || !province || !city || !district || !detail) return;

  const fullAddress = `${province}${city}${district}${detail}`;
  await ApiClient.addAddress({
    userPhone: user,
    receiver,
    phone,
    province,
    city,
    district,
    detail,
    fullAddress,
    label: state.selectedLabel,
    isDefault: isDefaultChecked,
  });

  await refreshAddresses();
  const selected = state.addresses.find((item) => item.fullAddress === fullAddress && item.phone === phone);
  if (selected) {
    state.selectedAddressId = selected.id;
  }
  renderAddressOptions();
  renderSummary();
  closeAddressModal();
});

backToCartBtn.addEventListener("click", () => {
  window.location.href = "./app.html";
});

addressSelectEl.addEventListener("change", (event) => {
  state.selectedAddressId = event.target.value;
  const selectedAddress = getSelectedAddress();
  checkoutData.addressId = state.selectedAddressId;
  checkoutData.address = selectedAddress ? selectedAddress.fullAddress : "";
  sessionStorage.setItem("deli_checkout", JSON.stringify(checkoutData));
  renderSummary();
});

payNowBtn.addEventListener("click", async () => {
  const selectedAddress = getSelectedAddress();
  if (!selectedAddress) {
    alert("Сначала добавьте или выберите адрес доставки.");
    return;
  }

  payNowBtn.disabled = true;
  payNowBtn.textContent = "Обработка оплаты...";

  try {
    const payMethod = document.querySelector("input[name='payMethod']:checked")?.value || "alipay";
    const result = await ApiClient.createOrder({
      userPhone: user,
      items: checkoutData.items,
      goodsAmount,
      deliveryFee,
      couponId: coupon && coupon.id ? coupon.id : null,
      couponDiscount,
      payable,
      payMethod,
      addressId: selectedAddress.id,
      address: selectedAddress.fullAddress,
      receiver: selectedAddress.receiver,
      receiverPhone: selectedAddress.phone,
      addressLabel: selectedAddress.label,
    });

    localStorage.removeItem("deli_cart");
    localStorage.removeItem("deli_selected_coupon");
    sessionStorage.removeItem("deli_checkout");
    alert(`Оплата успешна. Номер заказа: ${result.orderNo}`);
    window.location.href = "./app.html";
  } catch (error) {
    payNowBtn.disabled = false;
    payNowBtn.textContent = `Подтвердить оплату ${formatPrice(payable)}`;
    alert(`Ошибка оплаты: ${error.message}`);
  }
});

async function bootstrap() {
  try {
    await ApiClient.tryUpsertUser(user);
    await refreshAddresses();
    renderAddressOptions();
    renderOrderItems();
    renderSummary();
  } catch (error) {
    alert(`Ошибка инициализации: ${error.message}`);
  }
}

bootstrap();
