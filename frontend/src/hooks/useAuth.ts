import { useState } from "react";
import type { LoginResponse, LoginRequest, UserRole } from "../types/types";
import { api } from "../api/api";

interface AuthUser {
  username: string;
  role: UserRole;
}

interface UseAuthReturn {
  user: AuthUser | null;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  isAdmin: boolean;
}

export const useAuth = (): UseAuthReturn => {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : null;
  });

  const login = async (credentials: LoginRequest): Promise<void> => {
    const { data } = await api.post<LoginResponse>("/auth/login", credentials);

    localStorage.setItem("token", data.token);
    localStorage.setItem(
      "user",
      JSON.stringify({ username: data.username, role: data.role }),
    );
    setUser({ username: data.username, role: data.role });
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
  };

  const isAdmin = user?.role === ("ADMIN" as UserRole);

  return { user, login, logout, isAdmin };
};
