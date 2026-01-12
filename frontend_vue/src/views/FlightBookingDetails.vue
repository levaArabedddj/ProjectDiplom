<template>
  <div class="page-container">
    <div class="glass-card detail-container">
      <button class="back-btn" @click="$router.back()">← Назад до списку</button>

      <div v-if="loading" class="loader-container">
        <div class="loader"></div>
      </div>

      <div v-else-if="details && details.data" class="content">

        <div class="header-section">
          <h1>Деталі бронювання</h1>
          <div class="pnr-badge">
            <span class="label">PNR Код:</span>
            <span class="code">{{ getPnr(details) }}</span>
          </div>
        </div>

        <div class="section flight-info">
          <h3>✈️ Маршрут</h3>
          <div v-for="(segment, idx) in getSegments(details)" :key="idx" class="segment-card">
            <div class="segment-row">
              <div class="place">
                <div class="code">{{ segment.departure.iataCode }}</div>
                <div class="time">{{ formatTime(segment.departure.at) }}</div>
                <div class="date">{{ formatDate(segment.departure.at) }}</div>
              </div>

              <div class="flight-visual">
                <span class="duration">{{ formatDuration(segment.duration) }}</span>
                <div class="line"></div>
                <span class="airline">{{ segment.carrierCode }} {{ segment.number }}</span>
              </div>

              <div class="place text-right">
                <div class="code">{{ segment.arrival.iataCode }}</div>
                <div class="time">{{ formatTime(segment.arrival.at) }}</div>
                <div class="date">{{ formatDate(segment.arrival.at) }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="section travelers">
          <h3>👤 Пасажири</h3>
          <div v-for="traveler in details.data.travelers" :key="traveler.id" class="info-card">
            <div class="traveler-name">
              {{ traveler.name.firstName }} {{ traveler.name.lastName }}
            </div>
            <div class="traveler-meta">
              {{ traveler.gender }} • {{ traveler.dateOfBirth }}
            </div>
            <div class="contact-info" v-if="traveler.contact">
              <div>📧 {{ traveler.contact.emailAddress }}</div>
              <div>📱 +{{ traveler.contact.phones[0].countryCallingCode }}{{ traveler.contact.phones[0].number }}</div>
            </div>
          </div>
        </div>

        <div class="section payment">
          <h3>💰 Оплата</h3>
          <div class="payment-card">
            <div class="row">
              <span>Базовий тариф:</span>
              <span>{{ getPrice(details).base }} {{ getPrice(details).currency }}</span>
            </div>
            <div class="row total">
              <span>Всього сплачено:</span>
              <span class="grand-total">{{ getPrice(details).total }} {{ getPrice(details).currency }}</span>
            </div>
          </div>
        </div>

      </div>

      <div v-else class="error-msg">
        Не вдалося завантажити деталі рейсу.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from "@/api/axios.js";

const route = useRoute();
const details = ref(null);
const loading = ref(true);

onMounted(async () => {
  const id = route.params.id;
  try {
    const response = await api.get(`/api/flights/my-bookings/${id}`);
    details.value = response.data;
  } catch (e) {
    console.error("Error fetching details:", e);
  } finally {
    loading.value = false;
  }
});


function getPnr(json) {
  const records = json.data.associatedRecords;
  return records ? records[0].reference : 'N/A';
}

function getSegments(json) {

  try {
    return json.data.flightOffers[0].itineraries[0].segments;
  } catch (e) {
    return [];
  }
}

function getPrice(json) {
  try {
    const price = json.data.flightOffers[0].price;
    return {
      total: price.grandTotal,
      base: price.base,
      currency: price.currency
    };
  } catch (e) {
    return { total: 0, base: 0, currency: '' };
  }
}


function formatTime(isoString) {
  return new Date(isoString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function formatDate(isoString) {
  return new Date(isoString).toLocaleDateString();
}

function formatDuration(ptString) {
  if (!ptString) return '';
  return ptString.replace('PT', '').toLowerCase().replace('h', 'г ').replace('m', 'хв');
}
</script>

<style scoped>
.page-container { min-height: 100vh; padding: 40px 20px; color: white; display: flex; justify-content: center; }
.detail-container { max-width: 700px; width: 100%; padding: 40px; background: rgba(255, 255, 255, 0.1); backdrop-filter: blur(15px); border-radius: 24px; }
.back-btn { background: none; border: none; color: #ccc; cursor: pointer; margin-bottom: 20px; }

.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 20px; }
.pnr-badge { background: #4facfe; padding: 10px 20px; border-radius: 12px; color: #000; font-weight: bold; }
.pnr-badge .code { font-size: 1.2rem; margin-left: 10px; font-family: monospace; }

h3 { color: #4facfe; margin-bottom: 15px; font-size: 1.1rem; }
.section { margin-bottom: 30px; }

.segment-card { background: rgba(0,0,0,0.2); padding: 20px; border-radius: 16px; margin-bottom: 10px; }
.segment-row { display: flex; justify-content: space-between; align-items: center; }
.place .code { font-size: 2rem; font-weight: bold; }
.place .time { font-size: 1.2rem; color: #fff; }
.place .date { font-size: 0.8rem; color: #aaa; }
.text-right { text-align: right; }

.flight-visual { flex: 1; text-align: center; padding: 0 20px; }
.flight-visual .line { height: 2px; background: rgba(255,255,255,0.3); margin: 5px 0; position: relative; }
.flight-visual .line::after {content: '✈';position: absolute;right: 0;top: -11px;}.duration { font-size: 0.8rem; color: #4facfe; }

.info-card { background: rgba(255,255,255,0.05); padding: 15px; border-radius: 12px; margin-bottom: 10px; }
.traveler-name { font-size: 1.2rem; font-weight: bold; }
.traveler-meta { font-size: 0.9rem; color: #ccc; margin-bottom: 10px; }
.contact-info { border-top: 1px solid rgba(255,255,255,0.1); padding-top: 10px; font-size: 0.9rem; }


.payment-card { background: rgba(46, 204, 113, 0.1); padding: 20px; border-radius: 16px; border: 1px solid rgba(46, 204, 113, 0.3); }
.row { display: flex; justify-content: space-between; margin-bottom: 5px; }
.row.total { font-size: 1.5rem; font-weight: bold; color: #2ecc71; margin-top: 10px; padding-top: 10px; border-top: 1px dashed rgba(46, 204, 113, 0.3); }

.loader-container { text-align: center; padding: 50px; }
.loader { border: 4px solid rgba(255,255,255,0.1); border-left-color: #4facfe; width: 40px; height: 40px; border-radius: 50%; animation: spin 1s linear infinite; margin: 0 auto; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.segment-row {
  align-items: center;
}
.flight-visual {
  min-width: 120px;
}

</style>