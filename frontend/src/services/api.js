import axios from 'axios';

const resolveDefaultBaseUrl = () => {
  if (import.meta.env.DEV) {
    // Use the Vite dev proxy in local development to avoid brittle CORS issues.
    return '/api';
  }

  if (typeof window === 'undefined') {
    return 'http://localhost:8080/api';
  }

  const { hostname, protocol } = window.location;
  return `${protocol}//${hostname || 'localhost'}:8080/api`;
};

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL || resolveDefaultBaseUrl();

const api = axios.create({
  baseURL: configuredBaseUrl.replace(/\/$/, ''),
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
