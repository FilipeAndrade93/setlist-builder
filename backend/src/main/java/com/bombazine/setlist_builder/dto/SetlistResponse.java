package com.bombazine.setlist_builder.dto;

import com.bombazine.setlist_builder.entity.Setlist;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SetlistResponse(UUID id, String venueName, LocalDate eventDate, List<SongResponse> songs, int totalDurationSeconds, String formattedDuration) {
    public static SetlistResponse from(Setlist setlist){
        return new SetlistResponse(
                setlist.getId(), setlist.getVenueName(), setlist.getEventDate(), setlist.getSongs().stream().map(SongResponse::from).toList(), setlist.getTotalDurationSeconds(), setlist.getFormattedDuration()
        );
    }
}
