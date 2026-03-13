package com.bombazine.setlist_builder.service;

import com.bombazine.setlist_builder.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Deprecated
@Service
@RequiredArgsConstructor
@Slf4j
public class SpotifySyncService {

    private final SongRepository songRepository;
    private final SpotifyClient spotifyClient;

    //@Scheduled(fixedRate = 86400000) //24 hours
    @Transactional
    public void syncPopularity() {
        log.info("Spotify popularity syn init");
        var tracks = spotifyClient.getTopTracks().join();
        tracks.forEach(track -> songRepository.findBySpotifyIdAndDeletedAtIsNull(track.spotifyId())
                .ifPresent(song -> {
                    song.setPopularity(track.popularity());
                    song.setPopularitySyncedAt(LocalDateTime.now());
                    songRepository.save(song);
                    log.debug("Updated popularity for: {}", song.getName());
                }));

        log.info("Spotify popularity sync finished");
    }
}
