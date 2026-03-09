package com.bombazine.setlist_builder.service;

import com.bombazine.setlist_builder.dto.CreateSetlistRequest;
import com.bombazine.setlist_builder.dto.GenerateSetlistRequest;
import com.bombazine.setlist_builder.dto.SetlistResponse;

import java.time.LocalDate;

public interface SetlistService {

    SetlistResponse createSetlist(CreateSetlistRequest request);

    SetlistResponse generateSetlist(GenerateSetlistRequest request);
}
