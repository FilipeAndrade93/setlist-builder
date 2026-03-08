package com.bombazine.setlist_builder.dto;

import com.bombazine.setlist_builder.entity.Song;

import java.util.UUID;

public record SongResponse(UUID id, String name, int durationSeconds, String formattedDuration, boolean fromSpotify, int popularity, String source) {

    public static SongResponse from(Song song) {
        return new SongResponse(
                song.getId(),
                song.getName(),
                song.getDurationSeconds(),
                song.getFormattedDuration(),
                song.isFromSpotify(),
                song.getPopularity(),
                song.getSource().name().toLowerCase()
        );
    }
}
