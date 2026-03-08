package com.bombazine.setlist_builder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GenerateSetlistRequest(@NotBlank(message = "Venue name is required") String venueName,
                                     @NotNull(message = "Event date is required") LocalDate eventDate,
                                     @Min(value = 60, message = "Target duration must be at least 60 seconds") int targetDurationSeconds
                                     ) {
}
