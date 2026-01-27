<template>
  <div class="home-page">
    <!-- ===== LOADING ===== -->
    <div v-if="!user" class="loading">
      Завантаження профілю...
    </div>

    <!-- ===== PAGE ===== -->
    <template v-else>
      <!-- ===== TOP BAR ===== -->
      <header class="top-bar">
        <div class="container">
          <div class="top-content">
            <div class="user">
              <img src="/placeholder.png" class="avatar" alt="User Avatar" />
              <div class="user-text">
                <h2>{{ user.name }} {{ user.surname }}</h2>
                <p>@{{ user.userName }}</p>
              </div>
            </div>

            <div class="date">
              <div class="day">{{ currentDay }}</div>
              <div class="month">{{ currentMonth }}</div>
              <div class="weekday">{{ currentWeekday }}</div>
            </div>
          </div>
        </div>
      </header>

      <!-- ===== MAIL ===== -->
      <section class="mail-section">
        <div class="container">
          <h3>Вхідні повідомлення</h3>

          <div
              v-if="notification"
              class="notification-icon"
              @click="toggleNotification"
          >
            📩 Вхідні повідомлення
          </div>

          <div v-if="notification && showNotification" class="mail-card">
            <h4>{{ notification.title }}</h4>
            <p>{{ notification.message }}</p>

            <ul>
              <li v-for="place in notification.places" :key="place.id">
                {{ place.name }}
                <span v-if="place.liked">👍</span>
                <span v-if="place.disliked">👎</span>
              </li>
            </ul>
          </div>

          <div v-else-if="!notification" class="mail-card">
            📩 Нових повідомлень поки немає
          </div>
        </div>
      </section>

      <!-- ===== MAIN CONTENT ===== -->
      <main class="content">
        <div class="container grid">
          <section class="glass-card">
            <h3>Особисті дані</h3>
            <ul>
              <li><b>Email:</b> {{ user.gmail }}</li>
              <li><b>Телефон:</b> {{ user.phone }}</li>
              <li><b>Стать:</b> {{ user.gender }}</li>
              <li><b>Username:</b> {{ user.userName }}</li>
            </ul>
          </section>

          <section class="glass-card center">
            <h3>Telegram-бот</h3>
            <p>
              Керуйте профілем, нагадуваннями та планами подорожей
              через Telegram-бот.
            </p>
            <a :href="telegramLink" target="_blank" class="btn-main">
              Перейти до Telegram-бота
            </a>
          </section>

          <section v-if="securityStatus && !user.twoFactorEnabled" class="glass-card danger center">
            <h3>⚠️ Безпека акаунту</h3>

            <div v-if="!securityStatus.hasPassword || !securityStatus.hasSecretPhrase">
              <p style="margin-bottom: 15px; opacity: 0.9;">
                Для повноцінного захисту налаштуйте:
              </p>

              <div v-if="!securityStatus.hasPassword" style="margin-bottom: 12px;">
                <span>❌ Пароль не встановлено</span>
                <button class="btn-warning" @click="requestSetup('PASSWORD')">
                  Надіслати посилання на пароль
                </button>
              </div>

              <div v-if="!securityStatus.hasSecretPhrase" style="margin-bottom: 12px;">
                <span>❌ Секретна фраза відсутня</span>
                <button class="btn-warning" @click="requestSetup('SECRET')">
                  Надіслати посилання на фразу
                </button>
              </div>
            </div>

            <div style="margin-top: 20px; border-top: 1px solid rgba(255,255,255,0.2); padding-top: 15px;">
              <div v-if="user.twoFactorEnabled">
                ✅ <b style="color: #4ade80;">2FA (Google Auth) увімкнено</b>
              </div>
              <div v-else>
                <div style="margin-bottom: 8px;">🛡️ Додатковий захист</div>
                <button class="btn-main" @click="startSetup" style="width: 100%;">
                  Підключити 2FA
                </button>
              </div>
            </div>
          </section>



          <section class="glass-card center">
            <h3>Навігація</h3>
            <p>Перейти на головну сторінку</p>
            <button class="btn-main" @click="goToMain">
              Головна сторінка
            </button>
          </section>

          <section class="glass-card center danger">
            <h3>Обліковий запис</h3>
            <p>Вихід з облікового запису</p>
            <button class="btn-danger" @click="logout">
              Вийти
            </button>
          </section>
        </div>
      </main>

      <div v-if="show2FAModal" class="modal-card">
        <h3>🔐 Налаштування 2FA</h3>
        <div v-if="!qrUrl">
          <p>Завантаження QR-коду...</p>
        </div>
        <div v-else>
          <p>1. Скануйте цей код (Microsoft Authenticator або камерою):</p>
          <qrcode-vue :value="qrUrl" :size="200" level="H" />

          <div style="margin: 15px 0; background: rgba(255,255,255,0.1); padding: 10px; border-radius: 8px;">
            <p style="font-size: 12px; opacity: 0.7; margin: 0;">Не сканується? Введіть вручну:</p>
            <strong style="font-size: 25px; letter-spacing: 2px; color: #4ade80;">{{ manualSecret }}</strong>
          </div>
          <p>2. Введіть код із додатка:</p>
          <input v-model="code" type="number" placeholder="123456" />

          <div style="display: flex; gap: 10px; justify-content: center;">
            <button @click="show2FAModal = false" style="background: grey;">Скасувати</button>
            <button @click="confirmSetup">Підтвердити</button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import api from '@/api/axios'
