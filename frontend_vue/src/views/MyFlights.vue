<template>
  <div class="page-container">
    <div class="glass-card list-container">
      <button class="back-btn" @click="$router.back()">← Назад</button>
      <h1 class="page-title">Історія бронювань</h1>

      <div v-if="loading" class="loader-container">
        <div class="loader"></div>
      </div>

      <div v-else-if="bookings.length" class="bookings-list">
        <div
            v-for="booking in bookings"
            :key="booking.id"
            class="booking-card"
            @click="goToDetails(booking.id)"
        >
          <div class="card-header">
            <span class="pnr">PNR: {{ booking.pnrReference }}</span>
            <span :class="['status', booking.status.toLowerCase()]">{{ booking.status }}</span>
          </div>

          <div class="card-body">
            <div class="date">
              📅 {{ new Date(booking.createdAt).toLocaleDateString() }}
            </div>
            <div class="price">
              {{ booking.totalPrice }} {{ booking.currency }}
            </div>
          </div>

          <div class="card-footer">
            Натисніть для деталей →
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>У вас поки немає бронювань.</p>
        <button class="action-btn" @click="$router.push('/search')">Знайти рейс</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from "@/api/axios.js";

const router = useRouter();
const bookings = ref([]);
const loading = ref(true);

onMounted(async () => {
  try {
    const response = await api.get('/api/flights/my-bookings');
    bookings.value = response.data;
  } catch (e) {
    console.error("Помилка завантаження бронювань:", e);
  } finally {
    loading.value = false;
  }
});

function goToDetails(id) {
  router.push(`/my-flights/${id}`);
}
</script>

<style scoped>
.page-container { min-height: 100vh; padding: 40px 20px; color: white; display: flex; justify-content: center; }
.glass-card { background: rgba(255, 255, 255, 0.1); backdrop-filter: blur(15px); border-radius: 24px; padding: 40px; width: 100%; max-width: 800px; }
.list-container { max-width: 600px; }
.page-title { text-align: center; margin-bottom: 30px; }
.back-btn { background: none; border: none; color: white; cursor: pointer; font-size: 16px; margin-bottom: 20px; }

.booking-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: transform 0.2s, background 0.2s;
}
.booking-card:hover { transform: translateY(-3px); background: rgba(255, 255, 255, 0.1); }

.card-header { display: flex; justify-content: space-between; margin-bottom: 15px; font-weight: bold; }
.pnr { font-family: monospace; font-size: 1.2rem; color: #4facfe; letter-spacing: 1px; }
.status { padding: 4px 10px; border-radius: 12px; font-size: 0.8rem; }
.status.confirmed { background: rgba(46, 204, 113, 0.2); color: #2ecc71; }

.card-body { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.price { font-size: 1.5rem; font-weight: bold; }

.card-footer { font-size: 0.9rem; color: rgba(255,255,255,0.5); text-align: right; }

.loader-container { text-align: center; padding: 20px; }
.loader { border: 4px solid #f3f3f3; border-top: 4px solid #4facfe; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite; margin: 0 auto; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>