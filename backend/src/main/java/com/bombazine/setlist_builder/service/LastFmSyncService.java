package com.bombazine.setlist_builder.service;

import com.bombazine.setlist_builder.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LastFmSyncService {

    private final LastFmClient lastFmClient;
    private final SongRepository songRepository;

    @Scheduled(cron = "0 0 3 * * MON")
    public void syncPopularity() {
        log.info("Last.fm popularity sync init");

        try {
            runSync();
            log.info("Last.fm popularity sync finished");
        } catch (Exception exception) {
            log.error("Last.fm popularity sync failed", exception);
        }
    }

    @Transactional
    public void runSync() {
        long artistPlaycount = lastFmClient.getArtistPlaycount();

        if (artistPlaycount == 0) {
            log.warn("Artist total playcount is 0. Skipping sync to avoid division by 0");
            return;
        }

        songRepository.findByDeletedAtIsNull().forEach(song -> {
            LastFmClient.TrackStats stats = lastFmClient.getTrackStats(song.getName());

            if (stats.isEmpty()) {
                log.debug("Skipping '{}' due to lack of data", song.getName());
                return;
            }

            int popularity = (int) Math.min(Math.round((stats.playcount() * 100.0) / artistPlaycount), 100);

            song.setPopularity(popularity);
            song.setPopularitySyncedAt(LocalDateTime.now());

            log.debug("'{}' playcount: {}, popularity: {}%", song.getName(), stats.playcount(), popularity);
        });
    }
}
