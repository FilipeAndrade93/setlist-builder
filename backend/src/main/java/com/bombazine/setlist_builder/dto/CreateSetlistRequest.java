package com.bombazine.setlist_builder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateSetlistRequest(@NotBlank(message = "Venue name is required") String venueName, @NotNull(message = "Event date is required")
                                   LocalDate eventDate, @NotEmpty(message = "Setlists must have at least one song")List<UUID> songIds) {
}
