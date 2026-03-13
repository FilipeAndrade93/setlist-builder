package com.bombazine.setlist_builder.service;

import com.bombazine.setlist_builder.config.LastfmProperties;
import com.bombazine.setlist_builder.dto.LastFmTrackResponse;
import com.bombazine.setlist_builder.exception.Exceptions;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class LastFmClient {

    private static final int MIN_PLAYCOUNT = 50;

    private final WebClient lastFmWebClient;
    private final LastfmProperties properties;

    public LastFmClient(@Qualifier("lastFmWebClient") WebClient lastFmWebClient, LastfmProperties properties){
        this.lastFmWebClient = lastFmWebClient;
        this.properties = properties;
    }

    public long getArtistPlaycount() {
        LastFmArtistInfoResponse response = lastFmWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("method", "artist.getInfo")
                        .queryParam("artist", properties.artistName())
                        .queryParam("api_key", properties.apiKey())
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(LastFmArtistInfoResponse.class)
                .block();

        if (response == null || response.artist() == null) throw new Exceptions.LastFmApiException("No artist info returned for: " + properties.artistName());

        long playcount = Long.parseLong(response.artist().stats().playcount());

        log.debug("Artist '{}' total playcount: {}", properties.artistName(), playcount);

        return playcount;
    }

    public List<LastFmTrackResponse> getArtistTopTracks() {
        long artistPlaycount = getArtistPlaycount();

        LastFmTopTracksResponse response = lastFmWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("method", "artist.getTopTracks")
                        .queryParam("artist", properties.artistName())
                        .queryParam("autocorrect", 1)
                        .queryParam("api_key", properties.apiKey())
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(LastFmTopTracksResponse.class)
                .block();

        if (response == null || response.topTracks() == null || response.topTracks().track() == null) {
            log.warn("No top tracks returned for '{}'", properties.artistName());
            return Collections.emptyList();
        }

        return response.topTracks().track().stream()
                .filter(track -> Long.parseLong(track.playcount()) >= MIN_PLAYCOUNT)
                .map(track -> {
                    long playcount = parseLong(track.playcount());
                    int popularity = artistPlaycount > 0 ? (int) Math.min(Math.round((playcount * 100.0) / artistPlaycount), 100) : 0;
                    return new LastFmTrackResponse(track.name(), parseRank(track.attr()), playcount, parseLong(track.listeners()), popularity);
                }).toList();
    }

    public int getTrackDuration(String trackName) {
        try {
            LastFmTrackInfoResponse response = lastFmWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("method", "track.getInfo")
                            .queryParam("artist", properties.artistName())
                            .queryParam("track", trackName)
                            .queryParam("api_key", properties.apiKey())
                            .queryParam("autocorrect", "1")
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(LastFmTrackInfoResponse.class)
                    .block();
            if (response == null || response.track() == null || response.track().duration() == null) {
                log.warn("No duration found for track '{}', setting to 0", trackName);
                return 0;
            }

            int duration = Integer.parseInt(response.track().duration());
            return duration / 1000;
        } catch (WebClientResponseException | NumberFormatException exception) {
            log.warn("Failed to fetch duration for track '{}': {}", trackName, exception.getMessage());
            return 0;
        }
    }

    public TrackStats getTrackStats(String trackName) {
        try {
            LastFmTrackInfoResponse response = lastFmWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("method", "track.getInfo")
                            .queryParam("artist", properties.artistName())
                            .queryParam("track", trackName)
                            .queryParam("api_key", properties.apiKey())
                            .queryParam("autocorrect", "1")
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(LastFmTrackInfoResponse.class)
                    .block();

            if (response == null || response.track() == null) {
                log.warn("No Last.fm data found for track: '{}'", trackName);
                return TrackStats.empty();
            }

            long playcount = Long.parseLong(response.track().playcount());
            long listeners = Long.parseLong(response.track().listeners());
            log.debug("Track '{}' — playcount: {}, listeners: {}", trackName, playcount, listeners);
            return new TrackStats(playcount, listeners);

        } catch (WebClientResponseException | NumberFormatException e) {
            log.warn("Failed to fetch Last.fm stats for track '{}': {}", trackName, e.getMessage());
            return TrackStats.empty();
        }
    }

    private long parseLong(String value) {
        if (value == null || value.isBlank()) return 0L;

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private int parseRank(LastFmTrackAttribute trackAttribute) {
        if (trackAttribute == null) return 0;

        try {
            return Integer.parseInt(trackAttribute.rank());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    public record TrackStats(long playcount, long listeners) {
        public static TrackStats empty() { return new TrackStats(0L, 0L); }
        public boolean isEmpty() { return playcount == 0 && listeners == 0; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LastFmArtistInfoResponse(@JsonProperty("artist") LastFmArtist artist) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LastFmArtist(@JsonProperty("stats") LastFmArtistStats stats) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LastFmArtistStats(@JsonProperty("playcount") String playcount,
                                     @JsonProperty("listeners") String listeners) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LastFmTopTracksResponse(@JsonProperty("toptracks") LastFmTopTracks topTracks) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LastFmTopTracks(@JsonProperty("track") List<LastFmTopTrack> track) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LastFmTopTrack(
            @JsonProperty("name") String name,
            @JsonProperty("playcount") String playcount,
            @JsonProperty("listeners") String listeners,
            @JsonProperty("duration") String duration,
            @JsonProperty("@attr") LastFmTrackAttribute attr) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LastFmTrackAttribute(@JsonProperty("rank") String rank) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LastFmTrackInfoResponse(@JsonProperty("track") LastFmTrack track) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record LastFmTrack(@JsonProperty("playcount") String playcount, @JsonProperty("listeners") String listeners, @JsonProperty("duration") String duration) {}
}
