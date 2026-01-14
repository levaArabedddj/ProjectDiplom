<template>
  <div class="details-page">
    <div class="container">

      <header class="dashboard-header glass-card">
        <div class="header-left">
          <button @click="$router.push('/travel')" class="back-btn">←</button>
          <div>
            <div class="title-with-edit">
              <h1>{{ trip.cityName }}</h1>
              <button class="main-edit-btn" @click="openEditModal" title="Редагувати подорож">✏️</button>
            </div>
            <div class="subtitle-row">
              <p class="date-badge">📅 {{ formatDate(trip.startDate) }} — {{ formatDate(trip.endDate) }}</p>
              <span :class="['status-badge', trip.status === 'DONE' ? 'done' : 'planned']">
          {{ trip.status === 'DONE' ? '✅ Завершено' : '⏳ Планується' }}
       </span>
            </div>
          </div>
        </div>

        <div class="header-right">
          <div class="budget-widget">
            <span class="label">Бюджет:</span>
            <span class="amount">{{ trip.balance || 0 }} {{ trip.currency || '' }}</span>
            <button class="main-edit-btn" @click="openEditModal">✏️</button>
          </div>
        </div>
      </header>

      <div class="dashboard-grid">

        <aside class="sidebar">
          <div class="glass-card menu-card">
            <h3>Меню подорожі</h3>
            <ul>
              <li @click="toggleNotes" :class="{ active: showNotes }" class="menu-item">
                <span>📝 Нотатки</span>
                <span class="arrow">{{ showNotes ? '▼' : '▶' }}</span>
              </li>

              <transition name="slide">
                <div v-if="showNotes" class="notes-container">
                  <div class="add-note-wrapper">
                    <input
                        v-model="newNoteText"
                        placeholder="Нове завдання..."
                        @keyup.enter="addNote"
                        class="note-input"
                    />
                    <button @click="addNote" :disabled="!newNoteText.trim()" class="btn-add-note">
                      +
                    </button>
                  </div>

                  <ul class="notes-list">
                    <li v-for="note in notes" :key="note.id" class="note-card">
                      <label class="custom-checkbox">
                        <input
                            type="checkbox"
                            :checked="note.completed"
                            @change="toggleNote(note)"
                        />
                        <span class="checkmark"></span>
                      </label>

                      <span class="note-text" :class="{ 'completed-text': note.completed }">
          {{ note.text }}
        </span>

                      <button class="btn-delete-note" @click="deleteNote(note.id)" title="Видалити">
                        🗑️
                      </button>
                    </li>

                    <li v-if="notes.length === 0" class="empty-notes">
                      Список порожній 🍃
                    </li>
                  </ul>
                </div>
              </transition>
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

              <div v-if="loadingAdvices" class="ai-loading">
                <div class="spinner"></div>
                <p>Аналізую місто {{ trip.cityName }}...</p>
              </div>

              <div v-else-if="advices.length > 0" class="advices-list">
                <div v-for="(advice, index) in advices" :key="index" class="advice-item">
                  <div class="advice-category">
                    <span>{{ getCategoryIcon(advice.category) }}</span>
                    <strong>{{ advice.category }}</strong>
                  </div>
                  <p class="advice-text">{{ advice.content }}</p>
                </div>
              </div>

              <p v-else>Я підготую для вас поради щодо безпеки, економії та особливостей {{ trip.cityName }}.</p>

              <button
                  class="btn-ai-generate"
                  @click="generateNewAdvices"
                  :disabled="loadingAdvices"
              >
                {{ advices.length > 0 ? 'Оновити поради' : 'Отримати поради' }}
              </button>
            </div>

        </aside>

        <main class="main-content">

          <section>
            <h2 class="section-title">🛫 Ваші квитки</h2>

            <div v-if="trip.bookings && trip.bookings.length" class="tickets-list">
              <div v-for="b in trip.bookings" :key="b.id" class="ticket glass-card">
                <div class="ticket-content">
                  <div class="ticket-left">
                    <div class="airline-info">
                      <span class="plane-icon-mini">✈️</span>
                      <span class="flight-num">{{ b.flightNumber || '№ не вказано' }}</span>
                    </div>

                    <div class="route">
                      <div class="airport">
                        <span class="code">{{ b.departureAirport || '???' }}</span>
                      </div>
                      <div class="route-line">
                        <div class="line"></div>
                        <span class="plane-icon">✈️</span>
                      </div>
                      <div class="airport">
                        <span class="code">{{ b.arrivalAirport || '???' }}</span>
                      </div>
                    </div>

                    <div class="times">
                      <span>📅 {{ formatTime(b.departureTime) }}</span>
                    </div>
                  </div>

                  <div class="ticket-right">
                    <div class="pnr-label">ID: {{ b.id }}</div>
                    <div class="price-tag">{{ b.totalPrice }} <span class="curr">{{ b.currency }}</span></div>
                  </div>
                </div>
              </div>
            </div> <div v-else class="empty-state glass-card">
            <p>Квитків ще немає</p>
            <div class="buttons-row">
              <button class="btn-action" @click="$router.push('/avion')">Знайти нові 🔍</button>
              <button class="btn-action secondary" @click="openBookingModal" style="margin-left: 10px;">
                Додати з моїх квитків 🎫
              </button>
            </div>
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
              <button class="btn-action" @click="$router.push('/hotels')">Знайти готель на Booking 🏨</button>
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
              <li v-for="p in tripPlaces" :key="p.id" class="place-mini-card">
                <div class="place-header">
                  <h4>{{ p.name }}</h4>
                </div>

                <div class="place-actions">
                  <button class="btn-small" @click="goToPlaceDetails(p.id)">
                    Деталі →
                  </button>

                  <button class="btn-icon delete" @click="confirmDeletePlace(p)" title="Видалити місце">
                    🗑️
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

            <div v-if="showDeleteModal" class="modal-overlay" @click.self="cancelDelete">
              <div class="modal-confirm glass-card">
                <h3>Видалити це місце? 🗑️</h3>
                <p>
                  Ви впевнені, що хочете прибрати <strong>{{ placeToDelete?.name }}</strong> зі свого маршруту?
                  <br>Цю дію не можна скасувати.
                </p>

                <div class="confirm-actions">
                  <button class="btn-secondary" @click="cancelDelete">
                    Ні, залишити
                  </button>
                  <button class="btn-danger" @click="deletePlace" :disabled="isDeleting">
                    {{ isDeleting ? 'Видалення...' : 'Так, видалити' }}
                  </button>
                </div>
              </div>
            </div>
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
  <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
    <div class="modal-form glass-card">
      <h2>✏️ Редагувати подорож</h2>

      <div class="form-group">
        <label>Бюджет</label>
        <input v-model.number="editForm.balance" type="number" placeholder="Сума">
      </div>

      <div class="form-group">
        <label>Валюта</label>
        <select v-model="editForm.currency">
          <option value="USD">USD ($)</option>
          <option value="EUR">EUR (€)</option>
          <option value="UAH">UAH (₴)</option>
        </select>
      </div>

      <div class="form-group-row">
        <div class="form-group">
          <label>Початок</label>
          <input v-model="editForm.startDate" type="date">
        </div>
        <div class="form-group">
          <label>Кінець</label>
          <input v-model="editForm.endDate" type="date">
        </div>
      </div>

      <div class="form-group">
        <label>Статус</label>
        <select v-model="editForm.status">
          <option value="PLANNED">⏳ Планується</option>
          <option value="IN_PROGRESS">🚀 В процесі</option>
          <option value="DONE">✅ Завершено</option>
        </select>
      </div>

      <div class="modal-actions">
        <button class="btn-secondary" @click="closeEditModal">Скасувати</button>
        <button class="btn-primary" @click="saveTripChanges">Зберегти</button>
      </div>
    </div>
  </div>

  <UserBookingsModal
      v-if="showBookingModal"
      @close="showBookingModal = false"
      @add="handleAddTicket"
  />
