<template>
  <div class="booking-container">
    <h1>Оформлення бронювання</h1>

    <div v-if="pricingLoading" class="loading-state">
      <div class="spinner"></div>
      <p>Перевіряємо актуальність ціни та наявність місць...</p>
    </div>

    <div v-else-if="pricingError" class="error-card">
      <h2>Помилка валідації ціни</h2>
      <p>{{ pricingError }}</p>
      <button @click="$router.back()" class="back-btn">Повернутися назад</button>
    </div>

    <div v-else class="booking-content">
      <div class="flight-summary-mini" v-if="pricedOffer">
        <h3>Ваше замовлення:</h3>
        <p>Рейс: <strong>{{ pricedOffer.itineraries[0].segments[0].departure.iataCode }} → {{ pricedOffer.itineraries[0].segments.at(-1).arrival.iataCode }}</strong></p>
        <p>Фінальна ціна: <span class="price-tag">{{ pricedOffer.price.grandTotal }} {{ pricedOffer.price.currency }}</span></p>
      </div>

      <form @submit.prevent="handleFinalBooking" class="traveler-form">
        <section class="form-section">
          <h3>Дані пасажира (як у закордонному паспорті)</h3>

          <div class="form-row">
            <div class="field">
              <label>Ім'я (First Name)</label>
              <input v-model="traveler.firstName" type="text" placeholder="IVAN" required @input="traveler.firstName = traveler.firstName.toUpperCase()">
            </div>
            <div class="field">
              <label>Прізвище (Last Name)</label>
              <input v-model="traveler.lastName" type="text" placeholder="IVANOV" required @input="traveler.lastName = traveler.lastName.toUpperCase()">
            </div>
          </div>

          <div class="form-row">
            <div class="field">
              <label>Дата народження</label>
              <input v-model="traveler.dateOfBirth" type="date" required>
            </div>
            <div class="field">
              <label>Стать</label>
              <select v-model="traveler.gender" required>
                <option value="MALE">Чоловіча (MALE)</option>
                <option value="FEMALE">Жіноча (FEMALE)</option>
              </select>
            </div>
          </div>
        </section>

        <section class="form-section">
          <h3>Контактна інформація</h3>
          <div class="form-row">
            <div class="field">
              <label>Email</label>
              <input v-model="traveler.email" type="email" placeholder="email@example.com" required>
            </div>
            <div class="field">
              <label>Номер телефону (з кодом країни)</label>
              <input v-model="traveler.phone" type="text" placeholder="380991234567" required>
            </div>
          </div>
        </section>

        <button type="submit" :disabled="bookingLoading" class="confirm-btn">
          {{ bookingLoading ? 'Оформлюємо...' : 'Підтвердити та забронювати' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from "@/api/axios.js";

const router = useRouter();
const route = useRoute();

const pricingLoading = ref(true);
const bookingLoading = ref(false);
const pricingError = ref(null);
const pricedOffer = ref(null);

const traveler = ref({
  firstName: '',
  lastName: '',
  dateOfBirth: '',
  gender: 'MALE',
  email: '',
  phone: ''
});

onMounted(async () => {
  // 1. Берем ID из URL
  const flightId = route.params.id;

  if (!flightId) {
    pricingError.value = "ID рейсу не знайдено.";
    pricingLoading.value = false;
    return;
  }

  try {
    const detailResponse = await api.get(`/api/flights/get-details/${flightId}`);
    const flightOffer = detailResponse.data;

    if (!flightOffer) {
      throw new Error("Рейс не знайдено в кеші");
    }

    const pricingResponse = await api.post('/api/flights/pricing', {
      data: {
        type: 'flight-offers-pricing',
        flightOffers: [flightOffer]
      }
    });

    pricedOffer.value = pricingResponse.data.data.flightOffers[0];

  } catch (err) {
    console.error("Error flow:", err);
    if (err.response && err.response.data) {
      pricingError.value = JSON.stringify(err.response.data);
    } else {
      pricingError.value = "Не вдалося отримати дані рейсу або ціна змінилася.";
    }
  } finally {
    pricingLoading.value = false;
  }
});

async function handleFinalBooking() {
  bookingLoading.value = true;
  try {
    const bookingPayload = {
      data: {
        type: 'flight-order',
        flightOffers: [pricedOffer.value],
        travelers: [
          {
            id: "1",
            dateOfBirth: traveler.value.dateOfBirth,
            name: {
              firstName: traveler.value.firstName,
              lastName: traveler.value.lastName
            },
            gender: traveler.value.gender,
            contact: {
              emailAddress: traveler.value.email,
              phones: [
                {
                  deviceType: "MOBILE",
                  countryCallingCode: traveler.value.phone.substring(0, 2),
                  number: traveler.value.phone.substring(2)
                }
              ]
            }
          }
        ]
      }
    };

    const response = await api.post('/api/flights/book', bookingPayload);

    alert(`Успішно! Номер вашого бронювання (PNR): ${response.data.data.id}`);
    router.push('/main');
  } catch (err) {
    console.error("Booking error:", err);
    alert("Помилка при створенні бронювання. Перевірте консоль для деталей.");
  } finally {
    bookingLoading.value = false;
  }
}
</script>

<style scoped>
.booking-container { max-width: 800px; margin: 2rem auto; padding: 1rem; font-family: sans-serif; }
.loading-state { text-align: center; margin-top: 4rem; }
.form-section { background: #f9f9f9; padding: 1.5rem; border-radius: 8px; margin-bottom: 1.5rem; }
.form-row { display: flex; gap: 1rem; margin-bottom: 1rem; }
.field { flex: 1; display: flex; flex-direction: column; }
.field label { font-size: 0.9rem; margin-bottom: 0.3rem; color: #666; }
input, select { padding: 0.6rem; border: 1px solid #ccc; border-radius: 4px; }
.price-tag { font-size: 1.5rem; color: #2ecc71; font-weight: bold; }
.confirm-btn { width: 100%; padding: 1rem; background: #3498db; color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 1.1rem; }
.confirm-btn:disabled { background: #bdc3c7; }
.spinner { border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 0 auto 1rem; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>