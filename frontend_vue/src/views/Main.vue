<template>
  <div class="main-page">
    <div class="container">
      <h1 class="title">Головна сторінка</h1>

      <div class="grid">
        <div class="tile" @click="router.push('/home')">
          <span>🏠</span>
          <p> На головну</p>
        </div>

        <div class="tile" @click="$router.push('/suggestions')">
          <span>🤖</span>
          <p>Інтелектуальні пропозиції</p>
        </div>
        <div class="tile">
          🔍
          <p>Пошук рейсів / готелів</p>
        </div>

        <div class="tile" @click="$router.push('/saved')">
          <span>💾</span>
          <p>Збережені дані</p>
        </div>

        <div class="tile">
          <span>🗺</span>
          <p>Картки подорожей</p>
        </div>

        <div class="tile" @click="goToCompare">
          <span>📊</span>
          <p>Порівняння регіонів</p>
        </div>

        <div class="tile" @click="openCreateTrip">
          <span>➕</span>
          <h3>Створити подорож</h3>
          <p>Заплануйте нову поїздку</p>
        </div>
      </div>
      <CreateTripModal
          v-if="showCreateTrip"
          @close="showCreateTrip = false"
      />

    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import CreateTripModal from '@/components/CreateTripModal.vue'

const showCreateTrip = ref(false)

function openCreateTrip() {
  showCreateTrip.value = true
}

const router = useRouter()

function goToCompare() {
  router.push('/compare')
}


</script>

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

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
}

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

@media (max-width: 1024px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .title {
    font-size: 28px;
  }
}
.back-button {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  padding: 10px 20px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(-5px);
}


</style>
