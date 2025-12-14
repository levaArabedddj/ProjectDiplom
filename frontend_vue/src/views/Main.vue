<template>
  <div class="main-page">
    <div class="container">
      <h1 class="title">Головна сторінка</h1>

      <div class="grid">
        <div class="tile">
          <span>🤖</span>
          <p>Інтелектуальні пропозиції</p>
        </div>

        <div class="tile">
          🔍
          <p>Пошук рейсів / готелів</p>
        </div>

        <div class="tile">
          <span>💾</span>
          <p>Збережені дані</p>
        </div>

        <div class="tile">
          <span>🗺</span>
          <p>Картки подорожей</p>
        </div>

        <div class="tile">
          <span>📊</span>
          <p>Порівняння регіонів</p>
        </div>

        <div class="tile">
          <span>➕</span>
          <p>Створити подорож</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.main-page {
  min-height: 100vh;
  background: linear-gradient(
      135deg,
      #0f2027,
      #203a43,
      #2c5364
  );
  padding: 60px 40px;
  font-family: "Inter", "Segoe UI", sans-serif;
}

/* Контейнер — широкий, как сайт */
.container {
  max-width: 1200px;
  margin: 0 auto;
}

.title {
  text-align: center;
  font-size: 40px;
  font-weight: 700;
  margin-bottom: 60px;
  color: #ffffff;
}

/* СЕТКА ДЛЯ ДЕСКТОПА */
.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
}

/* Карточки */
.tile {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(12px);
  border-radius: 22px;
  padding: 48px 32px;
  text-align: center;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.25);
}

.tile:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.35);
}

.tile span {
  font-size: 46px;
  display: block;
}

.tile p {
  margin-top: 18px;
  font-size: 18px;
  font-weight: 500;
}

/* Планшеты */
@media (max-width: 1024px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* Телефоны */
@media (max-width: 640px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .title {
    font-size: 28px;
  }
}


</style>
