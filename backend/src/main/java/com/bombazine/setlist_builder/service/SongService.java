package com.bombazine.setlist_builder.service;

import com.bombazine.setlist_builder.dto.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SongService {

    List<SongResponse> getAllSongs();

    SongResponse createSong(CreateSongRequest request);

    @Deprecated
    SongResponse saveSongFromSpotify(String spotifyId);

    SongResponse saveSongFromLastFm(String trackName);

    SongResponse updateSong(UUID id, UpdateSongRequest request);

    ImportSummary importTopTracks();

    void deleteSong(UUID id);

    @Deprecated
    CompletableFuture<List<SpotifyTrackResponse>> getSpotifyTopTracks();

    List<LastFmTrackResponse> getLastFmTopTracks();
}
