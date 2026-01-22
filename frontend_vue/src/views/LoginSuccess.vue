<template>
  <div class="google-auth-loading">
    <div class="spinner"></div>
    <h2>Авторизація через Google...</h2>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import api from '@/api/axios'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

const statusText = ref('Авторизація через Google...')

onMounted(async () => {
  const token = route.query.token

  if (token) {
    try {
      store.setToken(token)

      statusText.value = 'Перевіряємо ваші дані...'

      await store.fetchUser()

      const response = await api.get('/api/preferences/status')
      const isQuestionnaireFilled = response.data

      if (isQuestionnaireFilled) {
        router.push('/home')
      } else {
        console.log('Анкети немає, йдемо заповнювати')
        router.push('/questionnaire')
      }

    } catch (e) {
      console.error('Помилка при вході:', e)
      router.push('/questionnaire')
    }
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