import QrcodeVue from 'qrcode.vue'
const showNotification = ref(false);


const store = useUserStore()
const router = useRouter()
const notification = ref(null);

const telegramLink = 'https://t.me/diplomtgfeedbackbot'
const now = new Date()

const currentDay = computed(() => now.getDate())
const currentMonth = computed(() =>
    now.toLocaleString('uk-UA', { month: 'long' })
)
const currentWeekday = computed(() =>
    now.toLocaleString('uk-UA', { weekday: 'long' })
)

const user = computed(() => store.user)

function logout() {
  store.logout()
  router.push('/login')
}

function goToMain() {
  router.push('/main')
}
function toggleNotification() {
  showNotification.value = !showNotification.value;
}
async function fetchNotification() {
  try {
    const res = await api.get("/api/notifications/latest");
    notification.value = res.data;
  } catch (err) {
    console.warn("Уведомлений нет", err);
    notification.value = null;
  }
}
const securityStatus = ref(null);
async function fetchSecurityStatus() {
  try {
    const res = await api.get("/me/security-status");
    securityStatus.value = res.data;
  } catch (err) {
    console.error("Помилка перевірки безпеки:", err);
  }
}

async function requestSetup(type) {
  try {
    await api.post("/me/request-setup", { type });
    alert("Посилання відправлено на вашу пошту! (Дивись консоль сервера)");
  } catch (e) {
    alert("Помилка відправки");
  }
}

const qrUrl = ref('')
const code = ref('')
const show2FAModal = ref(false)

// Додай нову змінну
const manualSecret = ref('')

// Онови функцію startSetup
async function startSetup() {
  try {
    const res = await api.get('/me/2fa/setup') // Або твій шлях
    qrUrl.value = res.data.qrUrl
    manualSecret.value = res.data.secret // <--- Зберігаємо секрет
    show2FAModal.value = true
  } catch (e) {
    alert("Помилка отримання QR-коду")
  }
}

async function confirmSetup() {
  try {
    // Используем правильный путь из последнего бэкенда
    await api.post('/me/2fa/verify', { code: Number(code.value) })
    alert('2FA успішно активовано!')
    show2FAModal.value = false
    store.fetchUser() // Обновляем данные юзера, чтобы кнопка изменилась на "Включено"
  } catch (e) {
    alert('Невірний код')
  }
}

