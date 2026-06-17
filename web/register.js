const registerForm = document.getElementById("registerForm");
const phoneInput = document.getElementById("phone");
const passwordInput = document.getElementById("password");
const confirmPasswordInput = document.getElementById("confirmPassword");
const nameInput = document.getElementById("name");

const savedUser = localStorage.getItem("deli_user");
if (savedUser) {
  window.location.href = "./app.html";
}

registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const phone = phoneInput.value.trim();
  const password = passwordInput.value.trim();
  const confirmPassword = confirmPasswordInput.value.trim();
  const name = nameInput.value.trim();

  if (!phone || !password || !confirmPassword) return;
  if (password.length < 6) {
    alert("Пароль должен содержать минимум 6 символов");
    return;
  }
  if (password !== confirmPassword) {
    alert("Пароли не совпадают");
    return;
  }

  try {
    await ApiClient.register(phone, password, name);
    alert("Регистрация успешна, войдите в аккаунт");
    window.location.href = "./index.html";
  } catch (error) {
    alert(`Ошибка регистрации: ${error.message}`);
  }
});
