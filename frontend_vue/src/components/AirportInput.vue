<template>
  <div class="page-container">
    <div class="glass-card container">
      <button class="back-btn" @click="$router ? $router.back() : window.history.back()">
        ← Назад
      </button>

      <h1 class="page-title">Пошук рейсів</h1>

      <div class="search-grid">
        <div class="form-group origin-destination">
          <label class="sr-only">Звідки / Куди</label>

          <div class="pair">
            <div class="input-with-badge">
              <AirportInput
                  v-model="origin"
                  label="Звідки (IATA або місто)"
                  placeholder="NCE"
                  @iataSelected="onOriginIata"
              />
            </div>

            <button class="swap-btn" @click="swap()" :aria-label="'Поменять местами ' + origin + ' и ' + destination">
              ⇄
            </button>

            <div class="input-with-badge">
              <AirportInput
                  v-model="destination"
                  label="Куди (IATA або місто)"
                  placeholder="WAW"
                  @iataSelected="onDestIata"
              />
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>Дата вильоту</label>
          <input v-model="departureDate" type="date" class="input-field" />
        </div>

        <div class="form-group small passengers-group" ref="passengersRef">
          <label>Пасажири</label>
          <button class="passengers-btn" @click="togglePassengers">
            {{ passengersSummary }}
          </button>

          <div v-if="passengersOpen" class="passengers-popup" @click.stop>
            <div class="row">
              <div>Дорослі</div>
              <div class="counter">
                <button @click="changeCount('adults', -1)">−</button>
                <span>{{ passengers.adults }}</span>
                <button @click="changeCount('adults', 1)">+</button>
              </div>
            </div>
            <div class="row">
              <div>Діти (2–11)</div>
              <div class="counter">
                <button @click="changeCount('children', -1)">−</button>
                <span>{{ passengers.children }}</span>
                <button @click="changeCount('children', 1)">+</button>
              </div>
            </div>
            <div class="row">
              <div>Немовлята</div>
              <div class="counter">
                <button @click="changeCount('infants', -1)">−</button>
                <span>{{ passengers.infants }}</span>
                <button @click="changeCount('infants', 1)">+</button>
              </div>
            </div>

            <div class="popup-actions">
              <button class="ghost" @click="resetPassengers">Скинути</button>
              <button class="primary" @click="togglePassengers">Готово</button>
            </div>
          </div>
        </div>

        <div class="form-group checkbox-group">
          <label class="pill">
            <input id="nonStop" type="checkbox" v-model="nonStop" />
            <span class="pill-label">Тільки прямі</span>
          </label>
        </div>

        <div class="button-group">
          <button @click="search" :disabled="loading" class="action-btn search-btn">
            {{ loading ? 'Пошук...' : 'Шукати' }}
          </button>
        </div>
      </div>

      <div v-if="error" class="error-msg">
        Помилка: {{ error }}
      </div>

      <section v-if="flights.length" class="results-section">
        <div class="results-header">
          <h2>Знайдені рейси</h2>

          <div class="mini-controls">
            <select v-model="sortBy" @change="applySort">
              <option value="best">Найкраще</option>
              <option value="price_asc">Ціна ↑</option>
              <option value="price_desc">Ціна ↓</option>
              <option value="duration">Час в дорозі</option>
            </select>

            <button class="small-btn" @click="toggleOnlyDirect">
              {{ nonStop ? 'Всі рейси' : 'Тільки прямі' }}
            </button>

            <button class="small-btn ghost" @click="openFilters">
              Фільтри
            </button>
          </div>
        </div>

        <div class="price-calendar" v-if="showPriceCalendar">
          <div class="pc-day" v-for="(d, idx) in priceDays" :key="idx" :class="{ active: d.date === departureDate }">
            <div class="p-day">{{ d.label }}</div>
            <div class="p-price">{{ d.price !== null ? d.price + '€' : '-' }}</div>
          </div>
        </div>

        <div class="cards-list">
          <div
              v-for="(f, idx) in visibleFlights"
              :key="idx"
              class="flight-item"
              @click="goToDetails(f)"
              :title="'Переглянути деталі: ' + f.airline + ' ' + f.flightNumber"
          >
            <div class="flight-info">
              <div class="route">
                <span class="airport">{{ f.departureAirport }}</span>
                <span class="arrow">→</span>
                <span class="airport">{{ f.arrivalAirport }}</span>
              </div>
              <div class="times">
                {{ formatDateTime(f.departureTime) }} — {{ formatDateTime(f.arrivalTime) }}
              </div>
              <div class="meta">
                {{ f.airline }} {{ f.flightNumber }} • {{ f.aircraft || '—' }}
              </div>
            </div>

            <div class="flight-price">
              <div class="price-tag">{{ f.price }} €</div>
              <div class="duration">{{ formatDuration(f.duration) }}</div>
            </div>
          </div>
        </div>
      </section>

      <div v-if="!loading && hasSearched && !flights.length" class="empty-state">
        <h3>Нічого не знайдено 😔</h3>
        <p>Спробуйте змінити дати, місто або зніміть фільтр "Тільки прямі".</p>
        <div class="suggestions">
          <button class="suggest" @click="shiftDate(-1)">Раніше</button>
          <button class="suggest" @click="shiftDate(1)">Пізніше</button>
          <button class="suggest" @click="clearFilters">Очистити фільтри</button>
        </div>
      </div>

      <div v-if="filtersOpen" class="filters-modal" @click.self="filtersOpen = false">
        <div class="filters-card" @click.stop>
          <h4>Фільтри</h4>
          <div class="filter-row">
            <label>Макс пересадок</label>
            <select v-model="maxStops">
              <option :value="0">Прямий</option>
              <option :value="1">До 1</option>
              <option :value="2">До 2</option>
              <option :value="99">Будь-які</option>
            </select>
          </div>

          <div class="filter-row">
            <label>Клас</label>
            <select v-model="cabinClass">
              <option value="">Будь-який</option>
              <option value="ECONOMY">Економ</option>
              <option value="PREMIUM">Преміум</option>
              <option value="BUSINESS">Бізнес</option>
            </select>
          </div>

          <div class="filter-actions">
            <button class="ghost" @click="resetFilters">Скинути</button>
            <button class="primary" @click="filtersOpen = false">Застосувати</button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from "@/api/axios.js";
