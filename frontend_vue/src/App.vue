<script setup>
//import Header from '@/components/Header.vue'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
</script>

<template>
  <Header v-if="!store.token" />

  <router-view />
</template>
