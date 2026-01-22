<template>
  <div class="auth-page">
    <div class="auth-card">
      <span class="emoji">🔐</span>

      <h2 class="title">З поверненням!</h2>
      <p class="subtitle">
        Увійди в обліковий запис і продовжуй планувати свої подорожі
      </p>

      <form @submit.prevent="handleLogin" class="form">
        <FormKit
            type="text"
            label="Username"
            v-model="userName"
            validation="required"
        />

        <FormKit
            type="password"
            label="Пароль"
            v-model="password"
            validation="required"
        />

        <button class="btn primary" :disabled="store.loading || showOverlay">
          Увійти
        </button>
      </form>

      <p v-if="store.error" class="error">
        {{ store.error }}
      </p>

      <p class="hint">
        Немає акаунту?
        <router-link to="/register">Створити безкоштовно</router-link>
      </p>
    </div>

    <transition name="fade">
      <div v-if="showOverlay" class="loading-overlay">
        <div class="overlay-content">
          <div class="spinner"></div>

          <transition name="slide-fade" mode="out-in">
            <h2 :key="currentMsgIndex" class="loading-title">
              {{ messages[currentMsgIndex] }}
            </h2>
          </transition>

          <p class="wait-text">Це займе лише мить...</p>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { useRouter } from 'vue-router'

const store = useUserStore()
const router = useRouter()

const userName = ref("")
const password = ref("")

const showOverlay = ref(false)
const currentMsgIndex = ref(0)
let msgInterval = null

const messages = [
  "Перевіряємо ваші дані...",
  "Раді бачити вас знову! ✈️",
  "Завантажуємо ваш профіль...",
  "Готуємо ваші подорожі..."
]

function startMsgRotation() {
  currentMsgIndex.value = 0
  msgInterval = setInterval(() => {
    currentMsgIndex.value = (currentMsgIndex.value + 1) % messages.length
  }, 1500) // Змінюємо текст кожні 1.5 секунди
}

function stopMsgRotation() {
  if (msgInterval) clearInterval(msgInterval)
}

async function handleLogin() {
  store.error = null

  showOverlay.value = true
  startMsgRotation()

  try {
    const [ok] = await Promise.all([
      store.login(userName.value, password.value),
      new Promise(resolve => setTimeout(resolve, 400))
    ])

    if (ok) {
      router.push("/home")
    } else {
      throw new Error("Login failed")
    }
  } catch (e) {
    showOverlay.value = false
    stopMsgRotation()
  }
}

onUnmounted(() => {
  stopMsgRotation()
})
</script>

<style scoped>
/* Ваші старі стилі */
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
  padding: 20px;
}

.auth-card {
  background: rgba(20, 40, 50, 0.85);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 26px;
  padding: 48px 36px;
  max-width: 420px;
  width: 100%;
  text-align: center;
  color: #ffffff;
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.45);
}

.emoji { font-size: 52px; display: block; margin-bottom: 16px; }
.title { font-size: 2.3rem; font-weight: 800; margin-bottom: 10px; }
.subtitle { font-size: 0.95rem; color: rgba(255, 255, 255, 0.75); margin-bottom: 30px; }
.form { display: flex; flex-direction: column; gap: 14px; }

.btn {
  margin-top: 20px;
  padding: 14px;
  border-radius: 14px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.25s ease;
}

.primary {
  background: #646cff;
  color: #ffffff;
  box-shadow: 0 6px 18px rgba(100, 108, 255, 0.4);
}
.primary:hover:not(:disabled) { background: #535bf2; transform: translateY(-2px); }
.primary:disabled { opacity: 0.6; cursor: not-allowed; }

.error { margin-top: 16px; color: #ffb4b4; font-size: 0.9rem; }
.hint { margin-top: 24px; font-size: 0.85rem; color: rgba(255, 255, 255, 0.65); }
.hint a { color: #8fa2ff; text-decoration: none; }
.hint a:hover { text-decoration: underline; }

/* НОВЕ: Стилі для оверлею (Mini-screen) */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(15, 23, 42, 0.9); /* Темний фон */
  backdrop-filter: blur(10px);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  text-align: center;
  padding: 20px;
}

.overlay-content {
  max-width: 400px;
}

.loading-title {
  font-size: 1.5rem;
  color: #fff;
  margin-bottom: 10px;
  min-height: 3rem; /* Щоб текст не стрибав */
}

.wait-text {
  color: #94a3b8;
  font-size: 0.9rem;
}

/* Спінер */
.spinner {
  width: 60px;
  height: 60px;
  border: 5px solid rgba(255,255,255,0.1);
  border-left-color: #646cff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 30px;
}

/* Анімації */
@keyframes spin { 100% { transform: rotate(360deg); } }

.fade-enter-active, .fade-leave-active { transition: opacity 0.4s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.3s ease; }
.slide-fade-enter-from { opacity: 0; transform: translateY(10px); }
.slide-fade-leave-to { opacity: 0; transform: translateY(-10px); }

@media (max-width: 480px) {
  .auth-card { padding: 36px 22px; }
  .title { font-size: 2rem; }
}
</style>