import AirportInput from "@/components/AirportInput.vue";
import { useRouter } from 'vue-router';

const router = useRouter();

const origin = ref('');
const destination = ref('');
const departureDate = ref('');
const nonStop = ref(false);

const passengers = ref({ adults: 1, children: 0, infants: 0 });
const passengersOpen = ref(false);
const passengersRef = ref(null);

const loading = ref(false);
const error = ref(null);
const flights = ref([]);
const hasSearched = ref(false);
const sortBy = ref('best');

const filtersOpen = ref(false);
const maxStops = ref(99);
const cabinClass = ref('');
const showPriceCalendar = ref(true);

const priceDays = ref([
  { label: 'пн', date: '', price: null },
  { label: 'вт', date: '', price: null },
  { label: 'сер', date: '', price: null },
  { label: 'чт', date: '', price: null },
  { label: 'пт', date: '', price: null }
]);

const loadingIata = ref(false);

const loadingSpinner = ref(false);

const passengersSummary = computed(() => {
  const p = passengers.value;
  const total = p.adults + p.children + p.infants;
  return `${total} пас. • ${p.adults}Д ${p.children}Дт ${p.infants}Н`;
});

const visibleFlights = computed(() => {
  let arr = flights.value.slice();
  if (nonStop.value) {
    arr = arr.filter(f => (f.stops || 0) === 0);
  } else if (maxStops.value < 99) {
    arr = arr.filter(f => (f.stops || 0) <= maxStops.value);
  }
  if (sortBy.value === 'price_asc') arr.sort((a,b)=>a.price-b.price);
  if (sortBy.value === 'price_desc') arr.sort((a,b)=>b.price-a.price);
  if (sortBy.value === 'duration') arr.sort((a,b)=>a.durationSeconds - b.durationSeconds);
  return arr;
});

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

function resetPassengers() {
  passengers.value = { adults: 1, children: 0, infants: 0 };
}

function openFilters() { filtersOpen.value = true; }
function resetFilters() {
  maxStops.value = 99;
  cabinClass.value = '';
  nonStop.value = false;
  filtersOpen.value = false;
}

function toggleOnlyDirect() { nonStop.value = !nonStop.value; }

