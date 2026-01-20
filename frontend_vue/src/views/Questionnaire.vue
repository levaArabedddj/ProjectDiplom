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
              <option value="середній (1 неделя)">середній (1 тиждень)</option>
              <option value="довгий (10+ дней)">довгий (10+ днів)</option>
            </select>
          </div>

          <div>
            <label>Транспорт</label>
            <select v-model="form.transportPreference">
              <option value="">Вибрати...</option>
              <option>Літак</option>
              <option>Потяг</option>
              <option>Авто</option>
              <option>Автобус</option>
            </select>
          </div>

          <div>
            <label>Хто подорожує</label>
            <select v-model="form.travelCompanion">
              <option value="">Вибрати...</option>
              <option>Один</option>
              <option>Пара</option>
              <option>Сім'я</option>
              <option>Друзі</option>
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
            Зберегти та отримати рекомендації
          </button>
        </div>
      </form>
    </div>

    <transition name="fade">
      <div v-if="showOverlay" class="loading-overlay">
        <div class="overlay-content">
          <div class="spinner"></div>
          <h2>Ми зберігаємо ваші результати...</h2>
          <p class="wait-text">Це займе близько 10 секунд</p>

          <div class="tip-container">
            <p class="tip-label">Чи знали ви?</p>
            <transition name="slide-fade" mode="out-in">
              <p :key="currentTipIndex" class="tip-text">
                {{ tips[currentTipIndex] }}
              </p>
            </transition>
          </div>
        </div>
      </div>
    </transition>

  </div>
</template>

<script>
import api from "@/api/axios";
import { reactive, ref, onUnmounted } from "vue";
import { useRouter } from "vue-router";

export default {
  name: "Questionnaire",
  setup() {
    const router = useRouter();

    const form = reactive({
      username: "",
      country: "",
      city: "",
      favoritePlaces: "",
      preferredTripDuration: "",
      transportPreference: "",
      visitedPlaces: {},
      dislikedPlaces: {},
      travelCompanion: "",
      interests: "",
      accommodationType: "",
      budget: null,
      traver_frequency: 0
    });

    const favoritePlacesInput = ref("");
    const interestsInput = ref("");
    const visited = ref([]);
    const disliked = ref([]);

    // НОВЕ: Змінні для оверлею та порад
    const loading = ref(false);
    const showOverlay = ref(false);
    const currentTipIndex = ref(0);
    let tipInterval = null;

    // НОВЕ: Масив порад
    const tips = [
      "Після реєстрації ви можете авторизуватися в нашому Telegram-боті, використовуючи дані, які ви ввели тут.",
      "На нашому сайті ви можете не тільки планувати, а й замовляти квитки для вашої подорожі.",
      "Ми допомагаємо спланувати маршрут так, щоб ви побачили максимум цікавого за мінімум часу.",
      "У розділі 'Профіль' ви завжди можете змінити свої вподобання, якщо ваші інтереси зміняться.",
      "Слідкуйте за новими функціями — скоро ми додамо можливість бронювання готелів зі знижками!"
    ];

    // helpers
    function addVisited() { visited.value.push({ name: "", rating: null }); }
    function removeVisited(i) { visited.value.splice(i, 1); }
    function addDisliked() { disliked.value.push({ name: "", rating: null }); }
    function removeDisliked(i) { disliked.value.splice(i, 1); }

    function rowsToMap(rows) {
      const m = {};
      for (const r of rows) {
        if (r.name && r.name.trim() !== "" && r.rating) {
          m[r.name.trim()] = Number(r.rating);
        }
      }
      return m;
    }

    // НОВЕ: Логіка ротації порад
    function startTipsRotation() {
      currentTipIndex.value = 0;
      tipInterval = setInterval(() => {
        currentTipIndex.value = (currentTipIndex.value + 1) % tips.length;
      }, 3500); // Міняємо пораду кожні 3.5 секунди
    }

    function stopTipsRotation() {
      if (tipInterval) clearInterval(tipInterval);
    }

    async function onSubmit() {
      try {
        loading.value = true;

        // НОВЕ: Показуємо оверлей і запускаємо поради
        showOverlay.value = true;
        startTipsRotation();

        form.favoritePlaces = favoritePlacesInput.value;
        form.interests = interestsInput.value;
        form.visitedPlaces = rowsToMap(visited.value);
        form.dislikedPlaces = rowsToMap(disliked.value);

        // Відправка запиту
        await api.post("/api/preferences", form);

        // Якщо сервер відповів дуже швидко, дамо користувачу хоча б 1.5 сек почитати
        // Якщо сервер думав довго (GPT), то код піде далі відразу після відповіді
        // setTimeout тут не обов'язковий, але додає плавності
        setTimeout(() => {
          router.push("/home");
        }, 1500);

      } catch (e) {
        console.error(e);
        // Якщо помилка - ховаємо оверлей
        showOverlay.value = false;
        stopTipsRotation();
        alert("Помилка при відправці даних. Спробуйте ще раз.");
      } finally {
        // loading.value = false; // Не вимикаємо loading тут, щоб не миготіло перед переходом
      }
    }

    // Очистка інтервалу при знищенні компонента
    onUnmounted(() => {
      stopTipsRotation();
    });

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

      // НОВЕ
      showOverlay,
      tips,
      currentTipIndex
    };
  }
};
</script>

<style scoped>
/* Стилі основної сторінки */
.prefs-page {
  display: flex;
  gap: 32px;
  padding: 40px;
  min-height: 80vh;
  position: relative; /* Для позиціонування */
}

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
.btn.primary { background: linear-gradient(90deg,#6dd3ff,#6a9cff); color: #012; padding: 12px 16px; font-weight: 700; border-radius: 10px; transition: 0.3s; }
.btn.primary:disabled { opacity: 0.7; cursor: not-allowed; }

/* НОВЕ: Стилі для оверлею (Mini-screen) */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(15, 23, 42, 0.95); /* Темний фон з прозорістю */
  backdrop-filter: blur(10px); /* Ефект розмиття фону */
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  text-align: center;
  padding: 20px;
}

.overlay-content {
  max-width: 500px;
  animation: scaleUp 0.3s ease-out;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255,255,255,0.1);
  border-left-color: #6dd3ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

.loading-overlay h2 {
  font-size: 24px;
  color: #fff;
  margin-bottom: 10px;
}

.wait-text {
  color: #94a3b8;
  margin-bottom: 30px;
  font-size: 14px;
}

.tip-container {
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.1);
  padding: 20px;
  border-radius: 12px;
  min-height: 120px; /* Фіксована висота, щоб не стрибало */
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.tip-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #6dd3ff;
  margin-bottom: 10px;
  font-weight: bold;
}

.tip-text {
  font-size: 16px;
  line-height: 1.5;
  color: #e2e8f0;
}

/* Анімації */
@keyframes spin { 100% { transform: rotate(360deg); } }
@keyframes scaleUp { from { transform: scale(0.9); opacity: 0; } to { transform: scale(1); opacity: 1; } }

/* Анімація появи/зникнення оверлею */
.fade-enter-active, .fade-leave-active { transition: opacity 0.5s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* Анімація зміни тексту поради */
.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.5s ease; }
.slide-fade-enter-from { opacity: 0; transform: translateY(10px); }
.slide-fade-leave-to { opacity: 0; transform: translateY(-10px); }

@media (max-width: 1000px) {
  .prefs-page { flex-direction: column; padding: 16px; }
  .panel { width: 100%; }
}
</style>