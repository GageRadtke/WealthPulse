import axios from "axios";


export const TOKEN_KEY = "wealthPulse.jwt";

// Authentication ------------------------------------------------------------
export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  window.location.reload();
}

const apiClient = axios.create({
  baseURL: `${import.meta.env.VITE_API_URL || "http://localhost:8283"}/api`,
  withCredentials: true
});

// Request handling: attach the token and let the browser set multipart headers.
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY);

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    if (config.data instanceof FormData) {
      delete config.headers["Content-Type"];
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Response handling: a rejected token ends the local session consistently.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearSession();
    }

    return Promise.reject(error);
  }
);

export default apiClient;
