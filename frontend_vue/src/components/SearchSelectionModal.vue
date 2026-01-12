<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <h2>Що ви шукаєте?</h2>
      <div class="selection-grid">
        <div class="selection-item" @click="select('avion')">
          <span class="icon">✈️</span>
          <p>Авіаквитки</p>
        </div>
        <div class="selection-item" @click="select('hotels')">
          <span class="icon">🏨</span>
          <p>Готелі</p>
        </div>
      </div>
      <button class="close-btn" @click="$emit('close')">Скасувати</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const emit = defineEmits(['close'])

function select(type) {
  emit('close')
  if (type === 'avion') {
    router.push('/avion')
  } else {
    router.push('/hotels')
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.modal-content {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 40px;
  border-radius: 30px;
  text-align: center;
  color: white;
  max-width: 500px;
  width: 90%;
}

.selection-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin: 30px 0;
}

.selection-item {
  background: rgba(255, 255, 255, 0.15);
  padding: 30px 20px;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.selection-item:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-5px);
}

.icon {
  font-size: 50px;
  margin-bottom: 10px;
  display: block;
}

.close-btn {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 10px 20px;
  border-radius: 12px;
  cursor: pointer;
  margin-top: 10px;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}
</style>