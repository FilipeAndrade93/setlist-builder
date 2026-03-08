package com.bombazine.setlist_builder.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "songs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "source", nullable = false)
    private SongSource source = SongSource.MANUAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "original_song_id")
    private Song originalSong;

    //Can be NULL for manually added songs
    @Column(name = "spotify_id", unique = true)
    private String spotifyId;

    @Column(name = "popularity")
    private int popularity;

    @Column(name = "popularity_synced_at")
    private LocalDateTime popularitySyncedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isFromSpotify() {
        return spotifyId != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public String getFormattedDuration() {
        return String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60);
    }
}
