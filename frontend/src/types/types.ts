// Auth

export type UserRole = "ADMIN" | "MEMBER";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: UserRole;
}

// Songs
export type SongSource = "LASTFM" | "SPOTIFY" | "MANUAL" | "ARRANGEMENT";

export interface SongResponse {
  id: string;
  name: string;
  durationSeconds: number;
  formattedDuration: string;
  fromSpotify: boolean;
  popularity: number;
  source: SongSource;
}

export interface CreateSongRequest {
  name: string;
  durationSeconds: number;
  originalSongId?: string;
}

export interface UpdateSongRequest {
  name: string;
  durationSeconds: number;
}

export interface LastFmTrackResponse {
  name: string;
  rank: number;
  playcount: number;
  listeners: number;
  popularity: number;
}

export interface ImportSummary {
  imported: number;
  skipped: number;
  total: number;
}

// Setlists
export interface SetlistResponse {
  id: string;
  venueName: string;
  eventDate: string;
  songs: SongResponse[];
  totalDurationSeconds: number;
  formattedDuration: string;
}

export interface CreateSetlistRequest {
  venueName: string;
  eventDate: string;
  songIds: string[];
}

export interface UpdateSetlistRequest {
  venueName: string;
  eventDate: string;
  songIds: string[];
}

export interface GenerateSetlistRequest {
  venueName: string;
  eventDate: string;
  targetDurationSeconds: number;
}

// Users (admin)
export interface UserResponse {
  id: string;
  username: string;
  role: UserRole;
  createdAt: string;
}

export interface ResetPasswordRequest {
  newPassword: string;
}
