const loginForm = document.getElementById("loginForm");
const phoneInput = document.getElementById("phone");
const passwordInput = document.getElementById("password");

const savedUser = localStorage.getItem("deli_user");
if (savedUser) {
  window.location.href = "./app.html";
}

loginForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const phone = phoneInput.value.trim();
  const password = passwordInput.value.trim();
  if (!phone || !password) return;

  ApiClient.login(phone, password)
    .then((result) => {
      localStorage.setItem("deli_user", result.user.phone);
      localStorage.setItem("deli_user_name", result.user.name || "");
      window.location.href = "./app.html";
    })
    .catch((error) => {
      alert(`Ошибка входа: ${error.message}`);
    });
});
