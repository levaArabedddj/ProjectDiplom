import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

import Landing from '@/views/Landing.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Main from '@/views/Main.vue'
import Home from '@/views/Home.vue'
import Questionnaire from "@/views/Questionnaire.vue";


const routes = [
    {
        path: '/',
        name: 'Landing',
        component: Landing
    },
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    {
        path: '/register',
        name: 'Register',
        component: Register
    },
    {
        path: '/home',
        name: 'Home',
        component: Home,
        meta: { requiresAuth: true }
    },
    {
        path: '/main',
        component: Main
    },
    {
        path: '/home',
        component: Home
    },
    {
        path: "/questionnaire",
        component: Questionnaire,
        meta: { requiresAuth: true }
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to) => {
    const store = useUserStore()

    // 🔐 защита приватных страниц
    if (to.meta.requiresAuth && !store.token) {
        return '/login'
    }

    // ❌ если авторизован — не пускаем на login/register
    if (
        (to.path === '/login' || to.path === '/register') &&
        store.token
    ) {
        return '/home'
    }
})

export default router
