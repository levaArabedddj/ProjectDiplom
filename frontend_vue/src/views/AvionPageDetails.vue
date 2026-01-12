<template>
  <div class="page-container">
    <div class="glass-card detail-container">
      <button class="back-btn" @click="$router.back()">← Назад до результатів</button>

      <div v-if="loading" class="loader-container">
        <div class="loader"></div>
        <p>Шукаємо деталі рейсу...</p>
      </div>

      <div v-else-if="detailedFlight">
        <h1 class="page-title">Деталі перельоту</h1>

        <div class="detail-section">
          <h3>Маршрут та пересадки</h3>
          <div v-for="(itin, i) in detailedFlight.itineraries" :key="i" class="itinerary-box">
            <div class="duration-badge">В дорозі: {{ formatDuration(itin.duration) }}</div>

            <div v-for="(seg, j) in itin.segments" :key="j" class="segment-line">
              <div class="seg-info">
                <span class="time">{{ formatTime(seg.departure.at) }}</span>
                <strong>{{ seg.departure.iataCode }}</strong>
                <span class="arrow">→</span>
                <span class="time">{{ formatTime(seg.arrival.at) }}</span>
                <strong>{{ seg.arrival.iataCode }}</strong>
              </div>
              <div class="seg-meta">
                Авіакомпанія: {{ seg.carrierCode }} | Рейс: {{ seg.number }} | Літак: {{ seg.aircraft.code }}
              </div>
            </div>
          </div>
        </div>

        <div class="detail-section price-section">
          <div class="price-info">
            <span class="label">Загальна вартість:</span>
            <span class="total-price">{{ detailedFlight.price.grandTotal }} {{ detailedFlight.price.currency }}</span>
          </div>
          <p class="seats-info">Доступно місць: {{ detailedFlight.numberOfBookableSeats }}</p>
        </div>

        <button class="action-btn book-btn" @click="handleBooking">
          Забронювати квиток
        </button>
      </div>

      <div v-else class="error-msg">
        Не вдалося завантажити дані. Спробуйте виконати пошук заново.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from "@/api/axios.js";
import { useRouter } from 'vue-router';
const detailedFlight = ref(null);
const loading = ref(true);
const router = useRouter();
function formatTime(iso) {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function formatDuration(pt) {
  if (!pt) return '';
  const re = /^PT(?:(\d+)H)?(?:(\d+)M)?$/;
  const m = pt.match(re);
  if (!m) return pt;
  return `${m[1] ? m[1] + 'г ' : ''}${m[2] ? m[2] + 'хв' : ''}`;
}

onMounted(async () => {
  const state = window.history.state;
  const flightId = state?.selectedId;

  if (flightId) {
    try {
      loading.value = true;
      const response = await api.get(`/api/flights/get-details/${flightId}`);

      detailedFlight.value = response.data;
    } catch (e) {
      console.error("Помилка завантаження деталей из кеша:", e);
      detailedFlight.value = null;
    } finally {
      loading.value = false;
    }
  } else {
    console.warn("ID рейсу не знайдено в state");
    loading.value = false;
  }
});



function handleBooking() {
  router.push({
    name: 'BookingPage',
    params: { id: detailedFlight.value.id }
  });
}


</script>
<style scoped>

.page-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
  padding: 40px 20px;
  font-family: "Inter", sans-serif;
  color: white;
  display: flex;
  justify-content: center;
}

/* Эффект стекла */
.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  padding: 40px;
  width: 100%;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
}

.detail-container {
  max-width: 700px !important;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 800;
  margin-bottom: 2rem;
  text-align: center;
  background: linear-gradient(to right, #ffffff, #4facfe);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: #4facfe;
}

h3 {
  font-size: 1.2rem;
  opacity: 0.9;
  margin-bottom: 1.5rem;
  letter-spacing: 0.5px;
  color: white;
}

.itinerary-box {
  background: rgba(255, 255, 255, 0.05);
  padding: 24px;
  border-radius: 20px;
  margin: 20px 0;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.duration-badge {
  display: inline-block;
  background: rgba(79, 172, 254, 0.15);
  color: #4facfe;
  padding: 6px 14px;
  border-radius: 30px;
  font-size: 0.9rem;
  font-weight: 600;
  margin-bottom: 20px;
}

.segment-line {
  padding: 20px 0 20px 40px;
  border-left: 2px dashed rgba(79, 172, 254, 0.4);
  margin-left: 10px;
  position: relative;
}

.segment-line::before {
  content: '';
  position: absolute;
  left: -7px;
  top: 24px;
  width: 12px;
  height: 12px;
  background: #4facfe;
  border: 3px solid #203a43;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(79, 172, 254, 0.8);
}

.seg-info {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  font-size: 1.25rem;
  margin-bottom: 8px;
}

.time {
  font-weight: 700;
  color: #fff;
}

.seg-info strong {
  color: #4facfe;
  letter-spacing: 1px;
}

.arrow {
  opacity: 0.5;
  font-size: 1rem;
  color: white;
}

.seg-meta {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 400;
}

.price-section {
  margin-top: 2rem;
  padding: 30px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 20px;
  border: 1px solid rgba(79, 172, 254, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 15px;
  margin-bottom: 10px;
}

.price-info .label {
  font-size: 1.1rem;
  opacity: 0.8;
  color: white;
}

.total-price {
  font-size: 2.5rem;
  font-weight: 800;
  color: #4facfe;
  text-shadow: 0 0 20px rgba(79, 172, 254, 0.3);
}

.seats-info {
  font-size: 0.9rem;
  color: #52e080;
  font-weight: 500;
}

.book-btn {
  width: 100%;
  margin-top: 25px;
  height: 60px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border-radius: 16px;
  border: none;
  color: #000;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 1px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 20px rgba(79, 172, 254, 0.3);
}

.book-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 30px rgba(79, 172, 254, 0.5);
  filter: brightness(1.1);
}

.back-btn {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  padding: 10px 20px;
  border-radius: 12px;
  cursor: pointer;
  margin-bottom: 30px;
  font-size: 0.9rem;
  transition: all 0.2s;
  display: inline-block;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(-5px);
}

.loader-container {
  text-align: center;
  padding: 40px;
  color: white;
}

.loader {
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-left-color: #4facfe;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-msg {
  color: #ff6b6b;
  text-align: center;
  font-size: 1.2rem;
  background: rgba(255, 0, 0, 0.1);
  padding: 20px;
  border-radius: 12px;
}
</style>