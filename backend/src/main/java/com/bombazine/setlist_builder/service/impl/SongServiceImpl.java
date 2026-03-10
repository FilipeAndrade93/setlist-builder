package com.bombazine.setlist_builder.service.impl;

import com.bombazine.setlist_builder.dto.CreateSongRequest;
import com.bombazine.setlist_builder.dto.SongResponse;
import com.bombazine.setlist_builder.dto.SpotifyTrackResponse;
import com.bombazine.setlist_builder.entity.Song;
import com.bombazine.setlist_builder.entity.SongSource;
import com.bombazine.setlist_builder.exception.Exceptions;
import com.bombazine.setlist_builder.repository.SongRepository;
import com.bombazine.setlist_builder.service.SongService;
import com.bombazine.setlist_builder.service.SpotifyClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final SpotifyClient spotifyClient;

    @Override
    public List<SongResponse> getAllSongs() {
        return songRepository.findByDeletedAtIsNullOrderByPopularityDesc()
                .stream()
                .map(SongResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public SongResponse createSong(CreateSongRequest request) {
        Song song = new Song();
        song.setName(request.name());
        song.setDurationSeconds(request.durationSeconds());

        if (request.originalSongId() != null) {
            Song original = songRepository.findByIdAndDeletedAtIsNull(request.originalSongId())
                    .orElseThrow(() -> new Exceptions.SongNotFoundException(request.originalSongId()));
            song.setOriginalSong(original);
            song.setSource(SongSource.ARRANGEMENT);
        } else {
            song.setSource(SongSource.MANUAL);
        }

        return SongResponse.from(songRepository.save(song));
    }

    @Override
    public SongResponse saveSongFromSpotify(String spotifyId) {
        if (songRepository.existsBySpotifyIdAndDeletedAtIsNull(spotifyId)) {
            throw new Exceptions.SongAlreadySavedException(spotifyId);
        }

        List<SpotifyTrackResponse> tracks = spotifyClient.getTopTracks().join();

        SpotifyTrackResponse match = tracks.stream()
                .filter(track -> track.spotifyId().equals(spotifyId))
                .findFirst()
                .orElseThrow(() -> new Exceptions.SpotifyApiException("Spotify track not found in top tracks: " + spotifyId));

        Song song = new Song();
        song.setName(match.name());
        song.setDurationSeconds(match.durationSeconds());
        song.setSpotifyId(match.spotifyId());
        song.setSource(SongSource.SPOTIFY);
        song.setPopularity(match.popularity());
        song.setPopularitySyncedAt(LocalDateTime.now());

        return SongResponse.from(songRepository.save(song));
    }

    @Override
    @Transactional
    public void deleteSong(UUID id) {
        Song song = songRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new Exceptions.SongNotFoundException(id));

        song.setDeletedAt(LocalDateTime.now());
    }

    @Override
    public CompletableFuture<List<SpotifyTrackResponse>> getSpotifyTopTracks() {
        return spotifyClient.getTopTracks();
    }
}
