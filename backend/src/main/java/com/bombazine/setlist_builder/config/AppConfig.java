package com.bombazine.setlist_builder.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties({SpotifyProperties.class, LastfmProperties.class})
public class AppConfig {

    @Bean
    public WebClient spotifyWebClient(SpotifyProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient spotifyAuthClient(SpotifyProperties properties) {
        return WebClient.builder().baseUrl(properties.authUrl()).build();
    }

    @Bean
    public WebClient lastFmWebClient() {
        return WebClient.builder()
                .baseUrl("https://ws.audioscrobbler.com/2.0")
                .defaultHeader("User-Agent", "BombazineSetlistBuilder/1.0")
                .build();
    }
}
