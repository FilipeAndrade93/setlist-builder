import type {
  ImportSummary,
  LastFmTrackResponse,
  CreateSongRequest,
  SongResponse,
  UpdateSongRequest,
} from "../types/types";
import { api } from "./api";

export const songsApi = {
  getAll: () => api.get<SongResponse[]>("/songs").then((r) => r.data),

  create: (data: CreateSongRequest) =>
    api.post<SongResponse>("/songs", data).then((r) => r.data),

  update: (id: string, data: UpdateSongRequest) =>
    api.put<SongResponse>(`/songs/${id}`, data).then((r) => r.data),

  delete: (id: string) => api.delete(`/songs/${id}`),

  getLastFmTopTracks: () =>
    api
      .get<LastFmTrackResponse[]>("/songs/lastfm/top-tracks")
      .then((r) => r.data),

  saveFromLastFm: (trackName: string) =>
    api
      .post<SongResponse>(`/songs/lastfm/${encodeURIComponent(trackName)}`)
      .then((r) => r.data),

  importFromLastFm: () =>
    api.post<ImportSummary>("/songs/lastfm/import").then((r) => r.data),

  syncLastFm: () => api.post("/songs/lastfm/sync"),
};
