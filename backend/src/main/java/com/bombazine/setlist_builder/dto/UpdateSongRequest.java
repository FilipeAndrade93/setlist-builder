package com.bombazine.setlist_builder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSongRequest(@NotBlank String name, @NotNull @Min(0) Integer durationSeconds) {
}
