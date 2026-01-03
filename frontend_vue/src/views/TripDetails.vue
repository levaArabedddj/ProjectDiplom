<template>
  <div class="details-page">
    <div class="container">

      <header class="dashboard-header glass-card">
        <div class="header-left">
          <button @click="$router.push('/travel')" class="back-btn">←</button>
          <div>
            <h1>{{ trip.cityName }}</h1>
            <p class="date-badge">📅 {{ formatDate(trip.startDate) }} — {{ formatDate(trip.endDate) }}</p>
          </div>
        </div>

        <div class="header-right">
          <div class="budget-widget">
            <span class="label">Бюджет:</span>
            <span class="amount">{{ trip.balance || 0 }} {{ trip.currency || '' }}</span>
            <button class="edit-btn">✏️</button>
          </div>
        </div>
      </header>

      <div class="dashboard-grid">

        <aside class="sidebar">
          <div class="glass-card menu-card">
            <h3>Меню подорожі</h3>
            <ul>
              <li>📝 Нотатки</li>
              <li>🧳 Чек-лист речей</li>
              <li>🏨 Знайти готель</li>
              <li>🎫 Купити квитки</li>
            </ul>
          </div>

          <div class="glass-card ai-card">
            <div class="ai-header">
              <span class="ai-icon">🤖</span>
              <h3>AI Порадник</h3>
            </div>
            <p>Я підібрав для вас 3 ресторани у центрі {{ trip.cityName }}, які підходять під ваш бюджет.</p>
            <button class="btn-small">Отримати нові поради</button>
          </div>
        </aside>

        <main class="main-content">

          <section>
            <h2 class="section-title">🛫 Ваші квитки</h2>
            <div v-if="trip.flights && trip.flights.length" class="tickets-list">
              <div v-for="f in trip.flights" :key="f.id" class="ticket glass-card">
                <div class="ticket-left">
                  <span class="airline">{{ f.airline }}</span>
                  <div class="route">
                    <h2>{{ f.fromAirport }}</h2>
                    <span class="plane-icon">✈️</span>
                    <h2>{{ f.toAirport }}</h2>
                  </div>
                  <div class="times">
                    <span>{{ formatTime(f.departureTime) }}</span>
                    <span>{{ formatTime(f.arrivalTime) }}</span>
                  </div>
                </div>
                <div class="ticket-right">
                  <span class="seat">GATE A1</span>
                  <span class="price">{{ f.price }} {{ f.currency }}</span>
                </div>
              </div>
            </div>
            <div v-else class="empty-state glass-card">
              <p>Квитків ще немає</p>
              <button class="btn-action">Знайти авіаквитки 🔍</button>
            </div>
          </section>

          <section class="hotels-section">
            <h2 class="section-title">🏨 Проживання</h2>
            <div v-if="trip.hotels && trip.hotels.length" class="hotels-list">
              <div v-for="h in trip.hotels" :key="h.id" class="hotel-card glass-card">
                <div class="hotel-info">
                  <h3>{{ h.name }}</h3>
                  <div class="stars">
                    <span v-for="n in h.stars" :key="n">⭐</span>
                  </div>
                  <p class="address">📍 {{ h.address }}</p>
                </div>
                <div class="hotel-actions">
                  <a :href="h.bookingUrl" target="_blank" class="btn-link">Бронь →</a>
                </div>
              </div>
            </div>
            <div v-else class="empty-state glass-card">
              <p>Готель не заброньовано</p>
              <button class="btn-action">Знайти готель на Booking 🏨</button>
            </div>
          </section>

        </main>

        <aside class="right-panel">
          <section class="glass-card places-wrapper">
            <h2>📍 Куди піти</h2>

            <div class="places-controls">
              <button class="btn-small" @click="toggleShowPlaces" v-if="!showPlaces && !loadingPlaces">
                Показати місця
              </button>

              <button class="btn-small ghost" @click="showPlaces = false" v-if="showPlaces && !loadingPlaces">
                Сховати
              </button>

              <div v-if="loadingPlaces" class="muted">Будь ласка, зачекайте — місця завантажуються…</div>
            </div>

            <ul v-if="showPlaces && !loadingPlaces && tripPlaces.length" class="simple-places-list">
              <li
                  v-for="p in tripPlaces"
                  :key="p.id"
                  class="place-mini-card"
              >
                <div class="place-header">
                  <h4>{{ p.name }}</h4>
                </div>

                <div class="place-actions">
                  <button class="btn-small" @click="goToPlaceDetails(p.id)">
                    Деталі →
                  </button>
                </div>
              </li>

              <li class="actions-row">
                <button class="btn-small" @click="openPlacesFinder">
                  ➕ Додати ще
                </button>
              </li>
            </ul>

            <div v-if="showPlaces && !loadingPlaces && !tripPlaces.length" class="empty-mini">
              <p>Поки що місць немає</p>
              <button class="btn-small" @click="openPlacesFinder">🔍 Підібрати місця</button>
            </div>

            <PlacesFinder
                v-if="showPlacesFinder"
                :city="trip.cityName"
                :tripId="trip.id"
                @close="showPlacesFinder = false"
                @added="handlePlaceAddedFromFinder"
            />
          </section>


          <section class="glass-card weather-widget" v-if="trip?.cityName">
            <div class="ww-header">
              <div>
                <span class="ww-title">Погода в {{ trip.cityName }}</span>
                <div class="ww-sub">Прогноз по дням</div>
              </div>

              <div class="weather-tabs">
                <button
                    v-for="(label, index) in ['Сьогодні', 'Завтра', 'Післязавтра']"
                    :key="index"
                    :class="['tab-btn', { active: selectedDay === index }]"
                    @click="changeDay(index)"
                >
                  {{ label }}
                </button>
              </div>
            </div>

            <div class="current-weather-box" v-if="weatherList && weatherList.length">
              <div class="main-info">
                <img :src="weatherList[0].iconUrl" class="weather-icon-lg" alt="icon" />
                <div class="temp-block">
                  <span class="temp-big">{{ Math.round(weatherList[0].temp) }}°</span>
                  <span class="desc-text">{{ weatherList[0].description }}</span>
                </div>
              </div>

              <div class="details-grid-weather">
                <div class="detail-item">
                  <span class="detail-icon">🌡</span>
                  <div>
                    <span class="detail-val">{{ Math.round(weatherList[0].feelsLike) }}°</span>
                    <span class="detail-label">Відчувається</span>
                  </div>
                </div>

                <div class="detail-item">
                  <span class="detail-icon">💨</span>
                  <div>
                    <span class="detail-val">{{ weatherList[0].windSpeed }} м/с</span>
                    <span class="detail-label">Вітер</span>
                  </div>
                </div>

                <div class="detail-item">
                  <span class="detail-icon">💧</span>
                  <div>
                    <span class="detail-val">{{ weatherList[0].humidity }}%</span>
                    <span class="detail-label">Вологість</span>
                  </div>
                </div>

                <div class="detail-item">
                  <span class="detail-icon">☔</span>
                  <div>
                    <span class="detail-val">{{ weatherList[0].precipProb }}%</span>
                    <span class="detail-label">Опади</span>
                  </div>
                </div>
              </div>

              <div class="hourly-strip">
                <div v-for="(w, i) in weatherList.slice(0, 5)" :key="i" class="h-item">
                  <span class="h-time">{{ w.time }}</span>
                  <img :src="w.iconUrl" width="35" alt="icon" />
                  <span class="h-temp">{{ Math.round(w.temp) }}°</span>
                </div>
              </div>
            </div>

            <div v-else class="center muted">Информация о погоде не доступна для выбранного дня.</div>
          </section>
        </aside>
      </div>
    </div>
  </div>
