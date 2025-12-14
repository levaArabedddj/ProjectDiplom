<script setup>
//import Header from '@/components/Header.vue'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
</script>

<template>
  <!-- Header показываем ТОЛЬКО если юзер не авторизован -->
  <Header v-if="!store.token" />

  <!-- сюда подставляются страницы -->
  <router-view />
</template>
