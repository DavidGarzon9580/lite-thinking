import axios from 'axios';

type LoginRequest = {
  email: string;
  password: string;
};

type RegisterRequest = LoginRequest & {
  role: 'ADMIN' | 'VIEWER';
};

type TokenResponse = {
  token: string;
  expiresInMinutes: number;
};

export const login = async (payload: LoginRequest): Promise<TokenResponse> => {
  const baseURL = (import.meta.env.VITE_API_URL ?? '/api').replace(/\/$/, '');
  const { data } = await axios.post<TokenResponse>(`${baseURL}/auth/login`, payload);
  return data;
};

export const register = async (payload: RegisterRequest): Promise<TokenResponse> => {
  const baseURL = (import.meta.env.VITE_API_URL ?? '/api').replace(/\/$/, '');
  const { data } = await axios.post<TokenResponse>(`${baseURL}/auth/register`, payload);
  return data;
};
