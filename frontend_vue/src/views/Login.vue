<template>
  <div class="auth-container">
    <h2>Авторизация</h2>

    <form @submit.prevent="handleLogin">
      <FormKit
          type="text"
          label="Username"
          v-model="userName" />


      <FormKit
          type="password"
          label="Пароль"
          v-model="password"
          validation="required"
      />

      <button :disabled="store.loading" class="btn">
        Войти
      </button>
    </form>

    <p v-if="store.error" class="error">{{ store.error }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { useRouter } from 'vue-router'

const store = useUserStore()
const router = useRouter()

const email = ref("")
const password = ref("")

const userName = ref("");

async function handleLogin() {
  const ok = await store.login(userName.value, password.value)
  if (ok) router.push("/home")
}
</script>

<style scoped>
.auth-container {
  max-width: 400px;
  margin: 40px auto;
}
.error {
  color: red;
}
.btn {
  margin-top: 10px;
}
</style>
