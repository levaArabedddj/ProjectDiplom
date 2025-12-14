<template>
  <div class="prefs-page">
    <div class="panel">
      <h1>Короткий опитувальник</h1>
      <p class="subtitle">Допоможіть нам краще зрозуміти ваші вподобання для персональних рекомендацій</p>

      <form @submit.prevent="onSubmit" class="form">
        <div class="row">
          <label>Ім'я</label>
          <input v-model="form.username" required />
        </div>

        <div class="row three-cols">
          <div>
            <label>Країна</label>
            <input v-model="form.country" required />
          </div>
          <div>
            <label>Місто</label>
            <input v-model="form.city" />
          </div>
          <div>
            <label>Бюджет (прибл., в USD)</label>
            <input v-model.number="form.budget" type="number" min="0" />
          </div>
        </div>

        <div class="row">
          <label>Улюблені місця (через кому)</label>
          <input v-model="favoritePlacesInput" placeholder="Наприклад: Paris, Kyiv, Bali" />
        </div>

        <div class="row three-cols">
          <div>
            <label>Тривалість подорожі</label>
            <select v-model="form.preferredTripDuration">
              <option value="">Вибрати...</option>
              <option value="короткий (2-3 дня)">короткий (2-3 дня)</option>
              <option value="середній (1 неделя)">середній (1 неделя)</option>
              <option value="довгий (10+ дней)">довгий (10+ дней)</option>
            </select>
          </div>

          <div>
            <label>Транспорт</label>
            <select v-model="form.transportPreference">
              <option value="">Вибрати...</option>
              <option>самолет</option>
              <option>поезд</option>
              <option>авто</option>
              <option>автобус</option>
            </select>
          </div>

          <div>
            <label>Хто подорожує</label>
            <select v-model="form.travelCompanion">
              <option value="">Вибрати...</option>
              <option>один</option>
              <option>пара</option>
              <option>сім'я</option>
              <option>друзі</option>
            </select>
          </div>
        </div>

        <div class="row">
          <label>Інтереси (через кому)</label>
          <input v-model="interestsInput" placeholder="спорт, музеї, пляж" />
        </div>

        <div class="row two-cols">
          <div>
            <label>Тип розміщення</label>
            <select v-model="form.accommodationType">
              <option value="Hotel">Hotel</option>
              <option value="Hostel">Hostel</option>
              <option value="Resort">Resort</option>
              <option value="Villa">Villa</option>
              <option value="Guesthouse">Guesthouse</option>
            </select>
          </div>

          <div>
            <label>Частота подорожей (в рік)</label>
            <input v-model.number="form.traver_frequency" type="number" min="0" />
          </div>
        </div>

        <!-- Динамические списки: visitedPlaces -->
        <div class="block">
          <h3>Місця, які ви відвідали (і оцінка 1-10)</h3>
          <div v-for="(row, idx) in visited" :key="'v-' + idx" class="place-row">
            <input v-model="row.name" placeholder="Місто/Країна" />
            <input v-model.number="row.rating" type="number" min="1" max="10" placeholder="1-10" />
            <button type="button" @click="removeVisited(idx)" class="btn small">✖</button>
          </div>
          <button type="button" class="btn" @click="addVisited">Додати відвідане місце</button>
        </div>

        <div class="block">
          <h3>Місця, які не сподобались</h3>
          <div v-for="(row, idx) in disliked" :key="'d-' + idx" class="place-row">
            <input v-model="row.name" placeholder="Місто/Країна" />
            <input v-model.number="row.rating" type="number" min="1" max="10" placeholder="1-10" />
            <button type="button" @click="removeDisliked(idx)" class="btn small">✖</button>
          </div>
          <button type="button" class="btn" @click="addDisliked">Додати незадовільне місце</button>
        </div>

        <div class="actions">
          <button class="btn primary" type="submit" :disabled="loading">
            {{ loading ? "Зберігання..." : "Зберегти та отримати рекомендації" }}
          </button>
        </div>

        <p class="hint">Після збереження вам буде надіслано електронного листа з персональними рекомендаціями.</p>
      </form>
    </div>

    <!-- Показываем результат GPT если пришёл -->
    <div v-if="result" class="result-panel">
      <h2>{{ result.greeting }}</h2>
      <ul>
        <li v-for="p in result.recommended_places" :key="p">{{ p }}</li>
      </ul>
      <button class="btn" @click="goToHome">Готово</button>
    </div>
  </div>
