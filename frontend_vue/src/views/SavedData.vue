<template>
  <div class="saved-page">
    <div class="container">
      <header class="top">
        <button class="back" @click="goHome">← Головна</button>
        <h1>Збережені місця</h1>
        <p class="subtitle">Тут зберігаються місця, які ви додали в обране</p>
      </header>

      <section class="content-card">
        <div v-if="loading" class="center">Завантаження...</div>

        <div v-else>
          <div v-if="favorites.length === 0" class="empty">
            Список порожній — додайте місце до обраного
          </div>

          <!-- ====== FAVORITES LIST ====== -->
          <ul v-if="favorites.length" class="favorites-list">
            <li v-for="f in favorites" :key="f.id" class="fav-item">
              <div class="left">
                <div class="name">{{ f.name }}</div>
                <div class="meta" v-if="f.country">{{ f.country }}</div>
              </div>

              <div class="right">
                <button
                    class="btn small"
                    :disabled="detailsLoadingGlobal"
                    @click="viewDetails(f)"
                >
                  <span v-if="detailsLoadingGlobal">Завантаження…</span>
                  <span v-else>Переглянути</span>
                </button>


                <button class="btn small ghost" @click="deleteFavorite(f)">
                  Видалити
                </button>
              </div>
            </li>
          </ul>


          <div v-if="detailsContainers.length" class="details-separator">
            Деталі вибраних місць
          </div>

          <div v-if="detailsContainers.length" class="details-stack">
            <div
                v-for="(d, idx) in detailsContainers"
                :key="d.containerId"
                class="detail-card"
            >
              <div class="detail-header">
                <div>
                  <strong>{{ d.name }}</strong>
                  <span v-if="d.country" class="meta"> — {{ d.country }}</span>
                </div>

                <div class="detail-actions">
                  <!-- сворачивать/разворачивать, не удалять -->
                  <button class="btn small ghost" @click="toggleCollapse(d.containerId)">
                    {{ d.collapsed ? 'Розгорнути' : 'Свернути' }}
                  </button>
                </div>
              </div>

              <!-- когда свернут — скрываем тело -->
              <div v-if="!d.collapsed">
                <div v-if="d.loading" class="detail-body center">Завантаження деталей…</div>

                <div v-else-if="d.error" class="detail-body error">
                  Помилка: {{ d.error }}
                </div>

                <div v-else class="detail-body">
                  <p class="desc" v-if="d.data.description">{{ d.data.description }}</p>

                  <div class="two-cols">
                    <div>
                      <b>Найкращий сезон:</b>
                      <div>{{ d.data.bestSeason || '—' }}</div>
                      <b>Приблизний бюджет:</b>
                      <div>{{ d.data.averageBudget || '—' }}</div>
                    </div>

                    <div>
                      <b>Мови:</b>
                      <div>{{ d.data.languages || '—' }}</div>
                      <b>Безпека:</b>
                      <div>{{ d.data.safety || '—' }}</div>
                    </div>
                  </div>

                  <div v-if="Array.isArray(d.data.attractions) && d.data.attractions.length" class="list-block">
                    <b>Достопримечательності / місця:</b>
                    <ul>
                      <li v-for="(a, i) in d.data.attractions" :key="i">{{ a }}</li>
                    </ul>
                  </div>

                  <div v-if="d.data.transportInside && Object.keys(d.data.transportInside).length" class="list-block">
                    <b>Транспорт всередині:</b>
                    <ul>
                      <li v-for="(val, key) in d.data.transportInside" :key="key">{{ key }}: {{ val }}</li>
                    </ul>
                  </div>

                  <div v-if="d.data.freeActivities && d.data.freeActivities.length" class="list-block">
                    <b>Безкоштовні активності:</b>
                    <ul>
                      <li v-for="(f, i) in d.data.freeActivities" :key="i">{{ f }}</li>
                    </ul>
                  </div>

                  <div v-if="d.data.accommodation && Object.keys(d.data.accommodation).length" class="list-block">
                    <b>Проживання (приблизно):</b>
                    <ul>
                      <li v-for="(val, key) in d.data.accommodation" :key="key">{{ key }}: {{ val }}</li>
                    </ul>
                  </div>

                  <div v-if="d.data.travelTips && d.data.travelTips.length" class="list-block">
                    <b>Поради:</b>
                    <ul>
                      <li v-for="(t, i) in d.data.travelTips" :key="i">{{ t }}</li>
                    </ul>
                  </div>


                </div>
              </div>
            </div>
          </div>

        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'


const ENDPOINTS = {
  FETCH_FAVORITES: '/api/preferences/favorites',
  DELETE_FAVORITE: (id) => `/api/preferences/favorites/${id}`,
  PLACE_DETAILS: (id) => `/api/preferences/places/${id}/details`,
  SAVE_FAVORITE: '/api/preferences/favoriteRegion'
}

const router = useRouter()
const loading = ref(false)
const favorites = ref([])
const detailsLoadingGlobal = ref(false)

const detailsContainers = ref([])
const loadingById = ref({})

function goHome() { router.push('/main') }

async function fetchFavorites() {
  loading.value = true
  try {
    const res = await api.get(ENDPOINTS.FETCH_FAVORITES)
    favorites.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    console.error('fetchFavorites', e)
    alert('Не удалось загрузить сохранённые места')
  } finally {
    loading.value = false
  }
}


