package com.bombazine.setlist_builder.service.impl;

import com.bombazine.setlist_builder.dto.CreateSetlistRequest;
import com.bombazine.setlist_builder.dto.GenerateSetlistRequest;
import com.bombazine.setlist_builder.dto.SetlistResponse;
import com.bombazine.setlist_builder.dto.UpdateSetlistRequest;
import com.bombazine.setlist_builder.entity.Setlist;
import com.bombazine.setlist_builder.entity.Song;
import com.bombazine.setlist_builder.exception.Exceptions;
import com.bombazine.setlist_builder.repository.SetlistRepository;
import com.bombazine.setlist_builder.repository.SongRepository;
import com.bombazine.setlist_builder.service.GenerationStrategy;
import com.bombazine.setlist_builder.service.SetlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SetlistServiceImpl implements SetlistService {

    private final SetlistRepository setlistRepository;
    private final SongRepository songRepository;

    @Override
    public List<SetlistResponse> getAllSetlists() {
        return setlistRepository.findAllWithSongs().stream().map(SetlistResponse::from).toList();
    }

    @Override
    public SetlistResponse getSetlistById(UUID id) {
        return setlistRepository.findByIdWithSongs(id)
                .map(SetlistResponse::from)
                .orElseThrow(() -> new Exceptions.SetlistNotFoundException(id));
    }


    @Override
    @Transactional
    public SetlistResponse createSetlist(CreateSetlistRequest request) {
        checkDuplicate(request.venueName(), request.eventDate());

        List<Song> songs = resolveSongs(request.songIds());

        Setlist setlist = Setlist.builder()
                .venueName(request.venueName())
                .eventDate(request.eventDate())
                .songs(songs)
                .build();
        return SetlistResponse.from(setlistRepository.save(setlist));
    }

    @Override
    @Transactional
    public SetlistResponse generateSetlist(GenerateSetlistRequest request) {
        checkDuplicate(request.venueName(), request.eventDate());

        List<Song> pool = songRepository.findByDeletedAtIsNull();

        List<Song> selected = GenerationStrategy.apply(new GenerationStrategy.TargetDuration(request.targetDurationSeconds()), pool);

        if (selected.isEmpty()) throw new Exceptions.InsufficientSongsException(request.targetDurationSeconds());

        Setlist setlist = Setlist.builder()
                .venueName(request.venueName())
                .eventDate(request.eventDate())
                .songs(selected)
                .build();

        return SetlistResponse.from(setlistRepository.save(setlist));
    }

    @Override
    @Transactional
    public SetlistResponse updateSetlist(UUID id, UpdateSetlistRequest request) {
        Setlist setlist = setlistRepository.findByIdWithSongs(id).orElseThrow(() -> new Exceptions.SetlistNotFoundException(id));

        boolean venueOrDateChanged = !setlist.getVenueName().equals(request.venueName()) || !setlist.getEventDate().equals(request.eventDate());

        if (venueOrDateChanged) {
            checkDuplicate(request.venueName(), request.eventDate());
        }

        List<Song> songs = resolveSongs(request.songIds());

        setlist.setVenueName(request.venueName());
        setlist.setEventDate(request.eventDate());
        setlist.getSongs().clear();
        setlist.getSongs().addAll(songs);

        return SetlistResponse.from(setlistRepository.save(setlist));
    }

    @Transactional
    @Override
    public void deleteSetlist(UUID id) {
        Setlist setlist = setlistRepository.findById(id).orElseThrow(() -> new Exceptions.SetlistNotFoundException(id));

        setlistRepository.delete(setlist);
    }

    private void checkDuplicate(String venue, LocalDate date) {
        if (setlistRepository.existsByVenueNameAndEventDate(venue, date)) {
            throw new Exceptions.SetlistAlreadyExistsException(venue, date.toString());
        }
    }

    private List<Song> resolveSongs(List<UUID> songIds) {
        return songIds.stream()
                .map(songId -> songRepository.findByIdAndDeletedAtIsNull(songId).orElseThrow(() -> new Exceptions.SongNotFoundException(songId)))
                .toList();
    }
}
