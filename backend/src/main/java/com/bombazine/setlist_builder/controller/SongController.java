package com.bombazine.setlist_builder.controller;

import com.bombazine.setlist_builder.dto.*;
import com.bombazine.setlist_builder.service.LastFmSyncService;
import com.bombazine.setlist_builder.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    private final LastFmSyncService lastFmSyncService;

    @GetMapping
    public ResponseEntity<List<SongResponse>> getAllSongs() {
        return ResponseEntity.ok(songService.getAllSongs());
    }

    @PostMapping
    public ResponseEntity<SongResponse> createSong(@Valid @RequestBody CreateSongRequest request) {
        return ResponseEntity.status(201).body(songService.createSong(request));
    }

    @PutMapping("/{id}")
    public SongResponse updateSong(@PathVariable UUID id, @RequestBody @Valid UpdateSongRequest request) {
        return songService.updateSong(id, request);
    }

    @Deprecated
    @PostMapping("/spotify/{spotifyId}")
    public ResponseEntity<SongResponse> saveFromSpotify(@PathVariable String spotifyId) {
        return ResponseEntity.status(201).body(songService.saveSongFromSpotify(spotifyId));
    }

    @Deprecated
    @GetMapping("/spotify/top-tracks")
    public CompletableFuture<ResponseEntity<List<SpotifyTrackResponse>>> getTopTracks() {
        return songService.getSpotifyTopTracks().thenApply(ResponseEntity::ok);
    }

    @GetMapping("/lastfm/top-tracks")
    public ResponseEntity<List<LastFmTrackResponse>> getLastFmTopTracks() {
        return ResponseEntity.ok(songService.getLastFmTopTracks());
    }

    @PostMapping("/lastfm/{trackName}")
    public ResponseEntity<SongResponse> saveFromLastFm(@PathVariable String trackName) {
        return ResponseEntity.status(201).body(songService.saveSongFromLastFm(trackName));
    }

    @PostMapping("/lastfm/import")
    public ResponseEntity<ImportSummary> importFronLastFm() {
        ImportSummary summary = songService.importTopTracks();
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/lastfm/sync")
    public ResponseEntity<Void> triggerLastFmSync() {
        lastFmSyncService.runSync();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable UUID id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }
}
