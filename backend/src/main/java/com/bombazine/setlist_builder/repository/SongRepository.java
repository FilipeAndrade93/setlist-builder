package com.bombazine.setlist_builder.repository;

import com.bombazine.setlist_builder.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SongRepository extends JpaRepository<Song, UUID> {

    List<Song> findByDeletedAtIsNull();

    List<Song> findByDeletedAtIsNullOrderByPopularityDesc();

    Optional<Song> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Song> findBySpotifyIdAndDeletedAtIsNull(String spotifyId);

    boolean existsBySpotifyIdAndDeletedAtIsNull(String spotifyId);
}
