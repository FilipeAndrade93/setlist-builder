package com.bombazine.setlist_builder.controller;

import com.bombazine.setlist_builder.dto.CreateSetlistRequest;
import com.bombazine.setlist_builder.dto.GenerateSetlistRequest;
import com.bombazine.setlist_builder.dto.SetlistResponse;
import com.bombazine.setlist_builder.service.SetlistService;
import com.bombazine.setlist_builder.service.pdf.PdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/setlists")
@RequiredArgsConstructor
public class SetlistController {

    private final SetlistService setlistService;
    private final PdfService pdfService;

    @GetMapping
    public ResponseEntity<List<SetlistResponse>> getAll() {
        return ResponseEntity.ok(setlistService.getAllSetlists());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SetlistResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(setlistService.getSetlistById(id));
    }

    @PostMapping
    public ResponseEntity<SetlistResponse> create(@Valid @RequestBody CreateSetlistRequest request) {
        return ResponseEntity.status(201).body(setlistService.createSetlist((request)));
    }

    @PostMapping("/generate")
    public ResponseEntity<SetlistResponse> generate(@Valid @RequestBody GenerateSetlistRequest request) {
        return ResponseEntity.status(201).body(setlistService.generateSetlist(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        setlistService.deleSetlist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable UUID id) {
        SetlistResponse setlist = setlistService.getSetlistById(id);
        byte[] pdf = pdfService.generateSetlistPdf(id);

        String filename = "bombazine_setlist_"+ setlist.venueName().replaceAll("[^a-zA-Z0-9]", "-").toLowerCase()
                + "_"
                + setlist.eventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + ".pdf";
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
