import { getAccessToken, updateAccessToken } from '@/context/UserContext';
import { UserService } from '@/services/UserService';
import axios, { InternalAxiosRequestConfig } from 'axios';

interface CustomAxiosRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

export const api = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  withCredentials: true,
});

export const authApi = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
  paramsSerializer: (params) => {
    const searchParams = new URLSearchParams();

    Object.entries(params).forEach(([key, value]) => {
      if (Array.isArray(value)) {
        value.forEach((v) => {
          searchParams.append(key, v); 
        });
      } else if (value !== undefined && value !== null) {
        searchParams.append(key, String(value));
      }
    });

    return searchParams.toString();
  },
});

authApi.interceptors.request.use(
  (config: CustomAxiosRequestConfig) => {
    const accessToken = getAccessToken();
    if (accessToken && !config._retry) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

authApi.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config as CustomAxiosRequestConfig;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const res = await UserService.refreshAccessToken();
        const newAccessToken = res.data?.accessToken;
        if (newAccessToken) {
          updateAccessToken(newAccessToken);
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return authApi(originalRequest)
        }
      } catch (refreshError) {
        updateAccessToken(null);
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
