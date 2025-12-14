<template>
  <div class="landing">
    <header class="hero">
      <h1>Добро пожаловать 👋</h1>
      <p>Сервис для планирования путешествий и напоминаний</p>

      <div class="actions">
        <router-link to="/login" class="btn primary">
          Войти
        </router-link>
        <router-link to="/register" class="btn secondary">
          Регистрация
        </router-link>
      </div>
    </header>
  </div>
</template>

<style scoped>
.landing {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2563eb, #1e293b);
  color: white;
}

.hero {
  text-align: center;
  max-width: 500px;
}

.actions {
  margin-top: 30px;
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn {
  padding: 12px 24px;
  border-radius: 12px;
  text-decoration: none;
  font-weight: 600;
}

.primary {
  background: white;
  color: #2563eb;
}

.secondary {
  border: 2px solid white;
  color: white;
}
</style>
