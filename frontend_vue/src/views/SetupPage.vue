<template>
  <div class="setup-page">
    <div class="glass-card">
      <h2 v-if="type === 'PASSWORD'">🔐 Встановлення пароля</h2>
      <h2 v-else>🔑 Встановлення секретної фрази</h2>

      <p>Введіть нове значення нижче:</p>

      <input
          v-model="value"
          :type="type === 'PASSWORD' ? 'password' : 'text'"
          class="input-field"
          placeholder="Введіть значення..."
      />

      <button class="btn-main" @click="submit">Зберегти</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios' // Твой настроенный axios

const route = useRoute()
const router = useRouter()

const token = ref('')
const type = ref('')
const value = ref('')

onMounted(() => {
  token.value = route.query.token
  type.value = route.query.type // PASSWORD или SECRET

  if (!token.value) {
    alert("Невірне посилання")
    router.push('/login')
  }
})

async function submit() {
  try {
    // ВАЖНО: Отправляем на ПУБЛИЧНЫЙ эндпоинт (токен сам подтверждает право)
    await api.post('/auth/public/finish-setup', {
      token: token.value,
      value: value.value
    })

    alert("Успішно збережено! Тепер ви можете увійти.")
    router.push('/login')

  } catch (e) {
    console.error(e)
    alert("Помилка: " + (e.response?.data || "Токен недійсний"))
  }
}
</script>

<style scoped>
.setup-page {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
  color: white;
}
.glass-card {
  background: rgba(255, 255, 255, 0.1);
  padding: 40px;
  border-radius: 16px;
  text-align: center;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
}
.input-field {
  display: block;
  width: 100%;
  padding: 10px;
  margin: 20px 0;
  border-radius: 8px;
  border: none;
}
.btn-main {
  background: #646cff;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
</style>