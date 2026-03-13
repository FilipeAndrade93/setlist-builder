package com.bombazine.setlist_builder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Deprecated
@ConfigurationProperties(prefix = "app.spotify")
public record SpotifyProperties(String clientId,
                                String clientSecret,
                                String artistName,
                                String baseUrl,
                                String authUrl,
                                String artistId) {

}