onMounted(() => {
  store.fetchUser();
  fetchNotification();
  fetchSecurityStatus();
});
</script>

<style scoped>
/* ===== PAGE ===== */
.home-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
  color: #ffffff;
}

/* ===== CONTAINER ===== */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* ===== TOP BAR (GLASS) ===== */
.top-bar {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  padding: 28px 0;
}

.top-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user {
  display: flex;
  gap: 16px;
  align-items: center;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
}

.user-text h2 {
  margin: 0;
  font-weight: 700;
}

.user-text p {
  margin: 0;
  opacity: 0.7;
}

/* ===== DATE ===== */
.date {
  background: rgba(255, 255, 255, 0.12);
  color: white;
  padding: 12px 18px;
  border-radius: 14px;
  text-align: center;
}

.date .day {
  font-size: 28px;
  font-weight: bold;
}

/* ===== CONTENT ===== */
.content {
  padding: 40px 0;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

/* ===== GLASS CARD ===== */
.glass-card {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-radius: 18px;
  padding: 22px;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.glass-card.center {
  text-align: center;
}

.glass-card.danger {
  border: 1px solid rgba(255, 120, 120, 0.4);
}

/* ===== BUTTONS ===== */
.btn-main {
  display: inline-block;
  margin-top: 16px;
  padding: 12px 22px;
  background: #646cff;
  color: white;
  border-radius: 14px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.25s ease;
}

.btn-main:hover {
  background: #535bf2;
  transform: translateY(-2px);
}

.btn-danger {
  margin-top: 16px;
  padding: 12px 22px;
  background: rgba(220, 38, 38, 0.85);
  color: white;
  border-radius: 14px;
  border: none;
  cursor: pointer;
}

.btn-danger:hover {
  background: #dc2626;
}

/* ===== MAIL ===== */
.notification-icon {
  cursor: pointer;
  background: rgba(255, 255, 255, 0.15);
  padding: 10px 16px;
  border-radius: 12px;
  display: inline-block;
  margin-bottom: 10px;
}

.mail-card {
  background: rgba(255, 255, 255, 0.08);
  padding: 16px;
  border-radius: 16px;
  margin-bottom: 12px;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 900px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .top-content {
    flex-direction: column;
    gap: 16px;
  }
}

.btn-warning {
  display: block;
  width: 100%;
  margin-top: 8px;
  padding: 10px;
  background: rgba(245, 158, 11, 0.9); /* Желто-оранжевый */
  color: white;
  border-radius: 10px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-warning:hover {
  background: #d97706;
  transform: translateY(-1px);
}

/* ===== MODAL ===== */
.modal-card {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #1e293b; /* Темний фон */
  padding: 30px;
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.8);
  border: 1px solid rgba(255,255,255,0.1);
  text-align: center;
  z-index: 1000;
  width: 90%;
  max-width: 400px;
  animation: fadeIn 0.3s ease;
}

/* Затемнення фону за модалкою */
.modal-card::before {
  content: '';
  position: fixed;
  top: -1000px; /* Розтягуємо на весь екран */
  left: -1000px;
  right: -1000px;
  bottom: -1000px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(5px);
  z-index: -1; /* Щоб фон був ЗА карткою */
}

.modal-card h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: white;
}

.modal-card input {
  display: block;
  width: 100%;
  padding: 12px;
  margin: 20px 0;
  border-radius: 10px;
  border: 1px solid #475569;
  background: #0f172a;
  color: white;
  font-size: 18px;
  text-align: center;
}

.modal-card button {
  background: #646cff;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 10px;
  cursor: pointer;
  font-weight: bold;
  font-size: 16px;
  width: 100%;
}

.modal-card button:hover {
  background: #535bf2;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translate(-50%, -40%); }
  to { opacity: 1; transform: translate(-50%, -50%); }
}

</style>
