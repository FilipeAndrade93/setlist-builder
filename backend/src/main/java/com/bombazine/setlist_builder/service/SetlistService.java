package com.bombazine.setlist_builder.service;

import com.bombazine.setlist_builder.dto.CreateSetlistRequest;
import com.bombazine.setlist_builder.dto.GenerateSetlistRequest;
import com.bombazine.setlist_builder.dto.SetlistResponse;
import com.bombazine.setlist_builder.dto.UpdateSetlistRequest;

import java.util.List;
import java.util.UUID;

public interface SetlistService {

    List<SetlistResponse> getAllSetlists();

    SetlistResponse getSetlistById(UUID id);

    SetlistResponse createSetlist(CreateSetlistRequest request);

    SetlistResponse generateSetlist(GenerateSetlistRequest request);

    SetlistResponse updateSetlist(UUID id, UpdateSetlistRequest request);

    void deleteSetlist(UUID id);
}
