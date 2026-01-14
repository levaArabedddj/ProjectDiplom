<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="glass-card modal-content">
      <div class="modal-header">
        <h3>🎫 Ваші квитки</h3>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>

      <div v-if="loading" class="loading-state">Завантаження квитків...</div>

      <div v-else-if="bookings.length === 0" class="empty-state">
        <p>У вас поки немає куплених квитків.</p>
      </div>

      <ul v-else class="bookings-list">
        <li v-for="ticket in bookings" :key="ticket.id" class="booking-item">
          <div class="ticket-info">
            <div class="pnr">Ref: {{ ticket.pnrReference }}</div>
            <div class="date">{{ formatDate(ticket.createdAt) }}</div> <div class="price">{{ ticket.totalPrice }} {{ ticket.currency }}</div>
          </div>

          <button class="btn-add" @click="selectTicket(ticket)">
            Додати у подорож
          </button>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios';

const emit = defineEmits(['close', 'add']);
const bookings = ref([]);
const loading = ref(false);

async function fetchBookings() {
  loading.value = true;
  try {
    // Використовуємо ваш існуючий ендпоінт
    const res = await api.get('/api/flights/my-bookings');
    bookings.value = res.data || [];
  } catch (e) {
    console.error("Помилка завантаження квитків", e);
  } finally {
    loading.value = false;
  }
}

function selectTicket(ticket) {
  emit('add', ticket);
}

function formatDate(dateStr) {
  if(!dateStr) return '';
  return new Date(dateStr).toLocaleDateString('uk-UA');
}

onMounted(() => {
  fetchBookings();
});
</script>

<style scoped>
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); z-index: 1000; display: flex; justify-content: center; align-items: center; backdrop-filter: blur(5px); }
.modal-content { width: 100%; max-width: 500px; padding: 25px; background: #1a2634; border: 1px solid rgba(255,255,255,0.1); border-radius: 16px; color: white; max-height: 80vh; overflow-y: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.close-btn { background: none; border: none; color: white; font-size: 24px; cursor: pointer; }

.bookings-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 10px; }
.booking-item { display: flex; justify-content: space-between; align-items: center; background: rgba(255,255,255,0.05); padding: 15px; border-radius: 10px; border: 1px solid rgba(255,255,255,0.05); }
.ticket-info { display: flex; flex-direction: column; gap: 4px; }
.pnr { font-weight: bold; color: #4facfe; }
.date { font-size: 12px; opacity: 0.7; }
.price { font-weight: bold; }

.btn-add { background: linear-gradient(90deg, #4facfe, #00f2fe); border: none; padding: 8px 16px; border-radius: 8px; color: #012; font-weight: bold; cursor: pointer; transition: 0.2s; }
.btn-add:hover { transform: scale(1.05); }
.loading-state, .empty-state { text-align: center; padding: 20px; opacity: 0.7; }
</style>