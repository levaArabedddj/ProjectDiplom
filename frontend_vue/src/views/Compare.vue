<template>
  <div class="compare-page">
    <div class="container">

      <!-- HEADER -->
      <div class="header">
        <button class="back" @click="goHome">← Повернутися на головну</button>
        <h1>Порівняння регіонів</h1>
      </div>

      <!-- FORM -->
      <div class="form-card">
        <div class="row">
          <div>
            <label>Регіон 1</label>
            <input v-model="placeOne" placeholder="Наприклад: Рим" />
          </div>

          <div>
            <label>Регіон 2</label>
            <input v-model="placeTwo" placeholder="Наприклад: Барселона" />
          </div>
        </div>

        <div class="row">
          <label>Додатковий коментар (необовʼязково)</label>
          <textarea
              v-model="comment"
              placeholder="Що для вас важливо при порівнянні?"
          />
        </div>

        <button class="btn primary" :disabled="loading" @click="compare">
          {{ loading ? 'Порівняння...' : 'Порівняти' }}
        </button>
      </div>

      <!-- RESULT -->
      <div v-if="result" class="result">

        <div class="grid">
          <!-- PLACE 1 PROS -->
          <div class="box">
            <h3>Плюси відпочинку у {{ result.place1.name }}</h3>
            <p v-if="result.place1.suitability" class="note">
              {{ result.place1.suitability }}
            </p>
            <ul>
              <li v-for="(p, i) in result.place1.pros" :key="'p1p'+i">
                {{ p }}
              </li>
              <li v-if="!result.place1.pros.length">— немає даних</li>
            </ul>
            <div v-if="result.place1.score !== null" class="score">
              Оцінка: {{ result.place1.score }}/10
            </div>
          </div>

          <!-- PLACE 1 CONS -->
          <div class="box">
            <h3>Мінуси відпочинку у {{ result.place1.name }}</h3>
            <ul>
              <li v-for="(m, i) in result.place1.cons" :key="'p1c'+i">
                {{ m }}
              </li>
              <li v-if="!result.place1.cons.length">— немає даних</li>
            </ul>
          </div>

          <!-- PLACE 2 PROS -->
          <div class="box">
            <h3>Плюси відпочинку у {{ result.place2.name }}</h3>
            <p v-if="result.place2.suitability" class="note">
              {{ result.place2.suitability }}
            </p>
            <ul>
              <li v-for="(p, i) in result.place2.pros" :key="'p2p'+i">
                {{ p }}
              </li>
              <li v-if="!result.place2.pros.length">— немає даних</li>
            </ul>
            <div v-if="result.place2.score !== null" class="score">
              Оцінка: {{ result.place2.score }}/10
            </div>
          </div>

          <!-- PLACE 2 CONS -->
          <div class="box">
            <h3>Мінуси відпочинку у {{ result.place2.name }}</h3>
            <ul>
              <li v-for="(m, i) in result.place2.cons" :key="'p2c'+i">
                {{ m }}
              </li>
              <li v-if="!result.place2.cons.length">— немає даних</li>
            </ul>
          </div>
        </div>

        <!-- SUMMARY -->
        <div class="summary">
          <h3>Загальний висновок</h3>
          <p>
            {{ result.summary || result.recommended }}
          </p>
          <div v-if="result.recommended" class="recommended">
            ⭐ Рекомендовано: <strong>{{ result.recommended }}</strong>
          </div>
        </div>

        <button class="btn success">
          Створити карту відпочинку для {{ result.recommended }}
        </button>

      </div>
    </div>
  </div>
</template>


<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'

const router = useRouter()

const placeOne = ref('')
const placeTwo = ref('')
const comment = ref('')
const loading = ref(false)
const result = ref(null)

function goHome() {
  router.push('/main')
}

/* ===== helpers ===== */
function ensureArray(val) {
  if (!val) return []
  if (Array.isArray(val)) return val
  if (typeof val === 'string') {
    return val
        .split(/\r?\n|•|−|–|—|- /)
        .map(v => v.trim())
        .filter(Boolean)
  }
  return []
}

function normalizeResponse(raw) {
  const p1 = raw.place1 || {}
  const p2 = raw.place2 || {}

  const place1 = {
    name: p1.name || placeOne.value,
    pros: ensureArray(p1.pros),
    cons: ensureArray(p1.cons),
    suitability: p1.suitability || '',
    score: p1.recommendationScore ?? null
  }

  const place2 = {
    name: p2.name || placeTwo.value,
    pros: ensureArray(p2.pros),
    cons: ensureArray(p2.cons),
    suitability: p2.suitability || '',
    score: p2.recommendationScore ?? null
  }

  let recommended = raw.final_recommendation || raw.recommended || null

  if (!recommended && place1.score !== null && place2.score !== null) {
    recommended = place1.score >= place2.score ? place1.name : place2.name
  }

  return {
    place1,
    place2,
    summary: raw.final_recommendation || '',
    recommended
  }
}

/* ===== API ===== */
async function compare() {
  if (!placeOne.value || !placeTwo.value) {
    alert('Вкажіть обидва регіони')
    return
  }

  try {
    loading.value = true
    const res = await api.post('/api/preferences/compare', {
      place_one: placeOne.value,
      place_two: placeTwo.value,
      comment: comment.value
    })

    result.value = normalizeResponse(res.data)
  } catch (e) {
    console.error(e)
    alert('Помилка порівняння')
  } finally {
    loading.value = false
  }
}
</script>


<style scoped>
.compare-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
  padding: 40px;
  color: white;
}

.container {
  max-width: 1100px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.back {
  background: none;
  border: none;
  color: #6dd3ff;
  cursor: pointer;
  font-size: 14px;
}

.form-card {
  background: rgba(255,255,255,0.08);
  padding: 24px;
  border-radius: 16px;
  margin-bottom: 32px;
}

.row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.row textarea {
  grid-column: 1 / -1;
  min-height: 90px;
}

label {
  display: block;
  margin-bottom: 6px;
  font-weight: 600;
}

input, textarea {
  width: 100%;
  padding: 10px;
  border-radius: 8px;
  border: none;
  background: rgba(255,255,255,0.1);
  color: white;
}

.btn {
  padding: 12px 18px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
}

.btn.primary {
  background: #6dd3ff;
  color: #012;
  font-weight: 700;
}

.btn.success {
  margin-top: 20px;
  background: #22c55e;
  color: white;
}

.result .grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.box {
  background: rgba(255,255,255,0.08);
  padding: 18px;
  border-radius: 14px;
}

.summary {
  margin-top: 24px;
  background: rgba(255,255,255,0.1);
  padding: 18px;
  border-radius: 14px;
}
</style>
