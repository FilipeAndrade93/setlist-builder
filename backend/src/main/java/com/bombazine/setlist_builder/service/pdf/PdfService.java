package com.bombazine.setlist_builder.service.pdf;

import com.bombazine.setlist_builder.dto.SetlistResponse;
import com.bombazine.setlist_builder.exception.Exceptions;
import com.bombazine.setlist_builder.repository.SetlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final SetlistRepository setlistRepository;
    private final PdfRenderer pdfRenderer;

    @Transactional(readOnly = true)
    public byte[] generateSetlistPdf(UUID setlistId) {
        SetlistResponse setlist = setlistRepository.findByIdWithSongs(setlistId)
                .map(SetlistResponse::from)
                .orElseThrow(() -> new Exceptions.SetlistNotFoundException(setlistId));

        return pdfRenderer.render(setlist);
    }
}