function makeContainerId(placeId) {
  return `${placeId}-${Date.now()}-${Math.random().toString(36).slice(2,8)}`
}

function findContainerByPlaceId(placeId) {
  return detailsContainers.value.find(c => c.placeId === placeId)
}


async function viewDetails(place) {
  // если уже идёт загрузка — ничего не делаем
  if (detailsLoadingGlobal.value) return

  detailsLoadingGlobal.value = true

  const existing = findContainerByPlaceId(place.id)
  if (existing) {
    existing.collapsed = false
       return
  }

  loadingById.value = { ...loadingById.value, [place.id]: true }

  try {
    const res = await api.get(ENDPOINTS.PLACE_DETAILS(place.id))
    const payload = res.data || {}

    // создаём контейнер ТОЛЬКО после получения данных
    const container = {
      containerId: makeContainerId(place.id),
      placeId: place.id,
      name: place.name,
      country: place.country,
      loading: false,
      saving: false,
      collapsed: false, // по умолчанию развернут
      error: null,
      data: {
        description: payload.description || '',
        bestSeason: payload.bestSeason || '',
        averageBudget: payload.averageBudget || '',
        attractions: payload.attractions || [],
        rule: payload.rule || '',
        weatherBySeason: payload.weatherBySeason || {},
        howToGet: payload.howToGet || '',
        transportInside: payload.transportInside || {},
        freeActivities: payload.freeActivities || [],
        accommodation: payload.accommodation || {},
        travelTips: payload.travelTips || [],
        safety: payload.safety || '',
        languages: payload.languages || ''
      }
    }

    detailsContainers.value.push(container)

  } catch (err) {
    console.error('viewDetails error', err)

    const errorContainer = {
      containerId: makeContainerId(place.id),
      placeId: place.id,
      name: place.name,
      country: place.country,
      loading: false,
      saving: false,
      collapsed: false,
      error: err?.response?.data?.message || err.message || 'Помилка при отриманні даних',
      data: {}
    }
    detailsContainers.value.push(errorContainer)
  } finally {
    const copy = { ...loadingById.value }
    delete copy[place.id]
    loadingById.value = copy
    detailsLoadingGlobal.value = false
  }
}

function toggleCollapse(containerId) {
  const c = detailsContainers.value.find(x => x.containerId === containerId)
  if (!c) return
  c.collapsed = !c.collapsed
}


async function deleteFavorite(fav) {
  const ok = confirm(`Видалити "${fav.name}" з обраних?`)
  if (!ok) return
  const prev = [...favorites.value]
  favorites.value = favorites.value.filter(x => x.id !== fav.id)
  try {
    await api.delete(ENDPOINTS.DELETE_FAVORITE(fav.id))
  } catch (e) {
    console.warn('deleteFavorite failed, rolling back or backend endpoint missing', e)
    favorites.value = prev
    alert('Не вдалося видалити на сервері')
  }
}

// initial load
fetchFavorites()
</script>


<style scoped>
.saved-page { min-height: 100vh; background: linear-gradient(135deg,#0f2027,#203a43,#2c5364); color: white; padding: 40px; }
.container { max-width: 1000px; margin: 0 auto; }
.top { display:flex; gap:20px; align-items:center; margin-bottom:20px; }
.back { background:none; border:none; color:#6dd3ff; cursor:pointer; }
.subtitle { opacity:0.85; margin-top:6px; }
.content-card { background: rgba(255,255,255,0.04); padding:20px; border-radius:12px; }
.center { text-align:center; padding:30px 0; }
.empty { padding:30px; opacity:0.8 }

.details-stack { display:flex; flex-direction:column; gap:12px; margin-bottom:18px; }
.detail-card { background: rgba(255,255,255,0.03); padding:14px; border-radius:10px; border:1px solid rgba(255,255,255,0.03); }
.detail-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.detail-actions { display:flex; gap:8px; }
.detail-body { color: #e6f3ff; }
.detail-body.error { color: #ffb4b4; }
.desc { margin-bottom:8px; opacity:0.95; }

.two-cols { display:flex; gap:16px; margin-bottom:8px; }
.two-cols > div { flex:1; }

.list-block { margin-top:8px; }
.list-block ul { margin:6px 0 0 20px; }
.detail-footer { display:flex; gap:8px; margin-top:10px; }

.favorites-list { list-style:none; padding:0; margin:0; display:grid; gap:12px; }
.fav-item { display:flex; justify-content:space-between; align-items:center; padding:12px; background: rgba(255,255,255,0.03); border-radius:10px; }
.left .name { font-weight:700; }
.left .meta { font-size:13px; opacity:0.8; }
.btn { padding:8px 12px; border-radius:8px; border:none; cursor:pointer; background:#6dd3ff; color:#012; font-weight:600; }
.btn.ghost { background:transparent; color:#fff; border:1px solid rgba(255,255,255,0.06); }
.btn.small { padding:6px 10px; font-size:13px; }
.details-separator {margin: 24px 0 12px;padding-top: 12px;border-top: 1px solid rgba(255,255,255,0.08);opacity: 0.8;}

</style>