</template>


<script setup>
  import { ref, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import api from '@/api/axios'
  import PlacesFinder from '@/components/PlacesFinder.vue'
  import UserBookingsModal from '@/components/UserBookingsModal.vue';
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
    bookings: [],
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

  // ЗАМЕНИТЕ старую fetchTrip на эту
  async function fetchTrip() {
    const tripId = route.params.tripId
    if (!tripId) return
    try {
      const tripResponse = await api.get(`/api/trips/${tripId}`)
      const data = tripResponse.data || {}

      // normalize: если сервер возвращает flights, но шаблон ожидает bookings
      if (data.flights && !data.bookings) {
        data.bookings = data.flights
      }

      // гарантируем, что поля — массивы
      data.bookings = Array.isArray(data.bookings) ? data.bookings : []
      data.hotels = Array.isArray(data.hotels) ? data.hotels : []
      data.placesToVisit = Array.isArray(data.placesToVisit) ? data.placesToVisit : []

      trip.value = data
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

  const showDeleteModal = ref(false)
  const placeToDelete = ref(null)
  const isDeleting = ref(false)

  function confirmDeletePlace(place) {
    placeToDelete.value = place
    showDeleteModal.value = true
  }

  function cancelDelete() {
    showDeleteModal.value = false
    placeToDelete.value = null
  }

  async function deletePlace() {
    if (!placeToDelete.value) return

    isDeleting.value = true
    try {
      const tripId = trip.value.id
      const placeId = placeToDelete.value.id

      await api.delete(`/api/trips/${tripId}/places/${placeId}`)

      tripPlaces.value = tripPlaces.value.filter(p => p.id !== placeId)

      cancelDelete()
    } catch (error) {
      console.error('Помилка видалення:', error)
      alert('Щось пішло не так при видаленні :(')
    } finally {
      isDeleting.value = false
    }
  }

  const showNotes = ref(false)
  const notes = ref([])
  const newNoteText = ref('')

  async function toggleNotes() {
    showNotes.value = !showNotes.value
    if (showNotes.value) {
      await loadNotes()
    }
  }

  async function loadNotes() {
    try {
      const res = await api.get(`/api/trips/${trip.value.id}/notes`)
      notes.value = res.data
    } catch (e) {
      console.error("Ошибка загрузки заметок", e)
    }
  }

  async function addNote() {
    if (!newNoteText.value.trim()) return
    try {
      const res = await api.post(`/api/trips/${trip.value.id}/note`, newNoteText.value, {
        headers: { 'Content-Type': 'text/plain' }
      })
      notes.value.unshift(res.data)
      newNoteText.value = ''
    } catch (e) {
      console.error("Ошибка добавления", e)
    }
  }


  async function toggleNote(note) {
    try {
      await api.put(`/api/trips/${trip.value.id}/${note.id}/toggle`)
      note.completed = !note.completed
    } catch (e) {
      console.error("Ошибка статуса", e)
    }
  }


  async function deleteNote(noteId) {
    try {
      await api.delete(`/api/trips/${trip.value.id}/update/${noteId}`)
      notes.value = notes.value.filter(n => n.id !== noteId)
    } catch (e) {
      console.error("Ошибка удаления", e)
    }
  }

  const showEditModal = ref(false)
  const editForm = ref({
    balance: 0,
    currency: 'USD',
    startDate: '',
    endDate: '',
    status: 'PLANNED'
  })

  const openEditModal = () => {
    editForm.value = {
      balance: trip.value.balance,
      currency: trip.value.currency,
      startDate: trip.value.startDate,
      endDate: trip.value.endDate,
      status: trip.value.status || 'PLANNED'
    }
    showEditModal.value = true
  }

  const closeEditModal = () => {
    showEditModal.value = false
  }

  const saveTripChanges = async () => {
    try {
      const id = trip.value.id

      if (!id) {
        alert('Помилка: ID подорожі не знайдено')
        return
      }

      const res = await api.put(`/api/trips/${id}`, editForm.value)

      trip.value = res.data
      closeEditModal()
    } catch (e) {
      alert('Не вдалося оновити дані подорожі')
      console.error(e)
    }
  }

  const showBookingModal = ref(false);

  function openBookingModal() {
    showBookingModal.value = true;
  }

  async function handleAddTicket(ticket) {
    const confirmed = window.confirm(`Додати квиток (Ref: ${ticket.pnrReference}) до цієї подорожі?`);

    if (confirmed) {
      try {
        await api.post(`/api/trips/${trip.value.id}/add-booking/${ticket.id}`);
        showBookingModal.value = false;
        await fetchTrip();

      } catch (e) {
        console.error("Помилка додавання квитка", e);
        alert("Не вдалося додати квиток. Спробуйте пізніше.");
      }
    }
  }

  const advices = ref([]);
  const loadingAdvices = ref(false);

  async function generateNewAdvices() {
    if (!trip.value?.id) return;

    loadingAdvices.value = true;
    try {
      const res = await api.post(`/api/trips/${trip.value.id}/advice/generate`);
      advices.value = res.data;
    } catch (e) {
      console.error("Помилка при генерації порад:", e);
      alert("Не вдалося отримати поради від AI.");
    } finally {
      loadingAdvices.value = false;
    }
  }

  const getCategoryIcon = (category) => {
    const cat = category.toLowerCase();
    if (cat.includes('безпека')) return '🛡️';
    if (cat.includes('економія')) return '💰';
    if (cat.includes('їжа') || cat.includes('ресторан')) return '🍽️';
    if (cat.includes('транспорт')) return '🚌';
    return '💡';
  };


</script>
<style scoped>


.ww-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 0.9rem;
  opacity: 0.8;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  padding-bottom: 8px;
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

.weather-widget {
  background: rgba(0,0,0,0.2);
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding: 20px;
  width: 100%;
  border-radius: 16px;
  box-sizing: border-box;
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

.ticket-right {
  width: 140px;
  background: rgba(255, 255, 255, 0.03);
  border-left: 2px dashed rgba(255, 255, 255, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 15px;
  box-sizing: border-box;
}


.ticket-left { flex: 1; padding-right: 20px; }

.route { display: flex; align-items: center; gap: 15px; margin: 5px 0; }
.price { font-size: 1.2rem; font-weight: bold; color: #ffd700; }
.airline { text-transform: uppercase; font-size: 0.8rem; letter-spacing: 1px; opacity: 0.7; }

.hotel-card { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.btn-link { color: #646cff; text-decoration: none; font-weight: bold; }

.place-mini-card {
  background: rgba(255,255,255,0.02);
  padding: 15px;
  border-radius: 10px;
  margin-bottom: 10px;
}
.place-header { display: flex; justify-content: space-between; margin-bottom: 5px; }

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
.actions-row { margin-top:10px; }

.place-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-icon.delete {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 99, 99, 0.3);
  border-radius: 8px;
  cursor: pointer;
  padding: 6px 10px;
  font-size: 1.1rem;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-icon.delete:hover {
  background: rgba(255, 70, 70, 0.2);
  border-color: rgba(255, 70, 70, 0.6);
  transform: scale(1.05);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(5px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease-out;
}

.modal-confirm {
  background: #1e293b;
  background: rgba(30, 41, 59, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 32px;
  border-radius: 20px;
  max-width: 400px;
  width: 90%;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0,0,0,0.5);
  color: #fff;
  animation: slideUp 0.3s ease-out;
}

.modal-confirm h3 {
  margin-top: 0;
  margin-bottom: 12px;
  color: #ff6b6b;
}

.modal-confirm p {
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 24px;
  line-height: 1.5;
}

.confirm-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn-secondary {
  padding: 10px 20px;
  border-radius: 10px;
  border: 1px solid rgba(255,255,255,0.2);
  background: transparent;
  color: white;
  cursor: pointer;
  transition: 0.2s;
}
.btn-secondary:hover {
  background: rgba(255,255,255,0.1);
}

.btn-danger {
  padding: 10px 20px;
  border-radius: 10px;
  border: none;
  background: #ff4757;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}
.btn-danger:hover {
  background: #ff6b81;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 71, 87, 0.4);
}
.btn-danger:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
.menu-item {
  display: flex;
  justify-content: space-between;
  cursor: pointer;
  transition: 0.3s;
}


.add-note input {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 5px;
  padding: 5px;
  flex: 1;
  font-size: 0.8rem;
}



.notes-container {
  margin-top: 15px;
  margin-bottom: 15px;
  padding: 0 5px;
}

.add-note-wrapper {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
}

.note-input {
  flex: 1;
  background: rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: white;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 0.9rem;
  transition: 0.2s;
}

.note-input:focus {
  outline: none;
  border-color: #646cff;
  background: rgba(0, 0, 0, 0.4);
}

.btn-add-note {
  background: #646cff;
  color: white;
  border: none;
  border-radius: 8px;
  width: 36px;
  font-size: 1.2rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.2s;
}

.btn-add-note:hover:not(:disabled) {
  background: #535bf2;
}

.btn-add-note:disabled {
  background: rgba(255, 255, 255, 0.1);
  cursor: not-allowed;
  color: rgba(255, 255, 255, 0.3);
}

.notes-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}


.note-card {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  padding: 12px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  transition: all 0.2s ease;
}

.note-card:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

.note-text {
  flex: 1;
  font-size: 0.95rem;
  line-height: 1.4;
  word-break: break-word;
  padding-top: 1px;
}

.completed-text {
  text-decoration: line-through;
  opacity: 0.5;
  color: #9ca3af;
}

.btn-delete-note {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  opacity: 0.4;
  padding: 2px;
  transition: 0.2s;
  margin-top: -2px;
  margin-left: auto;
  background: none;
  color: #ff4757;
}

.btn-delete-note:hover {
  opacity: 1;
  color: #ff4757;
  transform: scale(1.1);
}

/* Чекбокс */
.custom-checkbox {
  display: flex;
  align-items: center;
  padding-top: 2px;
}

.custom-checkbox input {
  cursor: pointer;
  width: 18px;
  height: 18px;
  accent-color: #646cff;
}


.empty-notes {
  text-align: center;
  font-size: 0.85rem;
  opacity: 0.5;
  padding: 10px 0;
  font-style: italic;
}

/* Бейдж статуса */
.subtitle-row {
  display: flex;
  gap: 15px;
  align-items: center;
}

.status-badge {
  font-size: 0.85rem;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-badge.planned {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 1px solid rgba(255,255,255,0.4);
}

.status-badge.done {
  background: #4ade80;
  color: #0f2027;
  box-shadow: 0 0 10px rgba(74, 222, 128, 0.4);
}

/* Модальное окно формы */
.modal-form {
  background: #1e293b; /* Темный фон, чтобы текст читался */
  color: white;
  padding: 30px;
  width: 90%;
  max-width: 400px;
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.2);
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group-row {
  display: flex;
  gap: 15px;
}

.form-group label {
  font-size: 0.9rem;
  color: #aaa;
}

.modal-form input,
.modal-form select {
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  padding: 10px;
  border-radius: 8px;
  color: white;
  font-size: 1rem;
}

.modal-form input:focus,
.modal-form select:focus {
  outline: none;
  border-color: #646cff;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 10px;
}

.btn-primary {
  background: #646cff;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
}

.btn-secondary {
  background: transparent;
  color: #ccc;
  padding: 10px 20px;
  border: 1px solid #555;
  border-radius: 8px;
  cursor: pointer;
}

.title-with-edit {
  display: flex;
  align-items: center;
  gap: 12px;
}

.main-edit-btn {
  background: rgba(255, 255, 255, 0.1); /* Прозрачный фон как у первой кнопки */
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 0.9rem;
  outline: none;
}

.main-edit-btn:hover {
  background: #646cff;
  border-color: #646cff;
  transform: scale(1.1);
  box-shadow: 0 0 15px rgba(100, 108, 255, 0.4);
}

.budget-widget {
  background: rgba(0,0,0,0.25);
  padding: 10px 15px;
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.05);
  display: flex;
  flex-direction: column;
  align-items: center; /* Изменено с flex-end на center для центровки */
  gap: 8px;            /* Добавляет отступ между текстом и кнопкой */
  min-width: 100px;    /* Немного увеличим для красоты */
}

.ticket-container {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  margin-bottom: 20px;
  display: flex;
  overflow: hidden;
}
.tickets-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: 100%;
}

.ticket.glass-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  margin-bottom: 15px;
  overflow: hidden;
  transition: transform 0.2s;
  display: block;
}

.ticket-content {
  display: flex;
  justify-content: space-between;
  min-height: 100px;
  width: 100%;
}

.ticket-left {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.airline-info {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #4facfe;
  font-weight: bold;
}

.route {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 5px 0;
}

.airport .code {
  font-size: 1.6rem;
  font-weight: 800;
  color: #fff;
  letter-spacing: 1px;
}


.route-line {
  flex-grow: 1;
  display: flex;
  align-items: center;
  position: relative;
  min-width: 50px;
  max-width: 120px;
}

.route-line .line {
  height: 0;
  width: 100%;
  border-top: 2px dashed rgba(255, 255, 255, 0.3);
}


.route-line::after {
  content: '✈';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #2c5364; /* Цвет фона страницы, чтобы скрыть линию под самолетом */
  padding: 0 5px;
  font-size: 12px;
}

.route-line .plane-icon {
  position: absolute;
  left: 50%;
  transform: translate(-50%, -50%);
  top: 50%;
  font-size: 14px;
  background: transparent;
  padding: 0 4px;
}



.price-tag {
  font-size: 1.3rem;
  font-weight: bold;
  color: #4ade80;
}

.pnr-label {
  font-size: 0.7rem;
  opacity: 0.6;
  margin-bottom: 5px;
}

.curr {
  font-size: 0.9rem;
}

.times {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.7);
}

.ai-card {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  background: linear-gradient(145deg, rgba(100,108,255,0.2),
  rgba(0,0,0,0)); border: 1px solid rgba(100,108,255,0.3);
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.advices-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  text-align: left;
}

.advice-item {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  padding: 12px;
  border-left: 3px solid #6366f1;
}

.advice-category {
  font-size: 0.75rem;
  text-transform: uppercase;
  color: #818cf8;
  margin-bottom: 5px;
  display: flex;
  gap: 5px;
}

.advice-text {
  font-size: 0.85rem;
  line-height: 1.4;
  margin: 0;
  color: #e2e8f0;
}

.btn-ai-generate {
  background: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
  color: white;
  border: none;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
  transition: 0.3s;
}

.btn-ai-generate:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.btn-ai-generate:disabled {
  background: #4a5568;
  cursor: not-allowed;
}

.ai-loading {
  text-align: center;
  padding: 20px 0;
}
.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid rgba(255,255,255,0.1);
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 10px;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .ticket-content {
    flex-direction: column;
  }

  .route-line {
    max-width: none;
  }
}

.buttons-row {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-start;
}
.empty-state .btn-action.secondary {
  background: transparent;
  border: 1px dashed rgba(255,255,255,0.12);
}



</style>