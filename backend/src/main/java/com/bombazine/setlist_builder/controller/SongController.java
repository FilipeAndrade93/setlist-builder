package com.bombazine.setlist_builder.controller;

import com.bombazine.setlist_builder.dto.CreateSongRequest;
import com.bombazine.setlist_builder.dto.SongResponse;
import com.bombazine.setlist_builder.dto.SpotifyTrackResponse;
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

    @GetMapping
    public ResponseEntity<List<SongResponse>> getAllSongs() {
        return ResponseEntity.ok(songService.getAllSongs());
    }

    @PostMapping
    public ResponseEntity<SongResponse> createSong(@Valid @RequestBody CreateSongRequest request) {
        return ResponseEntity.status(201).body(songService.createSong(request));
    }

    @PostMapping("/spotify/{spotifyId}")
    public ResponseEntity<SongResponse> saveFromSpotify(@PathVariable String spotifyId) {
        return ResponseEntity.status(201).body(songService.saveSongFromSpotify(spotifyId));
    }

    @GetMapping("/spotify/top-tracks")
    public CompletableFuture<ResponseEntity<List<SpotifyTrackResponse>>> getTopTracks() {
        return songService.getSpotifyTopTracks().thenApply(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable UUID id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }
}