</template>

<script>
import api from "@/api/axios";
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";

export default {
  name: "Questionnaire",
  setup() {
    const form = reactive({
      username: "",
      country: "",
      city: "",
      favoritePlaces: "",
      preferredTripDuration: "",
      transportPreference: "",
      visitedPlaces: {},     // will be filled before submit as map
      dislikedPlaces: {},    // same
      travelCompanion: "",
      interests: "",
      accommodationType: "",
      budget: null,
      traver_frequency: 0
    });

    // inputs for comma-separated fields
    const favoritePlacesInput = ref("");
    const interestsInput = ref("");

    // dynamic rows
    const visited = ref([]);
    const disliked = ref([]);

    const loading = ref(false);
    const result = ref(null);

    // helpers for dynamic lists
    function addVisited() {
      visited.value.push({ name: "", rating: null });
    }
    function removeVisited(i) {
      visited.value.splice(i, 1);
    }
    function addDisliked() {
      disliked.value.push({ name: "", rating: null });
    }
    function removeDisliked(i) {
      disliked.value.splice(i, 1);
    }

    const router = useRouter();

    // Convert array rows to map { name: rating }
    function rowsToMap(rows) {
      const m = {};
      for (const r of rows) {
        if (r.name && r.name.trim() !== "" && r.rating) {
          m[r.name.trim()] = Number(r.rating);
        }
      }
      return m;
    }

    async function onSubmit() {
      try {
        loading.value = true;

        form.favoritePlaces = favoritePlacesInput.value;
        form.interests = interestsInput.value;

        form.visitedPlaces = rowsToMap(visited.value);
        form.dislikedPlaces = rowsToMap(disliked.value);

        const token = localStorage.getItem("jwt");

        await api.post("/api/preferences", form);


        // ✅ РЕДИРЕКТ ПОСЛЕ УСПЕХА
        router.push("/home");

        // или если у тебя Home.vue на другом пути:
        // router.push("/home");

      } catch (e) {
        console.error(e);
        alert("Помилка при відправці");
      } finally {
        loading.value = false;
      }
    }


    function goToHome() {
      result.value = null;
      // например router.push('/')
      // если используешь Vue Router: this.$router.push('/')
     // window.location.href = "/"; // или router push
    }

    return {
      form,
      favoritePlacesInput,
      interestsInput,
      visited,
      disliked,
      addVisited,
      removeVisited,
      addDisliked,
      removeDisliked,
      onSubmit,
      loading,
      result,
      goToHome
    };
  }
};
</script>

<style scoped>
.prefs-page {
  display: flex;
  gap: 32px;
  padding: 40px;
  min-height: 80vh;
}

/* левый панель — форма */
.panel {
  flex: 1 1 720px;
  background: rgba(255,255,255,0.06);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.4);
  color: #fff;
}
.panel h1 { margin: 0 0 6px; font-size: 28px; }
.subtitle { color: rgba(255,255,255,0.8); margin-bottom: 18px; }

.form .row { margin-bottom: 14px; display: flex; flex-direction: column; }
.form .row.three-cols { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.form .row.two-cols { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.form label { font-weight: 600; margin-bottom: 6px; color: #e6f3ff; }
.form input, .form select { padding: 10px 12px; border-radius: 8px; border: none; background: rgba(255,255,255,0.06); color: #fff; outline: none; }
.form input::placeholder { color: rgba(255,255,255,0.5); }

.block { margin-top: 18px; padding-top: 12px; border-top: 1px dashed rgba(255,255,255,0.04); }
.place-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.place-row input { flex: 1; }
.btn { background: rgba(255,255,255,0.08); color: #fff; padding: 8px 12px; border-radius: 8px; border: none; cursor: pointer; }
.btn.small { padding: 6px 8px; font-size: 12px; }
.btn.primary { background: linear-gradient(90deg,#6dd3ff,#6a9cff); color: #012; padding: 12px 16px; font-weight: 700; border-radius: 10px; }

/* правая панель результатов */
.result-panel {
  width: 380px;
  background: rgba(255,255,255,0.05);
  border-radius: 12px;
  padding: 18px;
  color: #fff;
}

.hint { color: rgba(255,255,255,0.7); margin-top: 10px; font-size: 13px; }

/* адаптив */
@media (max-width: 1000px) {
  .prefs-page { flex-direction: column; padding: 16px; }
  .result-panel { width: 100%; }
  .panel { width: 100%; }
}
</style>
