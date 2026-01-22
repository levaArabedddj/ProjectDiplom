import axios from "axios";

const api = axios.create({
    baseURL: "https//triplevad.duckdns.org/api",
    withCredentials: false,
});

api.interceptors.request.use(config => {
    const token = localStorage.getItem('jwt')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

export default api
