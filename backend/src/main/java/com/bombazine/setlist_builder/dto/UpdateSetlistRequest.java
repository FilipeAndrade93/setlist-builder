package com.bombazine.setlist_builder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateSetlistRequest(@NotBlank(message = "Venue name required") String venueName, @NotNull(message = "Event date required")
                                   LocalDate eventDate, @NotEmpty(message = "Setlist must have at least one song")List<UUID> songIds) {
}
