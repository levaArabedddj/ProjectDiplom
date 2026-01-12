<template>
  <div class="travel-page">
    <div class="container">
      <button @click="$router.push('/main')" class="back-btn">← Назад на головну</button>
      <h1 class="title">Мої подорожі 🌍</h1>

      <div v-if="trips.length === 0" class="glass-card empty-state">
        <p>У вас ще немає створених подорожей.</p>
        <button @click="$router.push('/home')" class="btn-main">Створити першу</button>
      </div>

      <div v-else class="trips-grid">
        <div
            v-for="trip in trips"
            :key="trip.id"
            class="trip-card glass-card"
            @click="goToTrip(trip.id)"
        >
          <button class="delete-btn" @click.stop="confirmDelete(trip)" title="Видалити подорож">
            🗑️
          </button>

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

      <div v-if="showDeleteModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal-confirm glass-card">
          <h3>Видалити подорож? 🗑️</h3>
          <p>
            Ви впевнені, що хочете видалити поїздку до <strong>{{ tripToDelete?.cityName }}</strong>?
            <br>Цю дію не можна скасувати.
          </p>
          <div class="confirm-actions">
            <button class="btn-secondary" @click="closeModal">Ні, залишити</button>
            <button class="btn-danger" @click="deleteTrip" :disabled="isDeleting">
              {{ isDeleting ? 'Видалення...' : 'Так, видалити' }}
            </button>
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

// Состояния для модального окна
const showDeleteModal = ref(false)
const tripToDelete = ref(null)
const isDeleting = ref(false)

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

// Открыть модалку
function confirmDelete(trip) {
  tripToDelete.value = trip
  showDeleteModal.value = true
}

// Закрыть модалку
function closeModal() {
  showDeleteModal.value = false
  tripToDelete.value = null
}

// Логика удаления
async function deleteTrip() {
  if (!tripToDelete.value) return

  isDeleting.value = true
  try {
    // Вызов твоего контроллера DELETE /{tripId}
    // Предполагаю, что базовый путь контроллера /api/trips
    await api.delete(`/api/trips/${tripToDelete.value.id}`)

    // Удаляем из локального списка, чтобы не перезагружать страницу
    trips.value = trips.value.filter(t => t.id !== tripToDelete.value.id)

    closeModal()
  } catch (error) {
    console.error("Ошибка при удалении:", error)
    alert("Не вдалося видалити подорож.")
  } finally {
    isDeleting.value = false
  }
}
</script>

<style scoped>
.travel-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
  padding: 60px 20px;
  color: white;
  font-family: 'Inter', sans-serif;
}
.container {
  max-width: 1400px;
  margin: 0 auto;
}
.title { text-align: center; margin-bottom: 40px; }

/* Кнопка назад */
.back-btn {
  background: rgba(255,255,255,0.1);
  border: none;
  color: white;
  padding: 10px 20px;
  border-radius: 20px;
  cursor: pointer;
  margin-bottom: 20px;
  transition: 0.3s;
}
.back-btn:hover { background: rgba(255,255,255,0.2); }

/* Grid */
.trips-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 30px;
  padding: 20px 0;
}

/* Карточка */
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
  position: relative; /* Чтобы позиционировать кнопку удаления */
}

.trip-card:hover { transform: translateY(-10px); background: rgba(255, 255, 255, 0.15); }

/* Кнопка удаления на карточке */
.delete-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(0, 0, 0, 0.2);
  border: none;
  width: 35px;
  height: 35px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  opacity: 0.7;
}

.delete-btn:hover {
  background: #ff4757; /* Красный цвет при наведении */
  opacity: 1;
  transform: scale(1.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
}
.city-icon { font-size: 32px; }
.card-header h3 { margin: 0; font-size: 1.5rem; }
.date { opacity: 0.8; margin: 15px 0; }
.card-footer { color: #646cff; font-weight: 600; text-align: right; }

/* Empty State */
.empty-state {
  text-align: center;
  padding: 40px;
  max-width: 500px;
  margin: 0 auto;
}
.btn-main {
  margin-top: 15px;
  padding: 10px 20px;
  background: #646cff;
  border: none;
  color: white;
  border-radius: 8px;
  cursor: pointer;
}

/* --- МОДАЛЬНОЕ ОКНО --- */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(5px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-confirm {
  background: #1e293b;
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 30px;
  border-radius: 20px;
  max-width: 400px;
  width: 90%;
  text-align: center;
  color: white;
  box-shadow: 0 20px 40px rgba(0,0,0,0.5);
}

.modal-confirm h3 { margin-top: 0; color: #ff6b6b; }
.modal-confirm p { opacity: 0.8; line-height: 1.5; margin-bottom: 25px; }

.confirm-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.btn-secondary {
  padding: 10px 20px;
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.2);
  background: transparent;
  color: white;
  cursor: pointer;
}
.btn-secondary:hover { background: rgba(255,255,255,0.1); }

.btn-danger {
  padding: 10px 20px;
  border-radius: 10px;
  border: none;
  background: #ff4757;
  color: white;
  font-weight: bold;
  cursor: pointer;
}
.btn-danger:hover { background: #ff6b81; }
.btn-danger:disabled { opacity: 0.7; cursor: not-allowed; }

@media (max-width: 900px) {
  .trips-grid { grid-template-columns: 1fr; }
  .trip-card { width: 100%; flex: 1; }
}
</style>