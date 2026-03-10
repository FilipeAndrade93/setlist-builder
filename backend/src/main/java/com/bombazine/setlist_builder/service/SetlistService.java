package com.bombazine.setlist_builder.service;

import com.bombazine.setlist_builder.dto.CreateSetlistRequest;
import com.bombazine.setlist_builder.dto.GenerateSetlistRequest;
import com.bombazine.setlist_builder.dto.SetlistResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SetlistService {

    List<SetlistResponse> getAllSetlists();

    SetlistResponse getSetlistById(UUID id);

    SetlistResponse createSetlist(CreateSetlistRequest request);

    SetlistResponse generateSetlist(GenerateSetlistRequest request);

    void deleSetlist(UUID id);
}
