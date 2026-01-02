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
/* Твои базовые стили остаются, добавляем только для подсказок */

.city-input-container {
  position: relative; /* Чтобы список был привязан к инпуту */
}

.suggestions-list {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  color: #0f2027;
  border-radius: 12px;
  margin-top: 5px;
  padding: 0;
  list-style: none;
  z-index: 10;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
  max-height: 200px;
  overflow-y: auto;
}

.suggestions-list li {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #eee;
  font-size: 0.9rem;
}

.suggestions-list li:last-child {
  border-bottom: none;
}

.suggestions-list li:hover {
  background: #f0f0ff;
  color: #646cff;
}

/* Остальные стили из твоего кода... */
.overlay { position: fixed; inset: 0; background: rgba(0, 0, 0, 0.6); display: flex; align-items: center; justify-content: center; z-index: 1000; backdrop-filter: blur(4px); }
.modal { width: 100%; max-width: 420px; background: #1e293b; border-radius: 24px; padding: 32px; border: 1px solid rgba(255, 255, 255, 0.2); }
input, select { margin-top: 8px; padding: 12px 16px; border-radius: 12px; border: 1px solid rgba(255,255,255,0.1); background: white; color: #0f2027; outline: none; width: 100%; box-sizing: border-box; }
.actions { display: flex; gap: 16px; margin-top: 24px; }
.btn-main { flex: 1; background: #646cff; color: white; border: none; padding: 12px; border-radius: 12px; font-weight: 600; cursor: pointer; }
.btn-secondary { flex: 1; background: transparent; color: white; border: 1px solid rgba(255,255,255,0.3); padding: 12px; border-radius: 12px; cursor: pointer; }
</style>