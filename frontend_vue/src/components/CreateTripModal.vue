<template>
  <div class="overlay" @click.self="close">
    <div class="modal glass-card">
      <h2>Створити подорож ✈️</h2>

      <form @submit.prevent="submit">
        <!-- City -->
        <label>
          Місто *
          <input
              v-model="cityName"
              type="text"
              placeholder="Наприклад: Париж"
              required
          />
        </label>

        <!-- Start date -->
        <label>
          Дата початку *
          <input
              v-model="startDate"
              type="date"
              required
          />
        </label>

        <!-- End date -->
        <label>
          Дата завершення
          <input
              v-model="endDate"
              type="date"
          />
        </label>

        <!-- Balance -->
        <label>
          Бюджет
          <input
              v-model="balance"
              type="number"
              step="0.01"
              placeholder="Необовʼязково"
          />
        </label>

        <!-- Currency -->
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

          <button class="btn-main">
            Створити
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

function close() {
  emit('close')
}

async function submit() {
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
}
</script>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  width: 100%;
  max-width: 420px;
}

form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

label {
  display: flex;
  flex-direction: column;
  font-size: 0.9rem;
  opacity: 0.9;
}

input, select {
  margin-top: 6px;
  padding: 10px 12px;
  border-radius: 10px;
  border: none;
}

.actions {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
}

.btn-secondary {
  background: transparent;
  color: white;
  border: 1px solid rgba(255,255,255,0.3);
  padding: 10px 18px;
  border-radius: 12px;
}
</style>
