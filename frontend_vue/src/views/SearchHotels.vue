<template>
  <div class="page-container">
    <div class="glass-card search-container">
      <button class="back-btn" @click="$router.back()">← Назад</button>
      <h1 class="page-title">Пошук готелів</h1>

      <div class="search-form">
        <div class="input-group">
          <label>Місто (Назва або код)</label>
          <input v-model="searchQuery.cityCode" type="text" placeholder="Напр. Paris или PAR" />
        </div>

        <div class="input-group">
          <label>Дата заїзду</label>
          <input v-model="searchQuery.checkInDate" type="date" />
        </div>

        <div class="input-group">
          <label>Дата виїзду</label>
          <input v-model="searchQuery.checkOutDate" type="date" />
        </div>

        <div class="input-group">
          <label>Пасажири</label>
          <input v-model.number="searchQuery.adults" type="number" min="1" />
        </div>

        <button class="search-btn" @click="searchHotels" :disabled="loading">
          {{ loading ? 'Пошук...' : 'Шукати готелі' }}
        </button>
      </div>

      <hr class="divider" />

      <div class="results-section">
        <h2 v-if="hotels.length > 0">Знайдені готелі</h2>

        <div v-if="loading" class="loader-container">
          <div class="loader"></div>
        </div>

        <div v-else class="hotels-grid">
          <div v-for="hotel in hotels" :key="hotel.hotelId" class="hotel-card">
            <div class="hotel-info">
              <h3 class="hotel-name">{{ hotel.name }}</h3>
              <p class="city-tag">📍 {{ hotel.cityCode }}</p>
              <p class="description" v-if="hotel.description">
                {{ hotel.description.substring(0, 100) }}...
              </p>
            </div>

            <div class="price-section">
              <div v-if="hotel.price" class="price-val">{{ hotel.price }} {{ hotel.currency }}</div>
              <div v-else class="no-price">Ціна за запитом</div>

              <button :disabled="!hotel.price" class="book-btn">
                {{ hotel.price ? 'Забронювати' : 'Недоступно' }}
              </button>
            </div>
          </div>
        </div>

        <div v-if="!loading && hotels.length === 0 && searched" class="empty-state">
          Готелів не знайдено. Спробуйте інші дати.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import api from "@/api/axios.js";

const searchQuery = ref({
  cityCode: '',
  checkInDate: '',
  checkOutDate: '',
  adults: 1
});

const hotels = ref([]);
const loading = ref(false);
const searched = ref(false);

async function searchHotels() {
  if (!searchQuery.value.cityCode || !searchQuery.value.checkInDate) {
    alert("Будь ласка, заповніть основні поля");
    return;
  }

  loading.value = true;
  searched.value = true;
  try {
    const response = await api.post('/api/hotels/search-hotels', searchQuery.value);
    hotels.value = response.data;
  } catch (e) {
    console.error("Hotel search error:", e);
    alert("Помилка при пошуку готелів");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  padding: 40px 20px;
  color: white;
  display: flex;
  justify-content: center;
}

.glass-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(15px);
  border-radius: 24px;
  padding: 40px;
  width: 100%;
  max-width: 900px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.back-btn { background: none; border: none; color: #ccc; cursor: pointer; margin-bottom: 10px; }

.search-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  align-items: end;
  margin-top: 20px;
}

.input-group { display: flex; flex-direction: column; gap: 8px; }
.input-group label { font-size: 0.85rem; color: #ccc; }
.input-group input {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 12px;
  color: white;
  outline: none;
}

.search-btn {
  background: #00e5ff;
  color: #000;
  border: none;
  padding: 13px;
  border-radius: 12px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.3s;
}
.search-btn:hover { background: #00b8d4; transform: translateY(-2px); }

.divider { border: 0; border-top: 1px solid rgba(255,255,255,0.1); margin: 40px 0; }

/* Карточки отелей */
.hotels-grid {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 20px;
}

.hotel-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: 0.3s;
}
.hotel-card:hover { background: rgba(255, 255, 255, 0.08); }

.hotel-name { font-size: 1.3rem; margin-bottom: 5px; color: #00e5ff; }
.city-tag { font-size: 0.9rem; color: #aaa; margin-bottom: 10px; }
.description { font-size: 0.85rem; color: #ccc; max-width: 400px; }

.price-section { text-align: right; }
.price-val { font-size: 1.6rem; font-weight: 800; margin-bottom: 10px; }
.book-btn {
  background: #2ecc71;
  border: none;
  padding: 8px 20px;
  border-radius: 8px;
  color: white;
  cursor: pointer;
}

.loader-container { text-align: center; padding: 40px; }
.loader {
  border: 3px solid rgba(255,255,255,0.1);
  border-top: 3px solid #00e5ff;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>