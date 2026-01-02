<template>
  <Teleport to="body">
    <div class="overlay" @keydown.esc="closeOnEsc" tabindex="-1">
      <div class="modal-card" @click.stop>
        <header class="modal-header">
          <h3>Подбор мест</h3>
          <button class="close-btn" @click="close">✖</button>
        </header>

        <div class="modal-body">
          <div class="controls">
            <input
                v-model="localCity"
                placeholder="Город (например: Berlin)"
                @keyup.enter="fetchPlaces"
            />
            <button :disabled="loading" @click="fetchPlaces">
              {{ loading ? 'Загружаем…' : 'Найти места' }}
            </button>
          </div>

          <div v-if="error" class="error">{{ error }}</div>

          <div v-if="loading" class="muted">Идет поиск…</div>

          <div v-if="places && places.length" class="places-list">
            <div
                v-for="(p, idx) in places"
                :key="p.id ?? idx"
                class="place-item"
            >
              <div class="place-main">
                <h4>{{ p.name || p.title || p.category || 'Без названия' }}</h4>
                <div v-if="p.description" class="desc" v-html="p.description"></div>
              </div>

              <div class="actions">
                <button
                    @click="addToFavorites(p, idx)"
                    :disabled="saving[idx]"
                    class="btn"
                >
                  {{ saving[idx] ? 'Сохраняем…' : 'Добавить в избранное' }}
                </button>
              </div>
            </div>
          </div>

          <div v-else-if="!loading" class="muted">Ничего не найдено</div>
        </div>

        <footer class="modal-footer">
          <button class="btn ghost" @click="close">Закрыть</button>
        </footer>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import api from '@/api/axios'
import { ref, watch, onMounted, onUnmounted } from 'vue'

// props / emits
const props = defineProps({
  city: { type: String, default: '' }
})
const emits = defineEmits(['close', 'added'])

const localCity = ref(props.city || '')
const loading = ref(false)
const error = ref(null)
const places = ref([])
const saving = ref({})

function handleKeydown(e) {
  if (e.key === 'Escape') {
    close()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  if (localCity.value) fetchPlaces()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})

watch(() => props.city, (v) => { if (v) localCity.value = v })

function close() {
  emits('close')
}

function closeOnEsc() {
  close()
}

async function fetchPlaces() {
  if (!localCity.value || localCity.value.trim().length < 2) {
    error.value = 'Введите город для поиска'
    return
  }
  loading.value = true
  error.value = null
  places.value = []
  try {
    const res = await api.get('/api/trips/places', { params: { city: localCity.value } })
    let data = res.data
    // Нормализация данных (как в твоем коде)
    if (typeof data === 'string') {
      try { data = JSON.parse(data) } catch (e) {}
    }
    if (Array.isArray(data)) {
      places.value = data
    } else if (data && data.data && Array.isArray(data.data)) {
      places.value = data.data
    } else if (data && data.results && Array.isArray(data.results)) {
      places.value = data.results
    } else if (data && data.activities && Array.isArray(data.activities)) {
      places.value = data.activities
    } else {
      places.value = data ? [data] : []
    }
  } catch (e) {
  console.error('fetchPlaces', e)

  // Проверяем, есть ли детальное сообщение от сервера
  if (e.response && e.response.data && e.response.data.message) {
    // Если сервер прислал "Cannot invoke...", заменим это на понятный текст
    if (e.response.data.message.includes("Jackson") || e.response.data.error === "SERVER_ERROR") {
      error.value = "Сервіс пошуку тимчасово недоступний для цього міста. Спробуйте інше."
    } else {
      error.value = e.response.data.message
    }
  } else {
    error.value = 'Помилка запиту до сервера'
  }
} finally {
    loading.value = false
  }
}

async function addToFavorites(place, idx) {
  saving.value = { ...saving.value, [idx]: true }
  try {
    const dto = {
      name: place.name || place.title || place.category || 'Unknown',
      country: place.country || place.address?.country || null
    }
    await api.post('/api/preferences/favoriteRegion', dto)

    emits('added', {
      id: null,
      name: dto.name,
      country: dto.country
    })
    close()
  } catch (e) {
    console.error('addToFavorites', e)
    error.value = 'Не удалось добавить в избранное'
  } finally {
    saving.value = { ...saving.value, [idx]: false }
  }
}
</script>

<style scoped>
/* ВАЖНО: z-index очень большой, чтобы перекрыть все меню */
.overlay {
  position: fixed;
  inset: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99999;
  background: rgba(0,0,0,0.7);
  backdrop-filter: blur(4px); /* Размытие фона заднего плана */
}

.modal-card {
  width: min(900px, 95%);
  max-height: 85vh;
  overflow-y: auto;
  background: #1a2233; /* Чуть светлее черного, чтобы отличалось */
  color: #e6f3ff;
  border-radius: 12px;
  box-shadow: 0 30px 60px rgba(0,0,0,0.9);
  padding: 20px;
  border: 1px solid rgba(255,255,255,0.1);
  position: relative;
}

.modal-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 10px; }
.close-btn { background:transparent; border:none; color:inherit; font-size:1.5rem; cursor:pointer; }
.controls { display:flex; gap:10px; margin-bottom:15px; }
.controls input { flex:1; padding:10px; border-radius:8px; border:1px solid rgba(255,255,255,0.1); background: rgba(0,0,0,0.3); color:inherit; }
.controls button { padding:10px 16px; border-radius:8px; cursor:pointer; background: #646cff; color: white; border: none; font-weight: bold;}
.controls button:disabled { opacity: 0.6; cursor: not-allowed; }

.places-list { display:flex; flex-direction:column; gap:12px; margin-top:10px; }
.place-item { display:flex; justify-content:space-between; gap:15px; padding:12px; border-radius:8px; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.05); }
.place-main h4 { margin: 0 0 5px 0; color: #a5b4fc; }
.place-main .desc { margin:0; opacity:0.8; font-size:0.9rem; }

.actions .btn { padding:8px 12px; border-radius:6px; cursor:pointer; background:rgba(100,108,255,0.2); color:#a5b4fc; border:1px solid rgba(100,108,255,0.3); font-size: 0.85rem; white-space: nowrap; }
.actions .btn:hover { background:rgba(100,108,255,0.4); }

.muted { color: #9aa4b2; text-align: center; margin: 20px 0; }
.error { color: #ff8888; margin-bottom:10px; background: rgba(255,0,0,0.1); padding: 8px; border-radius: 6px;}
.modal-footer { display:flex; justify-content:flex-end; margin-top:20px; pt: 10px; border-top: 1px solid rgba(255,255,255,0.1); }
.btn.ghost { background:transparent; border:1px solid rgba(255,255,255,0.2); color:inherit; padding:8px 16px; border-radius:8px; cursor: pointer;}
.btn.ghost:hover { background: rgba(255,255,255,0.1); }
</style>