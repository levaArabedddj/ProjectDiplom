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

      <div class="divider">або</div>

      <a href="/api/oauth2/authorization/google" class="google-btn">
        <div class="icon-wrapper">
          <svg viewBox="0 0 24 24" width="24" height="24" xmlns="http://www.w3.org/2000/svg">
            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.84z" fill="#FBBC05"/>
            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
          </svg>
        </div>
        <span class="btn-text">Продовжити з Google</span>
      </a>

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
.actions-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.divider {
  display: flex;
  align-items: center;
  color: rgba(255, 255, 255, 0.4);
  font-size: 0.85rem;
  margin: 4px 0;
}
.divider::before, .divider::after {
  content: "";
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.15);
}
.divider::before { margin-right: 10px; }
.divider::after { margin-left: 10px; }

/* Стиль для кнопки Google */
.google-btn {
  background: #ffffff;
  color: #1f2937; /* Темно-серый текст */
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  position: relative;
  transition: transform 0.2s, box-shadow 0.2s;
}

.google-btn:hover {
  background: #f8fafc; /* Чуть серее при наведении */
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.google-icon {
  flex-shrink: 0;
}

/* Адаптив для кнопок */
@media (max-width: 480px) {
  .actions {
    flex-direction: column;
  }
}
/* --- КНОПКА GOOGLE (DARK GLASS STYLE) --- */
.google-btn {
  /* Форма та розмір */
  width: 90%;
  height: 52px;
  border-radius: 50px; /* Овальна форма */

  /* 👇 ГОЛОВНА ЗМІНА: Темний прозорий фон замість білого */
  background-color: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.2); /* Тонка рамка */

  /* Текст стає білим */
  color: #ffffff;
  font-family: 'Roboto', sans-serif;
  font-weight: 500;
  font-size: 16px;
  text-decoration: none;

  /* Вирівнювання */
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 10px;

  /* Ефекти */
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

/* Ефект при наведенні (стає світлішою) */
.google-btn:hover {
  background-color: rgba(255, 255, 255, 0.15); /* Більш помітна */
  border-color: rgba(255, 255, 255, 0.5); /* Рамка світиться */
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
}

/* Ефект натискання */
.google-btn:active {
  background-color: rgba(255, 255, 255, 0.05);
  transform: translateY(0);
}

/* Біла підкладка під іконку (опціонально, щоб логотип G краще читався) */
.icon-wrapper {
  background: white;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2px;
}

</style>