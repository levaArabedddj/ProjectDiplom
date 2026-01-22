<template>
  <div class="google-auth-loading">
    <div class="spinner"></div>
    <h2>Авторизація через Google...</h2>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

onMounted(() => {
  const token = route.query.token

  if (token) {
    store.setToken(token)

    store.fetchUser()

    router.push('/suggestions')
  } else {
    console.error('Токен не знайдено в URL')
    router.push('/login')
  }
})
</script>

<style scoped>
.google-auth-loading {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #0f2027; /* Твой темный фон */
  color: white;
  gap: 20px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 5px solid rgba(255,255,255,0.1);
  border-left-color: #646cff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin { 100% { transform: rotate(360deg); } }
</style>