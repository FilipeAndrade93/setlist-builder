package com.bombazine.setlist_builder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateSongRequest(@NotBlank(message = "Song name is required") String name, @Min(value = 1, message = "Duration must be at least 1 second") int durationSeconds) {
}
