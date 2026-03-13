package com.bombazine.setlist_builder.service;

import com.bombazine.setlist_builder.dto.CreateSongRequest;
import com.bombazine.setlist_builder.dto.LastFmTrackResponse;
import com.bombazine.setlist_builder.dto.SongResponse;
import com.bombazine.setlist_builder.dto.SpotifyTrackResponse;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SongService {

    List<SongResponse> getAllSongs();

    SongResponse createSong(CreateSongRequest request);

    @Deprecated
    SongResponse saveSongFromSpotify(String spotifyId);

    SongResponse saveSongfromLastFm(String trackName);

    void deleteSong(UUID id);

    @Deprecated
    CompletableFuture<List<SpotifyTrackResponse>> getSpotifyTopTracks();

    List<LastFmTrackResponse> getLastFmTopTracks();
}