function isIata(code) {
  return /^[A-Z]{3}$/.test((code || '').trim().toUpperCase());
}

async function resolveIata(keyword) {
  if (!keyword) return null;
  const raw = keyword.toString().trim();
  if (raw.length === 0) return null;
  const up = raw.toUpperCase();
  if (isIata(up)) return up;
  try {
    loadingIata.value = true;
    const resp = await api.get('/api/flights/locations', { params: { keyword: raw } });
    const arr = resp.data || [];
    if (!Array.isArray(arr) || !arr.length) return null;
    for (const item of arr) {
      if ((item.subType || item.subtype || '').toUpperCase() === 'CITY' && item.iataCode) return item.iataCode.toUpperCase();
    }
    for (const item of arr) {
      if ((item.subType || item.subtype || '').toUpperCase() === 'AIRPORT' && item.iataCode) return item.iataCode.toUpperCase();
    }
    for (const item of arr) { if (item.iataCode) return item.iataCode.toUpperCase(); }
    return null;
  } catch (e) {
    console.error('resolveIata error', e);
    return null;
  } finally {
    loadingIata.value = false;
  }
}

async function search() {
  error.value = null;
  flights.value = [];
  hasSearched.value = true;

  if (!origin.value || !destination.value || !departureDate.value) {
    error.value = 'Заповніть всі поля!';
    return;
  }

  loading.value = true;
  try {
    const originCode = await resolveIata(origin.value);
    const destCode = await resolveIata(destination.value);
    if (!originCode || !destCode) {
      error.value = 'Не вдалося визначити IATA-код — перевірте місто/аеропорт.';
      loading.value = false;
      return;
    }

    const body = {
      origin: originCode,
      destination: destCode,
      departureDate: departureDate.value,
      adults: Number(passengers.value.adults) || 1,
      children: Number(passengers.value.children) || 0,
      infants: Number(passengers.value.infants) || 0,
      nonStop: Boolean(nonStop.value),
      cabinClass: cabinClass.value || undefined
    };

    const resp = await api.post('/api/flights/search', body);
    const data = resp.data || resp;
    flights.value = Array.isArray(data) ? data : (data.data || []);
    if (data.priceCalendar) {
      priceDays.value = data.priceCalendar;
    }
  } catch (e) {
    console.error(e);
    error.value = e.response?.data?.message || e.message || 'Помилка сервера';
  } finally {
    loading.value = false;
  }
}

function formatDateTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function formatDuration(pt) {
  if (!pt) return '';
  // если приходит PTxHyM
  const re = /^PT(?:(\d+)H)?(?:(\d+)M)?$/;
  const m = (pt || '').match(re);
  if (!m) return pt;
  const hours = m[1] ? `${m[1]}г` : '';
  const mins = m[2] ? `${m[2]}хв` : '';
  return [hours, mins].filter(Boolean).join(' ');
}

function goToDetails(flight) {
  router.push({ name: 'FlightDetails', params: { id: flight.id } });
}

function applySort() {
}

function onOriginIata(iata) {
  // optional hook from AirportInput
  origin.value = iata;
}
function onDestIata(iata) {
  destination.value = iata;
}

function shiftDate(days) {
  if (!departureDate.value) return;
  const d = new Date(departureDate.value);
  d.setDate(d.getDate() + days);
  departureDate.value = d.toISOString().slice(0,10);
  search();
}

function clearFilters() {
  resetFilters();
  search();
}

onMounted(() => {
  const base = new Date();
  const labels = ['пн','вт','сер','чт','пт'];
  priceDays.value = labels.map((l,i)=>{
    const d = new Date(base);
    d.setDate(base.getDate() + i);
    return { label: l, date: d.toISOString().slice(0,10), price: Math.random() > 0.6 ? (20 + Math.floor(Math.random()*120)) : null };
  });
});
</script>

<style scoped>
.page-container { min-height: 100vh; padding: 40px 20px; color: white; display: flex; justify-content: center; }
.container { max-width: 1000px; width: 100%; padding: 30px; background: rgba(255,255,255,0.05); border-radius: 18px; backdrop-filter: blur(10px); }

.page-title { text-align: center; margin: 6px 0 20px; font-size: 30px; }

