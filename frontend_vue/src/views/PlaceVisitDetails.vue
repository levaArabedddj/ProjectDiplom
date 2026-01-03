<template>
  <div class="place-page">
    <div class="container">
      <button @click="goBack" class="back-btn">← Назад</button>

      <div v-if="loading" class="state-msg glass-card">
        <span class="loader">⏳</span> Завантаження деталей…
      </div>

      <div v-else-if="error" class="state-msg error glass-card">
        ⚠️ {{ error }}
      </div>

      <section v-else class="glass-card main-card">
        <div class="card-header">
          <h1>{{ place.name }}</h1>
          <div v-if="place.price" class="price-tag">
            💰 {{ place.price }} {{ place.currency }}
          </div>
        </div>

        <div class="content-grid">
          <div class="image-wrapper" v-if="place.pictureUrl">
            <img :src="place.pictureUrl" class="place-image" alt="Place photo" />
          </div>

          <div class="info-block">
            <div class="info-item" v-if="place.latitude && place.longitude">
              <h3>📍 Локація</h3>
              <p>{{ place.latitude }}, {{ place.longitude }}</p>
              <a
                  :href="`https://www.google.com/maps/search/?api=1&query=${place.latitude},${place.longitude}`"
                  target="_blank"
                  class="map-link"
              >
                Показати на карті →
              </a>
            </div>

            <div class="info-item" v-if="place.description">
              <h3>📝 Опис</h3>
              <p>{{ place.description }}</p>
            </div>

            <div class="action-area" v-if="place.bookingLink">
              <a :href="place.bookingLink" target="_blank" class="book-btn">
                🔗 Перейти до бронювання
              </a>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios'

const route = useRoute()
const router = useRouter()

const place = ref({})
const loading = ref(true)
const error = ref(null)

function goBack() {
  router.back()
}

onMounted(async () => {
  const { tripId, placeId } = route.params
  try {
    const res = await api.get(`/api/trips/${tripId}/places/${placeId}`)
    place.value = res.data
  } catch (e) {
    console.error(e)
    error.value = 'Не вдалося завантажити деталі місця'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.place-page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
  padding: 40px 20px;
  box-sizing: border-box;
  color: white;
  display: block;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
}

.back-btn {
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.3);
  color: white;
  padding: 10px 20px;
  border-radius: 30px;
  cursor: pointer;
  margin-bottom: 25px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(255,255,255,0.25);
  transform: translateX(-5px);
}

.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 30px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  flex-wrap: wrap;
  gap: 15px;
}

.card-header h1 {
  margin: 0;
  font-size: 2.2rem;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.price-tag {
  background: #4ade80;
  color: #0f2027;
  padding: 8px 16px;
  border-radius: 12px;
  font-weight: bold;
  font-size: 1.1rem;
  box-shadow: 0 4px 12px rgba(74, 222, 128, 0.3);
}


.content-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 30px;
}

@media (min-width: 768px) {
  .content-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.image-wrapper {
  width: 100%;
  height: 300px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.1);
}

.place-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.place-image:hover {
  transform: scale(1.05);
}

.info-block {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 20px;
}

.info-item h3 {
  margin: 0 0 5px 0;
  font-size: 1.1rem;
  color: rgba(255,255,255,0.7);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.info-item p {
  margin: 0;
  font-size: 1.1rem;
  line-height: 1.5;
}

.map-link {
  color: #646cff;
  font-size: 0.9rem;
  text-decoration: none;
  border-bottom: 1px dashed #646cff;
  margin-top: 5px;
  display: inline-block;
}
.map-link:hover { color: #8ea2ff; border-color: #8ea2ff; }

.action-area {
  margin-top: auto;
  padding-top: 20px;
}

.book-btn {
  display: inline-block;
  width: 100%;
  text-align: center;
  background: #646cff;
  color: white;
  padding: 14px;
  border-radius: 12px;
  text-decoration: none;
  font-weight: 600;
  transition: background 0.3s;
  box-sizing: border-box;
}

.book-btn:hover {
  background: #535bf2;
  box-shadow: 0 0 15px rgba(100, 108, 255, 0.5);
}

.state-msg {
  text-align: center;
  font-size: 1.2rem;
  padding: 40px;
}
.error { color: #ff8c8c; border-color: rgba(255, 70, 70, 0.3); }

@media (max-width: 600px) {
  .place-page { padding: 20px 15px; }
  .card-header h1 { font-size: 1.8rem; }
  .image-wrapper { height: 200px; }
}
</style>