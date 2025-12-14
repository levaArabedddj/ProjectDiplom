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

          <!-- Иконка уведомления -->
          <div class="notification-icon" v-if="notification" @click="toggleNotification">
            📩 Нове повідомлення
          </div>

          <!-- Сообщение раскрывается/сворачивается -->
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

          <!-- Заглушка, если сообщений нет -->
          <div class="mail-card" v-else-if="!notification">
            <p>📩 Нових повідомлень поки немає</p>
          </div>
        </div>
      </section>

      <!-- ===== MAIN CONTENT ===== -->
      <main class="content">
        <div class="container grid">
          <!-- Personal info -->
          <section class="card">
            <h3>Особисті дані</h3>
            <ul>
              <li><b>Email:</b> {{ user.gmail }}</li>
              <li><b>Телефон:</b> {{ user.phone }}</li>
              <li><b>Стать:</b> {{ user.gender }}</li>
              <li><b>Username:</b> {{ user.userName }}</li>
            </ul>
          </section>

          <!-- Telegram -->
          <section class="card center">
            <p>
              Керуйте профілем, нагадуваннями та планами подорожей
              через Telegram-бот.
            </p>

            <a :href="telegramLink" target="_blank" class="btn-main">
              Перейти до Telegram-бота
            </a>
          </section>

          <!-- Navigation -->
          <section class="card center">
            <p>Перейти на головну сторінку</p>

            <button class="btn-main" @click="goToMain">
              Головна сторінка
            </button>
          </section>

          <!-- Logout -->
          <section class="card center danger">
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

onMounted(() => {
  store.fetchUser();
  fetchNotification();
});
</script>

<style scoped>
/* ===== BASE ===== */
.home-page {
  min-height: 100vh;
  background: #f4f6f8;
  font-family: Arial, sans-serif;
}

/* ===== CONTAINER ===== */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* ===== TOP BAR ===== */
.top-bar {
  background: #2563eb;
  color: white;
  padding: 24px 0;
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
  background: white;
}

.date {
  background: white;
  color: #2563eb;
  padding: 12px 18px;
  border-radius: 10px;
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

.card {
  background: white;
  padding: 20px;
  border-radius: 12px;
}

.card.center {
  text-align: center;
}

.card.danger {
  border: 1px solid #fecaca;
}

/* ===== BUTTONS ===== */
.btn-main {
  display: inline-block;
  margin-top: 16px;
  padding: 12px 20px;
  background: #2563eb;
  color: white;
  border-radius: 10px;
  text-decoration: none;
}

.btn-danger {
  margin-top: 16px;
  padding: 12px 20px;
  background: #dc2626;
  color: white;
  border-radius: 10px;
  border: none;
  cursor: pointer;
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
.notification-icon {
  cursor: pointer;
  background: #2563eb;
  color: white;
  padding: 10px 16px;
  border-radius: 10px;
  display: inline-block;
  margin-bottom: 10px;
  user-select: none;
}
.mail-card {
  background: white;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 12px;
}

</style>
