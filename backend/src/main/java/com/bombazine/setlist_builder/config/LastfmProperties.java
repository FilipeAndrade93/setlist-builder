package com.bombazine.setlist_builder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.lastfm")
public record LastfmProperties(String apiKey, String artistName) {
}
