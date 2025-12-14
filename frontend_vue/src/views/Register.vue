<template>
  <div class="auth-container">
    <h2>Регистрация</h2>

    <form @submit.prevent="handleRegister">

      <!-- Username -->
      <FormKit
          type="text"
          label="Username"
          v-model="userName"
          validation="required"
      />

      <!-- Name -->
      <FormKit
          type="text"
          label="Имя"
          v-model="name"
          validation="required"
      />

      <!-- Surname -->
      <FormKit
          type="text"
          label="Фамилия"
          v-model="surName"
          validation="required"
      />

      <!-- Email -->
      <FormKit
          type="email"
          label="Email"
          v-model="gmail"
          validation="required|email"
      />

      <!-- Пароль -->
      <FormKit
          type="password"
          label="Пароль"
          v-model="password"
          validation="required"
      />

      <!-- Gender -->
      <FormKit
          type="select"
          label="Пол"
          v-model="gender"
          :options="[
            { label: 'Мужчина', value: 'male' },
            { label: 'Женщина', value: 'female' },
            { label: 'Небинарный', value: 'non_binary' }
          ]"
          validation="required"
      />

      <!-- Phone -->
      <FormKit
          type="text"
          label="Телефон"
          v-model="phone"
          validation="required"
      />

      <!-- Security word -->
      <FormKit
          type="password"
          label="Секретное слово"
          v-model="securityWord"
          validation="required"
      />

      <button :disabled="store.loading" class="btn">
        Зарегистрироваться
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

// Все поля из SignupRequest
const userName = ref("")
const name = ref("")
const surName = ref("")
const gmail = ref("")
const password = ref("")
const gender = ref("male")
const phone = ref("")
const securityWord = ref("")

async function handleRegister() {
  const ok = await store.register({
    userName: userName.value,
    name: name.value,
    surName: surName.value,
    gmail: gmail.value,
    password: password.value,
    gender: gender.value,
    phone: phone.value,
    securityWord: securityWord.value
  });

  if (ok) router.push("/questionnaire");
}
</script>

<style scoped>
.auth-container {
  max-width: 450px;
  margin: 40px auto;
}
.error {
  color: red;
}
.btn {
  margin-top: 10px;
}
</style>
