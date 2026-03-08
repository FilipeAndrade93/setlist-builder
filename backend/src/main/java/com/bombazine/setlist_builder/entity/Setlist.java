package com.bombazine.setlist_builder.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "setlists", uniqueConstraints = @UniqueConstraint(columnNames = {"venue_name", "event_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Setlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "venue_name", nullable = false)
    private String venueName;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @ManyToMany
    @JoinTable(name = "setlist_songs", joinColumns = @JoinColumn(name = "setlist_id"), inverseJoinColumns = @JoinColumn(name = "song_id"))
    @OrderColumn(name = "position")
    @Builder.Default
    private List<Song> songs = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    //Not stored because it would become stale if a song's duration changes
    public int getTotalDurationSeconds() {
        return songs.stream().mapToInt(Song::getDurationSeconds).sum();
    }

    public String getFormattedDuration() {
        int total = getTotalDurationSeconds();
        int hours = total / 3600;
        int minutes = (total % 3600) / 60;
        int seconds = total % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}
