<template>
  <div class="sa-page">
    <div class="container">

      <header class="top">
        <button @click="$router.push('/main')" class="back-btn">← Назад на головну</button>
        <h1>Інтелектуальні пропозиції — Розширені</h1>
        <p class="subtitle">Додавайте свої бажання, вмикайте/вимикайте їх і отримуйте персоналізовані рекомендації.</p>
      </header>

      <div class="controls">
        <div class="left">
          <div class="card form-card">
            <h3>Додати бажання</h3>

            <label>Тип бажання</label>
            <select v-model="form.type">
              <option disabled value="">Виберіть тип</option>
              <option v-for="opt in typeOptions" :key="opt" :value="opt">{{ opt }}</option>
            </select>

            <label>Текст бажання</label>
            <input v-model="form.value" placeholder="Наприклад: романтичне місце біля моря" />

            <div class="row-actions">
              <button class="btn primary" :disabled="adding" @click="addPreference">
                {{ adding ? 'Додавання...' : 'Додати' }}
              </button>
              <button class="btn ghost" @click="resetForm">Очистити</button>
            </div>

            <p class="hint"></p>
          </div>

          <div class="card action-card">
            <div class="toggle-view">
              <label>
                <input type="checkbox" v-model="showList" />
                Переглянути свої бажання
              </label>
              <button class="btn small" @click="fetchPreferences">Оновити список</button>
            </div>

            <div v-if="showList" class="prefs-list">
              <div v-if="loadingPrefs" class="center">Завантаження…</div>
              <div v-else-if="preferences.length === 0" class="muted">Список порожній</div>

              <ul v-else class="list">
                <li v-for="p in preferences" :key="p.id" class="pref-item">
                  <div class="left-info">
                    <div class="type">{{ p.type }}</div>
                    <div class="value">{{ p.value }}</div>
                  </div>

                  <div class="right-actions">
                    <button
                        class="btn toggle"
                        :class="{ on: p.active }"
                        @click="togglePreference(p)"
                    >
                      {{ p.active ? 'Участь: так' : 'Участь: ні' }}
                    </button>

                    <button
                        class="btn small ghost"
                        @click="removePreferencePlaceholder(p)"
                    >
                      ✖
                    </button>
                  </div>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div class="right">
          <div class="card recommend-card">
            <h3>Генерація рекомендацій</h3>
            <p class="muted">Система використає активні бажання для формування списку місць.</p>

            <button
                class="btn primary large"
                :disabled="loadingRecommendations || !hasActivePreferences"
                @click="requestRecommendations"
            >
              {{ loadingRecommendations ? 'Аналізуємо...' : (hasRequested ? 'Отримати нові рекомендації' : 'Отримати рекомендації') }}
            </button>

            <p v-if="loadingRecommendations" class="wait-note">Підождiть будь ласка, аналіз може зайняти кілька хвилин…</p>
            <p v-else-if="!hasActivePreferences" class="muted">Увімкніть хоча б одне бажання щоб отримати рекомендації.</p>

            <div v-if="lastRequestTime" class="last-run">Останній запит: {{ lastRequestTime }}</div>
          </div>
        </div>
      </div>

      <!-- Results area -->
      <section v-if="recommendations.length" class="results-area">
        <h2>Результати рекомендацій</h2>
        <div class="results-grid">
          <div v-for="(r, idx) in recommendations" :key="idx" class="result-card">
            <h4>{{ r.place }} <small v-if="r.country">— {{ r.country }}</small></h4>
            <p class="reason">{{ r.reason }}</p>

            <div class="result-actions">
              <!-- More details (placeholder) -->
              <button class="btn small" @click="moreDetailsPlaceholder(r)">
                Більше деталей
              </button>

              <!-- Add to favorites -->
              <button
                  class="btn small primary"
                  :disabled="r.addingFavorite || r.favorited"
                  @click="addToFavorites(r, idx)"
              >
                <span v-if="r.addingFavorite">Додаємо...</span>
                <span v-else-if="r.favorited">Додано</span>
                <span v-else>Додати в улюблені</span>
              </button>
            </div>
          </div>
        </div>
      </section>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()

// === ENDPOINTS (подкорректируй если в бэке по-другому) ===
const ENDPOINT = {
  GET_PREFS: '/api/preferences/preference',
  POST_PREF: '/api/preferences/preference',
  TOGGLE_PREF: (id) => `/api/preferences/preferences/${id}/toggle`,
  POST_RECOMMEND: '/api/preferences/ai/recommendations'
}

// === UI state ===
const form = ref({ type: '', value: '' })
const typeOptions = ['REGION', 'MOOD','ACTIVITY', 'FOOD', 'BUDGET', 'COMMENT', 'AVOID']

const preferences = ref([])
const showList = ref(true)
const loadingPrefs = ref(false)
const adding = ref(false)

const loadingRecommendations = ref(false)
const recommendations = ref([])
const hasRequested = ref(false)
const lastRequestTime = ref(null)

// helper computed
const hasActivePreferences = computed(() =>
    preferences.value.some(p => p.active)
)

// initial fetch
onMounted(() => {
  fetchPreferences()
})

function goHome() {
  router.push('/main')
}

function resetForm() {
  form.value.type = ''
  form.value.value = ''
}

// === API methods ===
async function fetchPreferences() {
  loadingPrefs.value = true
  try {
    const res = await api.get(ENDPOINT.GET_PREFS)
    preferences.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    console.error('fetchPreferences error', e)
    alert('Не удалось загрузить желания')
  } finally {
    loadingPrefs.value = false
  }
}