.search-grid { display: grid; grid-template-columns: 1fr 1fr 180px 160px 120px; gap: 14px; align-items: end; margin-bottom: 22px; }
@media (max-width: 900px) { .search-grid { grid-template-columns: 1fr; } }

.pair { display:flex; gap:10px; align-items:center; }
.input-with-badge { flex:1; }

.swap-btn {
  width: 44px; height:44px; border-radius:10px; border:none;
  background: linear-gradient(180deg,#ffffff12,#ffffff06); color: #cff3ff; font-weight:700;
  display:flex; align-items:center; justify-content:center; cursor:pointer; transition: transform .15s;
}
.swap-btn:hover { transform: rotate(90deg); }

.passengers-btn {
  width:100%;
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.12);
  padding: 10px; border-radius: 10px; color: white; text-align:left;
}
.passengers-popup {
  position: absolute;
  margin-top:8px;
  background: rgba(10,15,20,0.95);
  border: 1px solid rgba(255,255,255,0.06);
  padding: 12px; border-radius: 12px; min-width: 220px;
  box-shadow: 0 12px 30px rgba(0,0,0,0.6);
  z-index: 50;
}
.row { display:flex; justify-content:space-between; align-items:center; padding:8px 0; border-bottom:1px dashed rgba(255,255,255,0.03); }
.row:last-child { border-bottom:none; }
.counter button { width:28px; height:28px; border-radius:6px; border:none; background:#2b3940; color:white; cursor:pointer; }

.pill { display:inline-flex; align-items:center; gap:8px; cursor:pointer; }
.pill input { display:none; }
.pill .pill-label { padding:8px 12px; border-radius:999px; background: rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.06); }
.pill input:checked + .pill-label { background: linear-gradient(90deg,#4facfe,#00f2fe); color:#012; font-weight:600; }

.search-btn { background: linear-gradient(90deg, #4facfe, #00f2fe); border:none; padding:12px 18px; border-radius:12px; color:white; font-weight:700; cursor:pointer; }
.button-group { display:flex; align-items:center; justify-content:center; }

.results-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; gap:12px; }
.mini-controls { display:flex; gap:8px; align-items:center; }
.small-btn { padding:8px 12px; border-radius:8px; background: rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.06); cursor:pointer; }
.small-btn.ghost { background:transparent; }

.price-calendar { display:flex; gap:8px; margin-bottom:12px; }
.pc-day { background: rgba(255,255,255,0.03); padding:10px 12px; border-radius:10px; text-align:center; min-width:72px; }
.pc-day.active { border: 1px solid rgba(79,172,254,0.4); box-shadow: 0 6px 20px rgba(79,172,254,0.06); }

.cards-list { display:flex; flex-direction:column; gap:12px; margin-top:8px; }
.flight-item { display:flex; justify-content:space-between; align-items:center; padding:14px; border-radius:12px; background: rgba(255,255,255,0.03); cursor:pointer; }
.flight-item:hover { transform: translateY(-3px); box-shadow: 0 12px 30px rgba(0,0,0,0.5); }

.empty-state { text-align:center; padding:26px; background: rgba(255,255,255,0.02); border-radius:12px; margin-top:10px; }
.suggestions { display:flex; gap:10px; justify-content:center; margin-top:12px; }
.suggest { padding:10px 14px; border-radius:10px; border:1px solid rgba(255,255,255,0.06); background: rgba(255,255,255,0.02); cursor:pointer; }

.filters-modal { position:fixed; inset:0; background: rgba(0,0,0,0.45); display:flex; justify-content:flex-end; z-index:120; }
.filters-card { width:320px; background: linear-gradient(180deg, rgba(255,255,255,0.03), rgba(0,0,0,0.6)); padding:18px; border-left:1px solid rgba(255,255,255,0.04); }
.filter-row { margin-bottom:12px; display:flex; flex-direction:column; gap:8px; }

.ghost { background: transparent; border:1px solid rgba(255,255,255,0.08); padding:8px 12px; border-radius:8px; cursor:pointer; }
.primary { background: linear-gradient(90deg,#4facfe,#00f2fe); color:#012; border:none; padding:8px 12px; border-radius:8px; cursor:pointer; }

.sr-only { position:absolute; width:1px; height:1px; padding:0; margin:-1px; overflow:hidden; clip:rect(0,0,0,0); white-space:nowrap; border:0; }
</style>
