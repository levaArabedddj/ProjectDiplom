<template>
  <div class="page-container">
    <div class="glass-card container" @click.stop>
      <div class="header-row">
        <button class="back-btn" @click="$router ? $router.back() : window.history.back()">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><path d="M12 19l-7-7 7-7"/></svg>
          Назад
        </button>
        <h1 class="page-title">Пошук рейсів</h1>
        <div class="spacer"></div> </div>

      <div class="search-box">
        <div class="search-row route-row">
          <div class="input-group">
            <label>Звідки</label>
            <div class="input-wrapper">
              <span class="icon">🛫</span>
              <input
                  v-model="origin"
                  placeholder="Nice (NCE)"
                  class="glass-input"
              />
            </div>
          </div>

          <button class="swap-btn" @click="swap" title="Поміняти місцями">
            <span style="font-size: 24px; line-height: 1;">⇄</span>
          </button>

          <div class="input-group">
            <label>Куди</label>
            <div class="input-wrapper">
              <span class="icon">🛬</span>
              <input
                  v-model="destination"
                  placeholder="Warsaw (WAW)"
                  class="glass-input"
              />
            </div>
          </div>
        </div>

        <div class="search-row details-row">

          <div class="input-group date-group">
            <label>Дата вильоту</label>
            <div class="input-wrapper">
              <input
                  v-model="departureDate"
                  type="date"
                  class="glass-input date-input"
              />
            </div>
          </div>

          <div class="input-group pax-group" ref="passengersRef">
            <label>Пасажири</label>
            <div class="input-wrapper clickable" @click="togglePassengers">
              <span class="icon">👤</span>
              <span class="pax-text">{{ passengersSummary }}</span>
              <span class="chevron">▼</span>
            </div>

            <transition name="fade">
              <div v-if="passengersOpen" class="passengers-popup" @click.stop>
                <div class="pax-row">
                  <div class="pax-info">
                    <span class="pax-type">Дорослі</span>
                    <span class="pax-age">12+ років</span>
                  </div>
                  <div class="counter">
                    <button @click="changeCount('adults', -1)" :disabled="passengers.adults <= 1">−</button>
                    <span class="count-num">{{ passengers.adults }}</span>
                    <button @click="changeCount('adults', 1)">+</button>
                  </div>
                </div>
                <div class="pax-row">
                  <div class="pax-info">
                    <span class="pax-type">Діти</span>
                    <span class="pax-age">2-11 років</span>
                  </div>
                  <div class="counter">
                    <button @click="changeCount('children', -1)" :disabled="passengers.children <= 0">−</button>
                    <span class="count-num">{{ passengers.children }}</span>
                    <button @click="changeCount('children', 1)">+</button>
                  </div>
                </div>
                <div class="pax-row">
                  <div class="pax-info">
                    <span class="pax-type">Немовлята</span>
                    <span class="pax-age">0-2 роки</span>
                  </div>
                  <div class="counter">
                    <button @click="changeCount('infants', -1)" :disabled="passengers.infants <= 0">−</button>
                    <span class="count-num">{{ passengers.infants }}</span>
                    <button @click="changeCount('infants', 1)">+</button>
                  </div>
                </div>
                <div class="popup-footer">
                  <button class="done-btn" @click="passengersOpen = false">Готово</button>
                </div>
              </div>
            </transition>
          </div>

          <div class="action-block">
            <label class="checkbox-container">
              <input type="checkbox" v-model="nonStop">
              <span class="checkmark"></span>
              Тільки прямі
            </label>
            <button @click="search" :disabled="loading" class="search-btn">
              {{ loading ? 'Шукаємо...' : 'Знайти квитки' }}
            </button>
          </div>
        </div>
      </div>

      <transition name="slide-fade">
        <div v-if="error" class="error-msg">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
          {{ error }}
        </div>
      </transition>

      <section v-if="flights.length" class="results-section">
        <h2 class="section-title">Результати пошуку</h2>
        <div class="cards-list">
          <div
              v-for="(f, idx) in flights"
              :key="idx"
              class="flight-card"
              @click="goToDetails(f)"
          >
            <div class="card-left">
              <div class="airline-info">
                <span class="airline-name">{{ f.airline }}</span>
                <span class="flight-num">{{ f.flightNumber }}</span>
              </div>
              <div class="route-visual">
                <div class="time-place">
                  <span class="time">{{ formatDateTime(f.departureTime) }}</span>
                  <span class="code">{{ f.departureAirport }}</span>
                </div>
                <div class="duration-line">
                  <span class="dot"></span>
                  <span class="line"></span>
                  <span class="dur-text">{{ formatDuration(f.duration) }}</span>
                  <span class="plane-icon">✈</span>
                  <span class="line"></span>
                  <span class="dot"></span>
                </div>
                <div class="time-place right">
                  <span class="time">{{ formatDateTime(f.arrivalTime) }}</span>
                  <span class="code">{{ f.arrivalAirport }}</span>
                </div>
              </div>
            </div>

            <div class="card-right">
              <div class="price">{{ f.price }} €</div>
              <button class="select-btn">Вибрати</button>
            </div>
          </div>
        </div>
      </section>

      <div v-if="!loading && hasSearched && !flights.length" class="empty-state">
        <div class="empty-icon">😔</div>
        <h3>Ми не знайшли рейсів</h3>
        <p>Спробуйте змінити дати або прибрати фільтр "Тільки прямі".</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import api from "@/api/axios.js";
