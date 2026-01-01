<template>
  <div class="landing">
    <header class="hero-card">
      <span class="logo-emoji">🌍</span>

      <h1 class="title">
        Планируй путешествия<br />
        без хаоса и забытых идей
      </h1>

      <p class="subtitle">
        Сохраняй места, маршруты и идеи поездок.<br />
        Мы поможем всё структурировать и напомним в нужный момент.
      </p>

      <!-- СЦЕНАРИЙ -->
      <div class="story">
        <p>✈️ Нашёл интересное место</p>
        <p>⭐ Добавил в избранное</p>
        <p>🧠 Получил подробное описание и идеи</p>
        <p>⏰ Не забыл о поездке</p>
      </div>

      <!-- ВОЗМОЖНОСТИ -->
      <div class="features">
        <div class="feature">
          <span>📍</span>
          <p>Сохраняй города, места и идеи для путешествий</p>
        </div>
        <div class="feature">
          <span>🤖</span>
          <p>Получай умные описания и советы с помощью AI</p>
        </div>
        <div class="feature">
          <span>⭐</span>
          <p>Формируй список избранного и возвращайся к нему позже</p>
        </div>
        <div class="feature">
          <span>🔔</span>
          <p>Получай напоминания, чтобы ничего не забыть</p>
        </div>
      </div>

      <!-- CTA -->
      <div class="actions">
        <router-link to="/register" class="btn primary">
          Начать планировать бесплатно
        </router-link>
        <router-link to="/login" class="btn secondary">
          У меня уже есть аккаунт
        </router-link>
      </div>

      <p class="hint">
        Регистрация займёт меньше минуты
      </p>
    </header>
  </div>
</template>


<style scoped>
.landing {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f2027, #203a43, #2c5364);
  padding: 20px;
}

/* ОСНОВНАЯ КАРТОЧКА */
.hero-card {
  background: rgba(20, 40, 50, 0.85); /* темнее, чем было */
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);

  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 28px;

  padding: 60px 40px;
  text-align: center;
  max-width: 640px;
  width: 100%;

  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.45);

  /* 🔴 КЛЮЧЕВОЕ */
  color: #ffffff;
}

.logo-emoji {
  font-size: 64px;
  display: block;
  margin-bottom: 24px;
}

/* ЗАГОЛОВОК */
.title {
  font-size: 3.4rem;
  font-weight: 800;
  margin-bottom: 20px;
  line-height: 1.15;
  color: #ffffff;
}

/* ПОДЗАГОЛОВОК */
.subtitle {
  font-size: 1.15rem;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 36px;
  line-height: 1.6;
}

/* STORY */
.story {
  margin-bottom: 36px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.story p {
  margin: 0;
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.75);
}

/* FEATURES */
.features {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 40px;
}

.feature {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 14px;
  padding: 14px 16px;

  display: flex;
  gap: 10px;
  align-items: flex-start;

  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.85);
}

.feature span {
  font-size: 1.2rem;
  flex-shrink: 0;
}

/* CTA */
.actions {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-bottom: 16px;
}

.btn {
  padding: 14px 32px;
  border-radius: 14px;
  text-decoration: none;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
  display: inline-block;
}

.primary {
  background: #646cff;
  color: #ffffff;
  box-shadow: 0 6px 18px rgba(100, 108, 255, 0.4);
}

.primary:hover {
  background: #535bf2;
  transform: translateY(-2px);
}

.secondary {
  border: 1px solid rgba(255, 255, 255, 0.35);
  color: #ffffff;
  background: transparent;
}

.secondary:hover {
  background: rgba(255, 255, 255, 0.12);
}

/* ПОДСКАЗКА */
.hint {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.6);
}

/* МОБИЛКА */
@media (max-width: 480px) {
  .features {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column;
  }

  .title {
    font-size: 2.4rem;
  }

  .hero-card {
    padding: 40px 20px;
  }
}

</style>