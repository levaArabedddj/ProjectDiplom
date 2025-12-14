import { defineStore } from 'pinia'
import api from '../api/axios'
import { jwtDecode } from 'jwt-decode'


export const useUserStore = defineStore('user', {
    state: () => ({
        user: null,
        token: localStorage.getItem('jwt') || null,
        isAuth: !!localStorage.getItem('jwt'),
        userId: null,
        role: null,
    }),

    getters: {
        getUser: (state) => state.user,
        getRole: (state) => state.role,
        isLoggedIn: (state) => state.isAuth,
    },

    actions: {
        /** 🔓 LOGIN */
        async login(username, password) {
            try {
                const res = await api.post('/auth/signin', {
                    userName: username,
                    password,
                })

                const token = res.data.token || res.data
                this.setToken(token)

                return true
            } catch (e) {
                console.error('Login error:', e)
                return false
            }
        },

        /** 📝 REGISTER */
        async register(data) {
            try {
                const res = await api.post('/auth/signup-Login', data)
                const token = res.data.token || res.data
                this.setToken(token)

                return true
            } catch (e) {
                console.error('Registration error:', e)
                return false
            }
        },

        /** 🧠 ДЕКОДИРУЕМ JWT */
        setToken(token) {
            this.token = token
            this.isAuth = true
            localStorage.setItem('jwt', token)

            const decoded = jwtDecode(token)
            this.userId = decoded.id
            this.role = decoded.role
        },

        /** 👤 ЗАГРУЗКА ПРОФИЛЯ */
        async fetchUser() {
            try {
                const res = await api.get('/api/preferences/me')
                this.user = res.data
            } catch (e) {
                console.warn('Failed to fetch user:', e)
                this.logout()
            }
        },

        /** 🚪 LOGOUT */
        logout() {
            this.user = null
            this.token = null
            this.isAuth = false
            this.userId = null
            this.role = null
            localStorage.removeItem('jwt')
        },
    },
})
