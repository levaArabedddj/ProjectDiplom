<template>
  <div class="travel-page">
    <div class="container">
      <button @click="$router.push('/main')" class="back-btn">← Назад на головну</button>
      <h1 class="title">Мої подорожі 🌍</h1>

      <div v-if="trips.length === 0" class="glass-card empty-state">
        <p>У вас еще нет созданных путешествий.</p>
        <button @click="$router.push('/home')" class="btn-main">Создать первое</button>
      </div>

      <div v-else class="trips-grid">
        <div
            v-for="trip in trips"
            :key="trip.id"
            class="trip-card glass-card"
            @click="goToTrip(trip.id)"
        >
          <div class="card-header">
            <span class="city-icon">📍</span>
            <h3>{{ trip.cityName }}</h3>
          </div>
          <p class="date">📅 Початок: {{ formatDate(trip.startDate) }}</p>
          <div class="card-footer">
            <span>Переглянути деталі →</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const trips = ref([])
const router = useRouter()

onMounted(async () => {
  try {
    const response = await api.get('/api/trips')
    trips.value = response.data
  } catch (error) {
    console.error("Помилка завантаження подорожей:", error)
  }
})

function goToTrip(id) {
  router.push(`/travel/${id}`)
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('uk-UA')
}
</script>

<style scoped>
.travel-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
  padding: 60px 20px;
  color: white;
}
.container {
  max-width: 1400px;
  margin: 0 auto;
}
.title { text-align: center; margin-bottom: 40px; }

.trips-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 30px;
  padding: 20px 0;
}

.trip-card {
  width: 400px;
  flex: 0 1 400px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 200px;
  padding: 30px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.city-icon {
  font-size: 32px;
}

.trip-card:hover { transform: translateY(-10px); background: rgba(255, 255, 255, 0.15); }

.card-header h3 { margin: 0; font-size: 1.5rem; }
.date { opacity: 0.8; margin: 15px 0; }
.card-footer { color: #646cff; font-weight: 600; text-align: right; }
@media (max-width: 900px) {
  .trips-grid {
    grid-template-columns: 1fr;
  }
}
</style>