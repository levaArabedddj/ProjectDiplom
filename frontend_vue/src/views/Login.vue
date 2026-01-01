<template>
  <div class="auth-page">
    <div class="auth-card">
      <span class="emoji">🔐</span>

      <h2 class="title">С возвращением</h2>
      <p class="subtitle">
        Войди в аккаунт и продолжай планировать свои путешествия
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

        <button class="btn primary" :disabled="store.loading">
          Войти
        </button>
      </form>

      <p v-if="store.error" class="error">
        {{ store.error }}
      </p>

      <p class="hint">
        Нет аккаунта?
        <router-link to="/register">Создать бесплатно</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { useRouter } from 'vue-router'

const store = useUserStore()
const router = useRouter()

const email = ref("")
const password = ref("")

const userName = ref("");

async function handleLogin() {
  const ok = await store.login(userName.value, password.value)
  if (ok) router.push("/home")
}
</script>

<style scoped>
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

.emoji {
  font-size: 52px;
  display: block;
  margin-bottom: 16px;
}

.title {
  font-size: 2.3rem;
  font-weight: 800;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.75);
  margin-bottom: 30px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

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

.primary:hover:not(:disabled) {
  background: #535bf2;
  transform: translateY(-2px);
}

.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  margin-top: 16px;
  color: #ffb4b4;
  font-size: 0.9rem;
}

.hint {
  margin-top: 24px;
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.65);
}

.hint a {
  color: #8fa2ff;
  text-decoration: none;
}

.hint a:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .auth-card {
    padding: 36px 22px;
  }
  .title {
    font-size: 2rem;
  }
}

</style>
