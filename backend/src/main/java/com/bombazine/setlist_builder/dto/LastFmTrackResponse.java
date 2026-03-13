package com.bombazine.setlist_builder.dto;

public record LastFmTrackResponse(String name, int rank, long playcount, long listeners, int popularity) {
}
