package com.bombazine.setlist_builder.service.impl;

import com.bombazine.setlist_builder.dto.CreateSetlistRequest;
import com.bombazine.setlist_builder.dto.GenerateSetlistRequest;
import com.bombazine.setlist_builder.dto.SetlistResponse;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SetlistServiceImpl implements SetlistService {

    private final SetlistRepository setlistRepository;
    private final SongRepository songRepository;

    @Override
    @Transactional
    public SetlistResponse createSetlist(CreateSetlistRequest request) {
        checkDuplicate(request.venueName(), request.eventDate());

        List<Song> songs = request.songIds().stream()
                .map(id -> songRepository.findByIdAndDeletedAtIsNull(id)
                        .orElseThrow(() -> new Exceptions.SongNotFoundException(id)))
                .toList();
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

    private void checkDuplicate(String venue, LocalDate date) {
        if (setlistRepository.existsByVenueNameAndEventDate(venue, date)) {
            throw new Exceptions.SetlistAlreadyExistsException(venue, date.toString());
        }
    }
}
