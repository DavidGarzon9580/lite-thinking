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
  const { data } = await axios.post<TokenResponse>('/api/auth/login', payload);
  return data;
};

export const register = async (payload: RegisterRequest): Promise<TokenResponse> => {
  const { data } = await axios.post<TokenResponse>('/api/auth/register', payload);
  return data;
};