import { useRouter } from 'vue-router';

const router = useRouter();

// Данные формы
const origin = ref('');
const destination = ref('');
const departureDate = ref('');
const nonStop = ref(false);
const passengers = ref({ adults: 1, children: 0, infants: 0 });

//  UI
const passengersOpen = ref(false);
const passengersRef = ref(null);
const loading = ref(false);
const error = ref(null);
const flights = ref([]);
const hasSearched = ref(false);

const passengersSummary = computed(() => {
  const p = passengers.value;
  const total = p.adults + p.children + p.infants;
  return `${total} пасажир${total > 1 && total < 5 ? 'и' : (total > 4 ? 'ів' : '')}`;
});

const CACHE_KEY = 'flight_search_state';
const CACHE_TTL = 10 * 60 * 1000; // 10 минут

function saveState() {
  const state = {
    origin: origin.value,
    destination: destination.value,
    departureDate: departureDate.value,
    passengers: passengers.value,
    nonStop: nonStop.value,
    flights: flights.value,
    hasSearched: hasSearched.value,
    timestamp: Date.now() // Запоминаем время сохранения
  };
  sessionStorage.setItem(CACHE_KEY, JSON.stringify(state));
}

function restoreState() {
  const saved = sessionStorage.getItem(CACHE_KEY);
  if (!saved) return;

  try {
    const data = JSON.parse(saved);
    const now = Date.now();

    // Если прошло меньше 10 минут — восстанавливаем
    if (now - data.timestamp < CACHE_TTL) {
      origin.value = data.origin || '';
      destination.value = data.destination || '';
      departureDate.value = data.departureDate || '';
      passengers.value = data.passengers || { adults: 1, children: 0, infants: 0 };
      nonStop.value = data.nonStop || false;
      flights.value = data.flights || [];
      hasSearched.value = data.hasSearched || false;
    } else {
      sessionStorage.removeItem(CACHE_KEY);
    }
  } catch (e) {
    console.error('Ошибка восстановления состояния', e);
  }
}

function swap() {
  const t = origin.value;
  origin.value = destination.value;
  destination.value = t;
}

function togglePassengers() {
  passengersOpen.value = !passengersOpen.value;
}

function changeCount(type, delta) {
  const min = type === 'adults' ? 1 : 0;
  const next = Math.max(min, (passengers.value[type] || 0) + delta);
  passengers.value[type] = next;
}

async function search() {
  error.value = null;
  hasSearched.value = true;

  if (!origin.value || !destination.value || !departureDate.value) {
    error.value = 'Будь ласка, заповніть всі поля (Звідки, Куди, Дата).';
    return;
  }

  loading.value = true;
  try {
    const body = {
      origin: origin.value,
      destination: destination.value,
      departureDate: departureDate.value,
      adults: Number(passengers.value.adults) || 1,
      children: Number(passengers.value.children) || 0,
      infants: Number(passengers.value.infants) || 0,
      nonStop: Boolean(nonStop.value)
    };

    const response = await api.post('/api/flights/search', body);
    const data = response.data || response;

    flights.value = Array.isArray(data) ? data : (data.data || []);

    saveState();

  } catch (e) {
    console.error(e);
    error.value = e.response?.data?.message || 'Не вдалося виконати пошук. Спробуйте пізніше.';
  } finally {
    loading.value = false;
  }
}

