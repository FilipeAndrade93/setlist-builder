package com.bombazine.setlist_builder.service;

import com.bombazine.setlist_builder.entity.Song;
import com.bombazine.setlist_builder.entity.SongSource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public sealed interface GenerationStrategy permits GenerationStrategy.TargetDuration, GenerationStrategy.TopSpotifyTracks {

    record TargetDuration(int targetSeconds) implements GenerationStrategy {}
    record TopSpotifyTracks(int limit) implements GenerationStrategy {}

    static List<Song> apply(GenerationStrategy strategy, List<Song> pool) {
        return switch (strategy) {
            case TargetDuration s -> applyTargetDuration(pool, s.targetSeconds());
            case TopSpotifyTracks s -> applyTopSpotify(pool, s.limit());
        };
    }

    private static int effectivePopularity(Song song) {
        if (song.getSource() == SongSource.ARRANGEMENT && song.getOriginalSong() != null) {
            return song.getOriginalSong().getPopularity();
        }
        return song.getPopularity();
    }

    private static int compareByPopularityThenSource(Song a, Song b) {
        int cmp = Integer.compare(effectivePopularity(b), effectivePopularity(a));
        if (cmp != 0) return cmp;
        // Tiebreaker: prefer arrangement over Spotify track
        boolean aArr = a.getSource() == SongSource.ARRANGEMENT;
        boolean bArr = b.getSource() == SongSource.ARRANGEMENT;
        if (aArr && !bArr) return -1;
        if (!aArr && bArr) return 1;
        return 0;
    }

    // Arrangements share their original Spotify id, preventing both versions appearing in the same setlist
    private static UUID rootId(Song song) {
        if (song.getSource() == SongSource.ARRANGEMENT && song.getOriginalSong() != null) {
            return song.getOriginalSong().getId();
        }
        return song.getId();
    }

    private static List<Song> removeDuplicate(List<Song> songs) {
        Set<UUID> seen = new LinkedHashSet<>();
        List<Song> result = new ArrayList<>();
        for (Song song : songs) {
            if (seen.add(rootId(song))) result.add(song);
        }
        return result;
    }

    // Setlist starts and finishes with the two most popular songs then alternates between popular a less popular evenly
    private static List<Song> distributeEvenly(List<Song> songs) {
        if (songs.size() <= 2) return songs;

        List<Song> working = new ArrayList<>(songs);
        Song closer = working.removeFirst();
        Song opener = working.removeFirst();

        int midpoint = working.size() / 2;
        List<Song> popular     = new ArrayList<>(working.subList(0, midpoint));
        List<Song> lesserKnown = new ArrayList<>(working.subList(midpoint, working.size()));

        List<Song> middle = new ArrayList<>();
        int p = 0, l = 0;
        boolean takeLesser = true;
        while (p < popular.size() || l < lesserKnown.size()) {
            if (takeLesser && l < lesserKnown.size()) {
                middle.add(lesserKnown.get(l++));
            } else if (p < popular.size()) {
                middle.add(popular.get(p++));
            } else {
                middle.add(lesserKnown.get(l++));
            }
            takeLesser = !takeLesser;
        }

        List<Song> result = new ArrayList<>();
        result.add(opener);
        result.addAll(middle);
        result.add(closer);
        return result;
    }

    private static List<Song> applyTargetDuration(List<Song> pool, int targetSeconds) {
        List<Song> candidates = removeDuplicate(
                pool.stream()
                        .sorted(GenerationStrategy::compareByPopularityThenSource)
                        .toList()
        );

        // int[] instead of int: lambda requires effectively final variable
        int[] accumulated = {0};
        List<Song> selected = candidates.stream()
                .filter(song -> {
                    if (accumulated[0] + song.getDurationSeconds() <= targetSeconds) {
                        accumulated[0] += song.getDurationSeconds();
                        return true;
                    }
                    return false;
                })
                .toList();

        return distributeEvenly(new ArrayList<>(selected));
    }

    private static List<Song> applyTopSpotify(List<Song> pool, int limit) {
        List<Song> selected = removeDuplicate(
                pool.stream()
                        .sorted(GenerationStrategy::compareByPopularityThenSource)
                        .toList()
        ).stream().limit(limit).toList();

        return distributeEvenly(new ArrayList<>(selected));
    }
}
