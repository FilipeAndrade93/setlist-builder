package com.bombazine.setlist_builder.repository;

import com.bombazine.setlist_builder.entity.Setlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SetlistRepository extends JpaRepository<Setlist, UUID> {

    boolean existsByVenueNameAndEventDate(String venueName, LocalDate eventDate);

    @Query("SELECT DISTINCT s FROM Setlist s LEFT JOIN FETCH s.songs ORDER BY s.eventDate DESC")
    List<Setlist> findAllWithSongs();

    @Query("SELECT s FROM Setlist s LEFT JOIN FETCH s.songs WHERE s.id = :id")
    Optional<Setlist> findByIdWithSongs(UUID id);


}
