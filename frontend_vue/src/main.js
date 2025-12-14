import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import './style.css'

import { plugin as FormKitPlugin, defaultConfig } from "@formkit/vue";

const app = createApp(App);

app.use(router);
app.use(createPinia());
app.use(FormKitPlugin, defaultConfig);

app.mount("#app");
