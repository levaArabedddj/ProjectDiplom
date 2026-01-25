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
              <img src="/placeholder.png" class="avatar" />
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
          <!-- Personal info -->
          <section class="glass-card">
            <h3>Особисті дані</h3>
            <ul>
              <li><b>Email:</b> {{ user.gmail }}</li>
              <li><b>Телефон:</b> {{ user.phone }}</li>
              <li><b>Стать:</b> {{ user.gender }}</li>
              <li><b>Username:</b> {{ user.userName }}</li>
            </ul>
          </section>

          <!-- Telegram -->
          <section class="glass-card center">
            <h3>Telegram-бот</h3>
            <p>
              Керуйте профілем, нагадуваннями та планами подорожей
              через Telegram-бот.
            </p>

            <a
                :href="telegramLink"
                target="_blank"
                class="btn-main"
            >
              Перейти до Telegram-бота
            </a>
          </section>

          <section v-if="securityStatus && (!securityStatus.hasPassword || !securityStatus.hasSecretPhrase)" class="glass-card danger center">
            <h3>⚠️ Безпека акаунту</h3>
            <p style="margin-bottom: 15px; opacity: 0.9;">
              Для повноцінного захисту та входу через Telegram/Login, будь ласка, налаштуйте:
            </p>

            <div v-if="!securityStatus.hasPassword" style="margin-bottom: 12px;">
              <span>❌ Пароль не встановлено</span>
              <button class="btn-warning" @click="openSetPasswordModal">
                Встановити пароль
              </button>
            </div>

            <div v-if="!securityStatus.hasSecretPhrase">
              <span>❌ Секретна фраза відсутня</span>
              <button class="btn-warning" @click="openSetSecretPhraseModal">
                Встановити фразу
              </button>
            </div>
          </section>
          <!-- Navigation -->
          <section class="glass-card center">
            <h3>Навігація</h3>
            <p>Перейти на головну сторінку</p>

            <button class="btn-main" @click="goToMain">
              Головна сторінка
            </button>
          </section>


          <!-- Logout -->
          <section class="glass-card center danger">
            <h3>Обліковий запис</h3>
            <p>Вихід з облікового запису</p>

            <button class="btn-danger" @click="logout">
              Вийти
            </button>
          </section>

        </div>
      </main>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import api from '@/api/axios'
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
    const res = await api.get("/notifications/latest");
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

function openSetPasswordModal() {
  router.push('/settings/security');
  alert("Тут має відкритись форма встановлення пароля");
}

function openSetSecretPhraseModal() {
  router.push('/settings/security');
  alert("Тут має відкритись форма встановлення секретної фрази");
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

</style>
