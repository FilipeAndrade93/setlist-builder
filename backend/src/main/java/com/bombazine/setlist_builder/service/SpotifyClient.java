package com.bombazine.setlist_builder.service;

import com.bombazine.setlist_builder.config.SpotifyProperties;
import com.bombazine.setlist_builder.dto.SpotifyTrackResponse;
import com.bombazine.setlist_builder.exception.Exceptions;
import com.bombazine.setlist_builder.repository.SongRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service @Slf4j
public class SpotifyClient {

    private final WebClient spotifyWebClient;
    private final WebClient spotifyAuthClient;
    private final SpotifyProperties properties;
    private final SongRepository songRepository;

    private final AtomicReference<CachedToken> tokenCache = new AtomicReference<>();

    public SpotifyClient(WebClient spotifyWebClient, WebClient spotifyAuthClient, SpotifyProperties properties, SongRepository songRepository) {
        this.spotifyWebClient = spotifyWebClient;
        this.spotifyAuthClient = spotifyAuthClient;
        this.properties = properties;
        this.songRepository = songRepository;
    }

    @Async
    public CompletableFuture<List<SpotifyTrackResponse>> getTopTracks() {
        try {
            String token = getAccessToken();
            List<SpotifyTrack> tracks = fetchTopTracks(token, properties.artistId());

            List<SpotifyTrackResponse> response = tracks.stream()
                    .map(track -> new SpotifyTrackResponse(
                            track.id(),
                            track.name(),
                            track.durationMs() / 1000,
                            String.format("%d:%02d", (track.durationMs() / 1000) / 60, (track.durationMs() / 1000) % 60),
                            songRepository.existsBySpotifyIdAndDeletedAtIsNull(track.id()),
                            track.popularity()
                    )).toList();

            return CompletableFuture.completedFuture(response);
        } catch (WebClientException exception) {
            throw new Exceptions.SpotifyApiException(exception.getMessage(), exception);
        }
    }

    private String getAccessToken() {
        CachedToken cachedToken = tokenCache.get();

        if (cachedToken != null && cachedToken.isValid()) return cachedToken.token();

        //If token is expired, get a new one
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());

        SpotifyTokenResponse tokenResponse = spotifyAuthClient.post()
                .body(BodyInserters.fromFormData(form))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .retrieve().bodyToMono(SpotifyTokenResponse.class).block();

        if (tokenResponse == null) throw new Exceptions.SpotifyApiException("No token retrieved");

        CachedToken newToken = new CachedToken(
                tokenResponse.accessToken(), Instant.now().plusSeconds(tokenResponse.expiresIn() - 60) //Safety buffer of 60 seconds
        );
        tokenCache.set(newToken);
        return newToken.token();

    }

    private List<SpotifyTrack> fetchTopTracks(String token, String artistId) {
        SpotifyTopTracksResponse response = spotifyWebClient.get()
                .uri("/artists/{id}/top-tracks?market=PT", artistId)
                .header("Authorization", "Bearer " + token)
                .retrieve().bodyToMono(SpotifyTopTracksResponse.class).block();

        if (response == null) throw new Exceptions.SpotifyApiException("No tracks returned");

        return response.tracks();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SpotifyTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") int expiresIn
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SpotifyTopTracksResponse(
            @JsonProperty("tracks") List<SpotifyTrack> tracks
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SpotifyTrack(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("duration_ms") int durationMs,
            @JsonProperty("popularity") int popularity
    ){}

    private record CachedToken(String token, Instant expiresAt) {
        boolean isValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }
}
