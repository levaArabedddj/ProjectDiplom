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
/* Основний контейнер */
.booking-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 20px;
  font-family: 'Inter', sans-serif;
  color: #ffffff; /* Весь текст за замовчуванням білий */
}

h1 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 2rem;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

/* Стан завантаження */
.loading-state {
  text-align: center;
  margin-top: 4rem;
  background: rgba(255, 255, 255, 0.05);
  padding: 40px;
  border-radius: 16px;
  backdrop-filter: blur(5px);
}

/* Картка помилки */
.error-card {
  background: rgba(220, 53, 69, 0.2);
  border: 1px solid rgba(220, 53, 69, 0.4);
  padding: 20px;
  border-radius: 12px;
  text-align: center;
}
.back-btn {
  margin-top: 15px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.3s;
}
.back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* Міні-картка з інфо про рейс */
.flight-summary-mini {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 20px;
  border-radius: 16px;
  margin-bottom: 25px;
  backdrop-filter: blur(10px);
}
.flight-summary-mini h3 { margin-top: 0; margin-bottom: 10px; color: #a5b4fc; }
.price-tag { font-size: 1.4rem; color: #4ade80; font-weight: bold; }

/* Секції форми */
.form-section {
  background: rgba(30, 41, 59, 0.6); /* Темний напівпрозорий фон */
  padding: 25px;
  border-radius: 16px;
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.form-section h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 1.1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 10px;
  color: #e2e8f0;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
}

@media (max-width: 600px) {
  .form-row { flex-direction: column; gap: 15px; }
}

.field {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.field label {
  font-size: 0.9rem;
  margin-bottom: 6px;
  color: #94a3b8; /* Світло-сірий для підписів */
}

/* Стилізація полів вводу під темну тему */
input, select {
  padding: 12px;
  background: rgba(0, 0, 0, 0.2); /* Темний фон інпуту */
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  color: white;
  font-size: 1rem;
  transition: 0.3s;
}

input::placeholder { color: rgba(255, 255, 255, 0.3); }

input:focus, select:focus {
  outline: none;
  border-color: #6366f1;
  background: rgba(0, 0, 0, 0.4);
}

/* Select option fix for dark mode (options often stay white) */
select option {
  background: #1e293b;
  color: white;
}

/* Кнопка підтвердження */
.confirm-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: bold;
  margin-top: 10px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.confirm-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(99, 102, 241, 0.4);
}

.confirm-btn:disabled {
  background: #475569;
  cursor: not-allowed;
  opacity: 0.7;
}

/* Спіннер */
.spinner {
  border: 4px solid rgba(255,255,255,0.1);
  border-top: 4px solid #6366f1;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
</style>