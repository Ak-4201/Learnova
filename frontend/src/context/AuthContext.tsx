import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { authApi, AuthUser } from '../api/auth';

interface AuthContextType {
  user: AuthUser | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  signup: (email: string, password: string, fullName: string, role?: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

const STORAGE_USER = 'user';
const STORAGE_TOKEN = 'token';

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const loadStored = useCallback(() => {
    const t = localStorage.getItem(STORAGE_TOKEN);
    const u = localStorage.getItem(STORAGE_USER);
    if (t && u) {
      try {
        setToken(t);
        setUser(JSON.parse(u));
      } catch {
        localStorage.removeItem(STORAGE_TOKEN);
        localStorage.removeItem(STORAGE_USER);
      }
    } else {
      setToken(null);
      setUser(null);
    }
    setIsLoading(false);
  }, []);

  useEffect(() => {
    loadStored();
    const onStorage = () => loadStored();
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, [loadStored]);

  const login = useCallback(async (email: string, password: string) => {
    const { data } = await authApi.login(email, password);
    const u: AuthUser = {
      email: data.email,
      fullName: data.fullName,
      role: data.role,
      userId: data.userId,
    };
    localStorage.setItem(STORAGE_TOKEN, data.token);
    localStorage.setItem(STORAGE_USER, JSON.stringify(u));
    setToken(data.token);
    setUser(u);
  }, []);

  const signup = useCallback(
    async (email: string, password: string, fullName: string, role?: string) => {
      const { data } = await authApi.signup(email, password, fullName, role);
      const u: AuthUser = {
        email: data.email,
        fullName: data.fullName,
        role: data.role,
        userId: data.userId,
      };
      localStorage.setItem(STORAGE_TOKEN, data.token);
      localStorage.setItem(STORAGE_USER, JSON.stringify(u));
      setToken(data.token);
      setUser(u);
    },
    []
  );

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_TOKEN);
    localStorage.removeItem(STORAGE_USER);
    setToken(null);
    setUser(null);
  }, []);

  const value: AuthContextType = {
    user,
    token,
    login,
    signup,
    logout,
    isAuthenticated: !!token && !!user,
    isLoading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
