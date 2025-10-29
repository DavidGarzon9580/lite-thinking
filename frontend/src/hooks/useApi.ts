import axios from 'axios';
import { useMemo } from 'react';
import { useAuth } from '../context/AuthContext';

export const useApi = () => {
  const { token, logout } = useAuth();

  const client = useMemo(() => {
    const baseURL = (import.meta.env.VITE_API_URL ?? '/api').replace(/\/$/, '');
    const instance = axios.create({
      baseURL
    });

    instance.interceptors.request.use((config) => {
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    instance.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          logout();
        }
        return Promise.reject(error);
      }
    );

    return instance;
  }, [token, logout]);

  return client;
};
