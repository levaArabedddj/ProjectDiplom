<template>
  <div class="landing">
    <header class="hero-card">
      <span class="logo-emoji">🌍</span>

      <h1 class="title">
        Плануй подорожі<br />
        без хаосу та забутих ідей
      </h1>

      <p class="subtitle">
        Зберігай місця, маршрути та ідеї для подорожей.<br />
        Ми допоможемо все структурувати та нагадаємо у потрібний момент.
      </p>

      <div class="story">
        <p>✈️ Знайшов цікаве місце</p>
        <p>⭐ Додав у збережене</p>
        <p>🧠 Отримав детальний опис та ідеї</p>
        <p>⏰ Не забув про подорож</p>
      </div>

      <div class="features">
        <div class="feature">
          <span>📍</span>
          <p>Зберігай міста, місця та ідеї для подорожей</p>
        </div>
        <div class="feature">
          <span>🤖</span>
          <p>Отримуй розумні описи та поради за допомогою AI</p>
        </div>
        <div class="feature">
          <span>⭐</span>
          <p>Формуй список обраного та повертайся до нього пізніше</p>
        </div>
        <div class="feature">
          <span>🔔</span>
          <p>Отримуй нагадування, щоб нічого не забути</p>
        </div>
      </div>

      <div class="actions">
        <router-link to="/register" class="btn primary">
          Почати планувати безкоштовно
        </router-link>
        <router-link to="/login" class="btn secondary">
          У мене вже є акаунт
        </router-link>
      </div>

      <p class="hint">
        Реєстрація займе менше хвилини
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