import type {
  SetlistResponse,
  CreateSetlistRequest,
  UpdateSetlistRequest,
  GenerateSetlistRequest,
} from "../types/types";
import { api } from "./api";

export const setlistsApi = {
  getAll: () => api.get<SetlistResponse[]>("/setlists").then((r) => r.data),

  getById: (id: string) =>
    api.get<SetlistResponse>(`/setlists/${id}`).then((r) => r.data),

  create: (data: CreateSetlistRequest) =>
    api.post<SetlistResponse>("/setlists", data).then((r) => r.data),

  update: (id: string, data: UpdateSetlistRequest) =>
    api.put<SetlistResponse>(`/setlists/${id}`, data).then((r) => r.data),

  generate: (data: GenerateSetlistRequest) =>
    api.post<SetlistResponse>("/setlists/generate", data).then((r) => r.data),

  delete: (id: string) => api.delete(`/setlists/${id}`),

  downloadPdf: (id: string) =>
    api
      .get(`/setlists/${id}/pdf`, { responseType: "blob" })
      .then((r) => r.data),
};
