<template>
  <div class="overlay" @click.self="close">
    <div class="modal glass-card">
      <h2>Створити подорож ✈️</h2>

      <form @submit.prevent="submit">
        <div class="city-input-container">
          <label>
            Місто (Latin only) *
            <input
                v-model="cityName"
                type="text"
                placeholder="Наприклад: Paris"
                required
                @input="onCityInput"
                autocomplete="off"
            />
          </label>

          <ul v-if="suggestions.length > 0" class="suggestions-list">
            <li
                v-for="city in suggestions"
                :key="city.id"
                @click="selectCity(city)"
            >
              {{ city.name }}, {{ city.country }}
            </li>
          </ul>
        </div>

        <label>
          Дата початку *
          <input v-model="startDate" type="date" required />
        </label>

        <label>
          Дата завершення
          <input v-model="endDate" type="date" />
        </label>

        <label>
          Бюджет
          <input
              v-model="balance"
              type="number"
              step="0.01"
              placeholder="Необовʼязково"
          />
        </label>

        <label>
          Валюта
          <select v-model="currency">
            <option value="">—</option>
            <option value="EUR">EUR</option>
            <option value="USD">USD</option>
            <option value="UAH">UAH</option>
          </select>
        </label>

        <div class="actions">
          <button type="button" class="btn-secondary" @click="close">
            Скасувати
          </button>
          <button class="btn-main" :disabled="loading">
            {{ loading ? 'Створення...' : 'Створити' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/api/axios'
import { useRouter } from 'vue-router'

const emit = defineEmits(['close'])
const router = useRouter()

const cityName = ref('')
const startDate = ref('')
const endDate = ref('')
const balance = ref('')
const currency = ref('')
const loading = ref(false)

// Состояния для подсказок
const suggestions = ref([])
const debounceTimer = ref(null)

// 1. Запрет кириллицы
function onCityInput() {
  // Удаляем любые символы, кроме латиницы, пробелов и дефисов
  cityName.value = cityName.value.replace(/[^a-zA-Z\s-]/g, '')

  // Логика подсказок
  clearTimeout(debounceTimer.value)
  if (cityName.value.length < 3) {
    suggestions.value = []
    return
  }

  // Дебаунс, чтобы не спамить API при каждой букве
  debounceTimer.value = setTimeout(fetchCitySuggestions, 500)
}

// 2. Поиск городов (используем открытое API Nominatim)
async function fetchCitySuggestions() {
  try {
    const query = cityName.value
    const response = await fetch(`https://nominatim.openstreetmap.org/search?city=${query}&format=json&addressdetails=1&limit=5&accept-language=en`)
    const data = await response.json()

    suggestions.value = data.map(item => ({
      id: item.place_id,
      name: item.address.city || item.address.town || item.display_name.split(',')[0],
      country: item.address.country
    }))
  } catch (e) {
    console.error('Suggestions error:', e)
  }
}

function selectCity(city) {
  cityName.value = city.name
  suggestions.value = []
}

async function submit() {
  loading.value = true
  try {
    const payload = {
      cityName: cityName.value,
      startDate: startDate.value,
      endDate: endDate.value || null,
      balance: balance.value || null,
      currency: currency.value || null
    }

    await api.post('/api/trips/create', payload)
    close()
    router.push('/travel')
  } catch (error) {
    console.error('Create error:', error)
  } finally {
    loading.value = false
  }
}

function close() {
  emit('close')
}
</script>
<style scoped>
/* Основний оверлей */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

/* Модальне вікно */
.modal {
  width: 100%;
  max-width: 420px;
  background: #1e293b; /* Темно-синій фон */
  border-radius: 24px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
  color: white;
}

.modal h2 {
  margin-top: 0;
  text-align: center;
  margin-bottom: 24px;
  font-size: 1.5rem;
}

/* Стилізація лейблів */
label {
  display: block;
  margin-bottom: 16px;
  font-size: 0.9rem;
  color: #cbd5e1; /* Світло-сірий */
  font-weight: 500;
}

/* Поля вводу (темна тема) */
input, select {
  display: block;
  width: 100%;
  margin-top: 8px;
  padding: 12px 16px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(0, 0, 0, 0.3); /* Темний напівпрозорий */
  color: white;
  outline: none;
  font-size: 1rem;
  transition: border-color 0.2s;

  /* Магія для іконки календаря в темній темі */
  color-scheme: dark;
}

input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

input:focus, select:focus {
  border-color: #646cff;
  background: rgba(0, 0, 0, 0.5);
}

/* Контейнер для інпуту міста */
.city-input-container {
  position: relative;
}

/* Список підказок (темна тема) */
.suggestions-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #1e293b; /* Такий же колір як у модалки */
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  margin-top: 5px;
  padding: 0;
  list-style: none;
  z-index: 20;
  box-shadow: 0 10px 25px rgba(0,0,0,0.5);
  max-height: 200px;
  overflow-y: auto;
}

.suggestions-list li {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 0.9rem;
  color: #e2e8f0;
}

.suggestions-list li:last-child {
  border-bottom: none;
}

.suggestions-list li:hover {
  background: rgba(255, 255, 255, 0.1); /* Підсвітка при наведенні */
  color: #fff;
}

/* Кнопки */
.actions {
  display: flex;
  gap: 16px;
  margin-top: 32px;
}

.btn-main {
  flex: 1;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  border: none;
  padding: 12px;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}
.btn-main:hover {
  opacity: 0.9;
}
.btn-main:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  flex: 1;
  background: transparent;
  color: #cbd5e1;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}
</style>