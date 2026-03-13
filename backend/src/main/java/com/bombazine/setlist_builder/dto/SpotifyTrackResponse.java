package com.bombazine.setlist_builder.dto;

@Deprecated
public record SpotifyTrackResponse(
        String spotifyId,
        String name,
        int durationSeconds,
        String formattedDuration,
        boolean alreadySaved,
        int popularity) {
}