function goToDetails(flight) {
  saveState();

  router.push({
    name: 'FlightDetails',
    state: { selectedId: flight.id }
  });
}

function formatDateTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function formatDuration(pt) {
  if (!pt) return '';
  const re = /^PT(?:(\d+)H)?(?:(\d+)M)?$/;
  const m = pt.match(re);
  if (!m) return pt;
  const hours = m[1] ? `${m[1]}г` : '';
  const mins = m[2] ? `${m[2]}хв` : '';
  return [hours, mins].filter(Boolean).join(' ');
}

function handleClickOutside(e) {
  if (!passengersRef.value) return;
  if (!passengersRef.value.contains(e.target)) {
    passengersOpen.value = false;
  }
}

onMounted(() => {
  window.addEventListener('click', handleClickOutside);
  restoreState();
});

onBeforeUnmount(() => {
  window.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
  padding: 40px 20px;
  font-family: 'Inter', system-ui, sans-serif;
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.container {
  width: 100%;
  max-width: 950px;
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

/* HEADER */
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30px;
}
.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  font-size: 16px;
  transition: color 0.2s;
}
.back-btn:hover { color: #fff; }
.page-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(90deg, #fff, #a5b4fc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.spacer { width: 60px; }

.search-box {
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: rgba(0, 0, 0, 0.2);
  padding: 25px;
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.05);
}

.search-row {
  display: grid;
  gap: 15px;
}

.route-row {
  grid-template-columns: 1fr auto 1fr;
  align-items: end;
}

.details-row {
  grid-template-columns: 1fr 1fr 1.2fr;
  align-items: end;
}

.input-group label {
  display: block;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
  font-weight: 500;
}

.input-wrapper {
  position: relative;
  background: rgba(255, 255, 255, 0.07);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  height: 52px;
  display: flex;
  align-items: center;
  padding: 0 15px;
  transition: all 0.2s ease;
}

.input-wrapper:hover, .input-wrapper:focus-within {
  background: rgba(255, 255, 255, 0.12);
  border-color: #4facfe;
  box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.2);
}

.icon { margin-right: 12px; font-size: 18px; filter: grayscale(100%) brightness(150%); opacity: 0.8; }
.glass-input {
  width: 100%;
  background: transparent;
  border: none;
  color: #fff;
  font-size: 16px;
  outline: none;
  font-weight: 500;
}
.glass-input::placeholder { color: rgba(255, 255, 255, 0.4); }

/* Custom Date Input Icon Fix */
.date-input::-webkit-calendar-picker-indicator {
  filter: invert(1);
  opacity: 0.6;
  cursor: pointer;
}

.swap-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: #fff;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px; /* Alignment fix */
}
.swap-btn:hover {
  background: #4facfe;
  transform: rotate(180deg);
}

.clickable { cursor: pointer; justify-content: space-between; }
.chevron { font-size: 10px; opacity: 0.6; }
.passengers-popup {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  min-width: 280px;
  margin-top: 8px;
  background: #1a2634;
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.5);
  z-index: 100;
}
.pax-group { position: relative; }

