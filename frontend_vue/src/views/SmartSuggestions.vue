<template>
  <div class="suggestions-page">
    <div class="container">

      <!-- HEADER -->
      <div class="header">
        <button class="back" @click="goHome">← Повернутися на головну</button>
        <h1>Інтелектуальні пропозиції</h1>
        <p class="subtitle">
          Персоналізовані ідеї для подорожей на основі ваших інтересів
        </p>
      </div>

      <!-- ACTION -->
      <div class="action-card">
        <p>
          Натисніть кнопку, щоб отримати персональні рекомендації від AI
        </p>

        <button class="btn primary" :disabled="loading" @click="getSuggestions">
          {{ loading ? 'Аналізуємо...' : 'Отримати пропозиції' }}
        </button>
      </div>

      <!-- RESULTS -->
      <div v-if="suggestions.length" class="results">
        <div
            v-for="(s, i) in suggestions"
            :key="i"
            class="suggestion-card"
        >
          <h3>{{ s.title }}</h3>
          <p>{{ s.description }}</p>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
// позже подключишь api
// import api from '@/api/axios'

const router = useRouter()
const loading = ref(false)

const suggestions = ref([])

function goHome() {
  router.push('/main')
}

async function getSuggestions() {
  loading.value = true

  // 🔧 ПОКА ЗАГЛУШКА (потом заменишь на GPT API)
  setTimeout(() => {
    suggestions.value = [
      {
        title: '🌍 Неочевидний напрямок',
        description: 'Розгляньте Порту — він схожий на Барселону, але дешевший і спокійніший.'
      },
      {
        title: '💸 Бюджетна альтернатива',
        description: 'Замість Парижа варто звернути увагу на Ліон — культура та кухня без натовпів.'
      },
      {
        title: '🗓 Ідея для короткої подорожі',
        description: '3–4 дні в Кракові ідеально підійдуть для культурного відпочинку.'
      },
      {
        title: '⚠️ Що варто уникати',
        description: 'Високий сезон у Римі може бути перевантаженим та дорогим.'
      }
    ]
    loading.value = false
  }, 1200)
}
</script>

<style scoped>
.suggestions-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
  padding: 40px;
  color: white;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.back {
  position: absolute;
  left: 40px;
  background: none;
  border: none;
  color: #6dd3ff;
  cursor: pointer;
}

.subtitle {
  margin-top: 10px;
  opacity: 0.85;
}

.action-card {
  background: rgba(255,255,255,0.08);
  padding: 30px;
  border-radius: 18px;
  text-align: center;
  margin-bottom: 40px;
}

.btn {
  margin-top: 20px;
  padding: 14px 22px;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  font-weight: 700;
}

.btn.primary {
  background: #6dd3ff;
  color: #012;
}

.results {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.suggestion-card {
  background: rgba(255,255,255,0.1);
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.25);
}

.suggestion-card h3 {
  margin-bottom: 8px;
  font-size: 18px;
}
</style>
