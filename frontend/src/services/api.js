import axios from 'axios';

const DEFAULT_RENDER_API_BASE_URL = 'https://worklog-backend-xts0.onrender.com/api';

const resolveBaseUrl = () => {
  const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
  if (configuredBaseUrl) {
    return configuredBaseUrl;
  }

  if (import.meta.env.DEV) {
    // Use the Vite dev proxy in local development.
    return '/api';
  }

  return DEFAULT_RENDER_API_BASE_URL;
};

const api = axios.create({
  baseURL: resolveBaseUrl().replace(/\/$/, ''),
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;
