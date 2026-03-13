package com.bombazine.setlist_builder.service.impl;

import com.bombazine.setlist_builder.dto.*;
import com.bombazine.setlist_builder.entity.Song;
import com.bombazine.setlist_builder.entity.SongSource;
import com.bombazine.setlist_builder.exception.Exceptions;
import com.bombazine.setlist_builder.repository.SongRepository;
import com.bombazine.setlist_builder.service.LastFmClient;
import com.bombazine.setlist_builder.service.LastFmSyncService;
import com.bombazine.setlist_builder.service.SongService;
import com.bombazine.setlist_builder.service.SpotifyClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private  final LastFmClient lastFmClient;
    private final LastFmSyncService lastFmSyncService;

    @Deprecated
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

    @Deprecated
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
    public SongResponse saveSongFromLastFm(String trackName) {
        if (songRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(trackName)) {
            throw new Exceptions.SongAlreadySavedException(trackName);
        }

        List<LastFmTrackResponse> tracks = lastFmClient.getArtistTopTracks();

        LastFmTrackResponse match = tracks.stream()
                .filter(track -> track.name().equalsIgnoreCase(trackName))
                .findFirst()
                .orElseThrow(() -> new Exceptions.LastFmApiException("Track not found in Last.fm top tracks: "+ trackName));

        int durationSeconds = lastFmClient.getTrackDuration((match.name()));

        Song song = new Song();
        song.setName(match.name());
        song.setDurationSeconds(durationSeconds);
        song.setSource(SongSource.LASTFM);
        song.setPopularity(match.popularity());
        song.setPopularitySyncedAt(LocalDateTime.now());

        return SongResponse.from(songRepository.save(song));
    }

    @Override
    @Transactional
    public SongResponse updateSong(UUID id, UpdateSongRequest request) {
        Song song = songRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new Exceptions.SongNotFoundException(id));

        song.setName(request.name());
        song.setDurationSeconds(request.durationSeconds());

        return SongResponse.from(songRepository.save(song));
    }

    @Override
    @Transactional
    public ImportSummary importTopTracks() {
        List<LastFmTrackResponse> trackResponseList = lastFmClient.getArtistTopTracks();

        AtomicInteger imported = new AtomicInteger(0);
        AtomicInteger skipped = new AtomicInteger(0);

        trackResponseList.forEach(track -> {
            if (songRepository.existsByNameIgnoreCaseAndDeletedAtIsNull(track.name())) {
                log.debug("'{}' already in library", track.name());
                skipped.incrementAndGet();
                return;
            }

            int durationSeconds = lastFmClient.getTrackDuration(track.name());

            Song song = new Song();
            song.setName(track.name());
            song.setDurationSeconds(durationSeconds);
            song.setSource(SongSource.LASTFM);
            songRepository.save(song);

            log.info("Imported '{}' from Last.fm", track.name());
            imported.incrementAndGet();
        });

        lastFmSyncService.runSync();

        return new ImportSummary(imported.get(), skipped.get(), trackResponseList.size());
    }

    @Override
    @Transactional
    public void deleteSong(UUID id) {
        Song song = songRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new Exceptions.SongNotFoundException(id));

        song.setDeletedAt(LocalDateTime.now());
    }

    @Deprecated
    @Override
    public CompletableFuture<List<SpotifyTrackResponse>> getSpotifyTopTracks() {
        return spotifyClient.getTopTracks();
    }

    @Override
    public List<LastFmTrackResponse> getLastFmTopTracks() {
        return lastFmClient.getArtistTopTracks();
    }
}