</template>


<script setup>
  import { ref, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import api from '@/api/axios'
  import PlacesFinder from '@/components/PlacesFinder.vue'

  const route = useRoute()
  const router = useRouter()

  // реактивная модель поездки
  const trip = ref({
  id: null,
  cityName: '',
  startDate: null,
  endDate: null,
  balance: null,
  currency: null,
  flights: [],
  hotels: [],
  placesToVisit: []
})

  const weatherList = ref([])
  const selectedDay = ref(0)
  const showPlacesFinder = ref(false)

  function openPlacesFinder() {
  showPlacesFinder.value = true
}
  const showPlaces = ref(false)         // флаг отобразить список
  const loadingPlaces = ref(false)      // индикатор загрузки
  const tripPlaces = ref([])            // массив упрощённых мест (id, name, category)

  function goToPlaceDetails(placeId) {
    if (!placeId || !trip.value?.id) return

    router.push({
      name: 'PlaceDetails',
      params: {
        tripId: trip.value.id,
        placeId: placeId
      }
    })
  }

  function toggleShowPlaces() {
    if (showPlaces.value) {
      showPlaces.value = false
      return
    }
    showPlaces.value = true
    if (!tripPlaces.value.length) {
      loadTripPlaces()
    }
  }

  async function loadTripPlaces() {
    if (!trip.value?.id) return
    loadingPlaces.value = true
    tripPlaces.value = []
    try {
      const res = await api.get(`/api/trips/${trip.value.id}/places`)
      tripPlaces.value = Array.isArray(res.data) ? res.data : []
    } catch (e) {
      console.error('loadTripPlaces error', e)
    } finally {
      loadingPlaces.value = false
    }
  }

  function handlePlaceAddedFromFinder(savedPlace) {
    if (!tripPlaces.value) tripPlaces.value = []
    if (!savedPlace) {
      if (showPlaces.value) loadTripPlaces()
      return
    }
    tripPlaces.value.unshift(savedPlace)
    showPlacesFinder.value = false
  }

  async function fetchTrip() {
  const tripId = route.params.tripId
  if (!tripId) return
  try {
  const tripResponse = await api.get(`/api/trips/${tripId}`)
  trip.value = tripResponse.data
} catch (e) {
  console.error('fetchTrip error', e)
}
}

  const fetchWeather = async (city, dayOffset = 0) => {
  if (!city) return
  try {
  const response = await api.get('/api/weather/weather', {
  params: { city: city, dayOffset }
})
  weatherList.value = response.data || []
} catch (e) {
  console.error('Ошибка погоды', e)
  weatherList.value = []
}
}

  const changeDay = async (day) => {
  selectedDay.value = day
  await fetchWeather(trip.value.cityName, day)
}



  onMounted(async () => {
  await fetchTrip()
  if (trip.value.cityName) {
  await fetchWeather(trip.value.cityName, selectedDay.value)
}
})

  // маленькие утилиты
  function formatDate(d) { return d ? new Date(d).toLocaleDateString('uk-UA') : '—' }
  function formatTime(d) { return d ? new Date(d).toLocaleTimeString('uk-UA', {hour: '2-digit', minute:'2-digit'}) : '--:--' }
</script>


<style scoped>

.ww-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.weather-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-left: auto;
}

