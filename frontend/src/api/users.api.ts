import type {
  UserResponse,
  RegisterRequest,
  ResetPasswordRequest,
} from "../types/types";
import { api } from "./api";

export const usersApi = {
  getAll: () =>
    api.get<UserResponse[]>("/auth/users").then((response) => response.data),

  create: (data: RegisterRequest) =>
    api.post<void>("auth/register", data).then((response) => response.data),

  resetPassword: (id: string, data: ResetPasswordRequest) =>
    api.put<void>(`/auth/users/${id}/password`, data),

  delete: (id: string) => api.delete(`/auth/users/${id}`),
};