.pax-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}
.pax-row:last-of-type { border-bottom: none; }
.pax-type { display: block; font-weight: 600; font-size: 15px; }
.pax-age { font-size: 12px; color: rgba(255,255,255,0.5); }
.counter { display: flex; align-items: center; gap: 10px; background: rgba(0,0,0,0.2); padding: 4px; border-radius: 8px; }
.counter button {
  width: 28px; height: 28px;
  background: rgba(255,255,255,0.1);
  border: none;
  color: white;
  border-radius: 6px;
  cursor: pointer;
}
.counter button:hover:not(:disabled) { background: #4facfe; }
.counter button:disabled { opacity: 0.3; cursor: not-allowed; }
.count-num { width: 20px; text-align: center; font-weight: bold; }
.popup-footer { margin-top: 10px; text-align: right; }
.done-btn { background: none; border: none; color: #4facfe; font-weight: bold; cursor: pointer; }

.action-block {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-btn {
  flex: 1;
  height: 52px;
  background: linear-gradient(90deg, #4facfe, #00f2fe);
  border: none;
  border-radius: 12px;
  color: #001e3c;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.search-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(79, 172, 254, 0.4);
}
.search-btn:disabled { opacity: 0.7; cursor: wait; filter: grayscale(1); }

/* Custom Checkbox */
.checkbox-container {
  display: flex;
  align-items: center;
  position: relative;
  padding-left: 28px;
  cursor: pointer;
  font-size: 14px;
  color: rgba(255,255,255,0.8);
  user-select: none;
}
.checkbox-container input { position: absolute; opacity: 0; cursor: pointer; height: 0; width: 0; }
.checkmark {
  position: absolute; top: 0; left: 0; height: 20px; width: 20px;
  background-color: rgba(255,255,255,0.1); border-radius: 6px; border: 1px solid rgba(255,255,255,0.2);
}
.checkbox-container:hover input ~ .checkmark { background-color: rgba(255,255,255,0.2); }
.checkbox-container input:checked ~ .checkmark { background-color: #4facfe; border-color: #4facfe; }
.checkmark:after { content: ""; position: absolute; display: none; }
.checkbox-container input:checked ~ .checkmark:after { display: block; }
.checkbox-container .checkmark:after {
  left: 6px; top: 2px; width: 5px; height: 10px;
  border: solid #001e3c; border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.error-msg {
  background: rgba(255, 77, 77, 0.15);
  border: 1px solid rgba(255, 77, 77, 0.3);
  color: #ff8585;
  padding: 15px;
  border-radius: 12px;
  margin-top: 20px;
  display: flex; align-items: center; gap: 10px;
}
.empty-state { text-align: center; padding: 60px 0; opacity: 0.7; }
.empty-icon { font-size: 48px; margin-bottom: 10px; display: block; }

.section-title { margin-top: 40px; margin-bottom: 20px; font-size: 20px; }
.flight-card {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  margin-bottom: 16px;
  padding: 20px 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}
.flight-card:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.2);
}

.card-left { flex: 1; display: flex; flex-direction: column; gap: 15px; }
.airline-info { display: flex; gap: 10px; font-size: 13px; opacity: 0.7; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }

.route-visual { display: flex; align-items: center; gap: 20px; }
.time-place { display: flex; flex-direction: column; }
.time { font-size: 22px; font-weight: 700; color: #fff; }
.code { font-size: 13px; color: rgba(255,255,255,0.5); font-weight: 600; }
.time-place.right { text-align: right; }

.duration-line { flex: 1; display: flex; align-items: center; position: relative; justify-content: center; gap: 5px; opacity: 0.6; }
.line { height: 1px; background: rgba(255,255,255,0.3); flex: 1; }
.dot { width: 6px; height: 6px; background: rgba(255,255,255,0.3); border-radius: 50%; }
.plane-icon { font-size: 14px; transform: rotate(90deg); }
.dur-text { font-size: 12px; font-weight: 500; margin: 0 5px; white-space: nowrap; }

.card-right { text-align: right; display: flex; flex-direction: column; align-items: flex-end; justify-content: center; gap: 8px; border-left: 1px dashed rgba(255,255,255,0.1); padding-left: 25px; margin-left: 25px; }
.price { font-size: 24px; font-weight: 800; color: #4facfe; }
.select-btn {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.2);
  color: white;
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
  transition: 0.2s;
}
.flight-card:hover .select-btn { background: white; color: #0f2027; border-color: white; }

@media (max-width: 768px) {
  .route-row { grid-template-columns: 1fr; gap: 5px; }
  .swap-btn { transform: rotate(90deg); margin: 0 auto; width: 36px; height: 36px; }
  .swap-btn:hover { transform: rotate(270deg); }

  .details-row { grid-template-columns: 1fr; }
  .action-block { flex-direction: column; align-items: stretch; margin-top: 10px; }
  .search-btn { width: 100%; }

  .flight-card { flex-direction: column; align-items: flex-start; gap: 20px; }
  .card-right {
    width: 100%; border-left: none; padding-left: 0; margin-left: 0;
    border-top: 1px dashed rgba(255,255,255,0.1); padding-top: 15px;
    flex-direction: row; justify-content: space-between; align-items: center;
  }
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>