.tab-btn {
  padding: 6px 10px;
  border-radius: 10px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.06);
  color: white;
  font-size: 0.9rem;
  cursor: pointer;
  min-width: 72px;
  text-align: center;
}

.tab-btn.active {
  background: #646cff;
  border-color: #646cff;
  color: #012;
}

@media (max-width: 420px) {
  .tab-btn { padding: 6px 8px; font-size: 0.82rem; min-width: 64px; }
}

.details-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
  padding: 30px;
  color: white;
  font-family: 'Inter', sans-serif;
}

.container { max-width: 1400px; margin: 0 auto; }

.glass-card {
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 16px;
  padding: 20px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.header-left { display: flex; align-items: center; gap: 20px; }
.back-btn { background: rgba(255,255,255,0.1); border: none; color: white; width: 40px; height: 40px; border-radius: 50%; cursor: pointer; font-size: 1.2rem; }
.back-btn:hover { background: rgba(255,255,255,0.2); }
.header-right { display: flex; gap: 20px; }

.budget-widget, .weather-widget {
  background: rgba(0,0,0,0.2);
  padding: 10px 20px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.amount { font-size: 1.4rem; font-weight: bold; color: #4ade80; }

.dashboard-grid {
  display: grid;
  grid-template-columns: 280px 1fr 300px;
  gap: 25px;
  align-items: start;
}

.menu-card ul { list-style: none; padding: 0; }
.menu-card li { padding: 12px 0; border-bottom: 1px solid rgba(255,255,255,0.1); cursor: pointer; transition: 0.2s; display: flex; gap: 10px;}
.menu-card li:hover { color: #646cff; transform: translateX(5px); }

.ai-card { margin-top: 20px; background: linear-gradient(145deg, rgba(100,108,255,0.2), rgba(0,0,0,0)); border: 1px solid rgba(100,108,255,0.3); }
.ai-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.ai-icon { font-size: 1.5rem; }

.section-title { margin-top: 0; margin-bottom: 15px; font-size: 1.2rem; opacity: 0.9; }

.ticket {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  position: relative;
  overflow: hidden;
}
.ticket::before {
  content: "";
  position: absolute;
  right: 120px;
  top: 0;
  bottom: 0;
  border-left: 2px dashed rgba(255,255,255,0.3);
}

.ticket-left { flex: 1; padding-right: 20px; }
.ticket-right {
  width: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-left: 20px;
}
.route { display: flex; align-items: center; gap: 15px; margin: 5px 0; }
.price { font-size: 1.2rem; font-weight: bold; color: #ffd700; }
.airline { text-transform: uppercase; font-size: 0.8rem; letter-spacing: 1px; opacity: 0.7; }

.hotel-card { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.btn-link { color: #646cff; text-decoration: none; font-weight: bold; }

.place-mini-card {
  background: rgba(0,0,0,0.2);
  padding: 15px;
  border-radius: 10px;
  margin-bottom: 10px;
}
.category-tag { font-size: 0.65rem; background: #646cff; padding: 2px 6px; border-radius: 4px; text-transform: uppercase; }
.place-header { display: flex; justify-content: space-between; margin-bottom: 5px; }
.place-desc { font-size: 0.85rem; opacity: 0.7; margin: 0; }

.btn-action {
  width: 100%;
  padding: 15px;
  background: rgba(255,255,255,0.1);
  border: 1px dashed rgba(255,255,255,0.3);
  color: white;
  border-radius: 12px;
  cursor: pointer;
  transition: 0.3s;
}
.btn-action:hover { background: rgba(255,255,255,0.2); border-color: white; }
.btn-small { background: #646cff; color: white; border: none; padding: 8px 12px; border-radius: 8px; cursor: pointer; width: 100%; margin-top: 10px; }

@media (max-width: 1024px) {
  .dashboard-grid { grid-template-columns: 1fr; }
  .sidebar, .right-panel { display: none;  }
  .ticket::before { display: none; }
  .ticket { flex-direction: column; align-items: flex-start; gap: 15px; }
  .ticket-right { width: 100%; flex-direction: row; justify-content: space-between; padding: 0; border-top: 1px solid rgba(255,255,255,0.1); padding-top: 10px;}
}
.weather-widget {
  padding: 20px;
  width: 100%;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 16px;
  box-sizing: border-box;
  align-items: stretch;
}

.ww-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 0.9rem;
  opacity: 0.8;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  padding-bottom: 8px;
}

.current-weather-box {
  margin-bottom: 20px;
}

.main-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.weather-icon-lg {
  width: 80px;
  height: 80px;
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.2));
}

.temp-big {
  font-size: 3rem;
  font-weight: 700;
  line-height: 1;
}

.desc-text {
  display: block;
  font-size: 1rem;
  text-transform: capitalize;
  opacity: 0.9;
}

.details-grid-weather {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  background: rgba(255,255,255,0.05);
  padding: 15px;
  border-radius: 12px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-icon { font-size: 1.2rem; }
.detail-val { display: block; font-weight: bold; font-size: 0.95rem; }
.detail-label { display: block; font-size: 0.7rem; opacity: 0.6; }

.hourly-strip {
  display: flex;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.h-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 0.8rem;
}
.h-time { opacity: 0.6; margin-bottom: 4px; }

.places-controls { margin-bottom: 8px; display:flex; gap:8px; align-items:center; }
.simple-places-list { list-style:none; padding:0; margin:0; display:block; gap:8px; }
.place-mini-card { padding:10px; border-radius:8px; background: rgba(255,255,255,0.02); margin-bottom:8px; }
.actions-row { margin-top:10px; }

</style>