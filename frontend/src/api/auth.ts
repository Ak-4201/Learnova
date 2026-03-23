export interface AuthUser {
  email: string;
  fullName: string;
  role: string;
  userId: number;
}

export interface AuthResponse {
  token: string;
  email: string;
  fullName: string;
  role: string;
  userId: number;
}

import api from './client';

export const authApi = {
  login: (email: string, password: string) =>
    api.post<AuthResponse>('/auth/login', { email, password }),

  signup: (
    email: string,
    password: string,
    fullName: string,
    role?: string
  ) =>
    api.post<AuthResponse>('/auth/signup', {
      email,
      password,
      fullName,
      ...(role ? { role } : {}),
    }),
};