async function addPreference() {
  if (!form.value.type || !form.value.value || form.value.value.trim() === '') {
    alert('Заполните тип и текст желания')
    return
  }

  adding.value = true
  try {
    const payload = { type: form.value.type, value: form.value.value }
    const res = await api.post(ENDPOINT.POST_PREF, payload)

    if (res?.data?.id) {
      preferences.value.unshift(res.data)
    } else {
      await fetchPreferences()
    }

    resetForm()
  } catch (e) {
    console.error('addPreference error', e)
    alert('Ошибка при добавлении желания')
  } finally {
    adding.value = false
  }
}

async function togglePreference(pref) {
  const prev = pref.active
  pref.active = !pref.active

  try {
    await api.put(ENDPOINT.TOGGLE_PREF(pref.id))
  } catch (e) {
    pref.active = prev
    console.error('toggle error', e)
    alert('Не удалось переключить желание')
  }
}


function removePreferencePlaceholder(pref) {
  if (!confirm('Удалить желание локально? (Реальное удаление требует реализации бэка)')) return
  preferences.value = preferences.value.filter(p => p.id !== pref.id)
}

// === Recommendations ===
async function requestRecommendations() {
  if (hasRequested.value) {
    const ok = confirm('Вы уверены, что хотите получить новые рекомендации? Это затронет лимиты и перезапишет текущие результаты.')
    if (!ok) return
  }

  const payload = {
    preferences: preferences.value.map(p => ({
      id: p.id,
      type: p.type,
      value: p.value,
      active: p.active
    }))
  }

  loadingRecommendations.value = true
  try {
    const res = await api.post(ENDPOINT.POST_RECOMMEND, payload)

    const data = res.data || {}

    // нормализуем
    recommendations.value = Array.isArray(data.recommendations) ? data.recommendations : []
    hasRequested.value = true
    lastRequestTime.value = new Date().toLocaleString()
  } catch (e) {
    console.error('requestRecommendations error', e)
    const serverMsg = e?.response?.data?.message || ''
    alert('Ошибка при получении рекомендаций. ' + (serverMsg || 'Попробуйте позже'))
  } finally {
    loadingRecommendations.value = false
  }
}

async function addToFavorites(r, idx) {
  if (r.favorited || r.addingFavorite) return

  const payload = {
    name: r.place || '',
    country: r.country || ''
  }

  r.addingFavorite = true
  try {
    await api.post('/api/preferences/favoriteRegion', payload)

    r.favorited = true
    alert(`"${r.place}" додано в фаворити`)
  } catch (err) {
    console.error('addToFavorites error', err)
    const msg = err?.response?.data?.message || 'Помилка при додаванні в улюблені'
    alert(msg)
  } finally {
    r.addingFavorite = false
  }
}

// placeholder
function moreDetailsPlaceholder(r) {
  alert('Здесь будет запрос деталей для: ' + r.place)
}
</script>

<style scoped>
.sa-page {
  min-height: 100vh;
  background: linear-gradient(135deg,#0f2027,#203a43,#2c5364);
  color: #fff;
  padding: 32px;
  font-family: Inter, system-ui, Arial;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
}
.top {
  position: relative;
  text-align: center;
  margin-bottom: 22px;
}
.back {
  position: absolute;
  left: 0;
  top: 0;
  background: none;
  border: none;
  color: #6dd3ff;
  font-size: 14px;
  cursor: pointer;
}
.subtitle { opacity: 0.85; margin-top: 6px; }

.controls {
  display: flex;
  gap: 20px;
  margin-bottom: 28px;
}
.left { flex: 1.2; display: flex; flex-direction: column; gap: 20px; }
.right { flex: 0.6; }

.card {
  background: rgba(255,255,255,0.06);
  padding: 18px;
  border-radius: 12px;
}
.form-card input, .form-card select {
  width: 100%;
  padding: 10px;
  margin: 8px 0 12px 0;
  border-radius: 8px;
  border: none;
  background: rgba(255,255,255,0.04);
  color: #fff;
}
.row-actions { display:flex; gap:8px; align-items:center; }
.btn {
  padding: 10px 14px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  background: rgba(255,255,255,0.06);
  color: #fff;
}
.btn.primary { background: #6dd3ff; color: #012; font-weight:700; }
.btn.large { padding: 14px 18px; font-size: 16px; }
.btn.small { padding:6px 8px; border-radius:8px; font-size:13px; }
.btn.ghost { background: transparent; border: 1px solid rgba(255,255,255,0.06); }
.card.action-card .toggle-view { display:flex; justify-content:space-between; align-items:center; gap:8px; }
.prefs-list .list { margin-top:12px; padding:0; list-style:none; }
.pref-item { display:flex; justify-content:space-between; align-items:center; padding:10px; border-radius:8px; background: rgba(0,0,0,0.2); margin-bottom:8px; }
.pref-item .type { font-weight:700; font-size:13px; opacity:0.9; }
.pref-item .value { font-size:14px; opacity:0.9; }
.pref-item .right-actions { display:flex; gap:8px; align-items:center; }

.recommend-card { text-align:center; }
.wait-note { margin-top:12px; color:#ffd; opacity:0.9; }
.last-run { margin-top:10px; color:#bcd; font-size:13px; }

.results-area { margin-top:24px; }
.results-grid { display:grid; grid-template-columns: repeat(auto-fill, minmax(260px,1fr)); gap:12px; margin-top:12px; }
.result-card { background: rgba(255,255,255,0.06); padding:14px; border-radius:10px; }
.result-card h4 { margin:0 0 6px 0; }
.reason { opacity:0.9; font-size:14px; margin-bottom:10px; }
.center { text-align:center; padding:18px; }
.muted { opacity:0.75; font-size:13px; }
</style>
