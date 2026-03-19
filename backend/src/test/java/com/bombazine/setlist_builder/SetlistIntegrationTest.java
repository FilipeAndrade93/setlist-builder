package com.bombazine.setlist_builder;

import com.bombazine.setlist_builder.entity.Song;
import com.bombazine.setlist_builder.entity.SongSource;
import com.bombazine.setlist_builder.repository.SetlistRepository;
import com.bombazine.setlist_builder.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SetlistIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SetlistRepository setlistRepository;

    private Song songA;
    private Song songB;

    @BeforeEach
    void clean() {
        setlistRepository.deleteAll();
        songRepository.deleteAll();

        songA = createSong("Song A", 180, 30);
        songB = createSong("Song B", 240, 20);
    }

    private Song createSong(String name, int duration, int popularity) {
        Song song = new Song();
        song.setName(name);
        song.setDurationSeconds(duration);
        song.setSource(SongSource.MANUAL);
        song.setPopularity(popularity);
        return songRepository.save(song);
    }

    private Map createSetlist(String token, String venue, String date, List<String> songIds) {
        var response = restTemplate.exchange(
                baseUrl() + "/setlists",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("venueName", venue, "eventDate", date, "songIds", songIds), authHeaders(token)),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    @Test
    void createSetlist_withValidData_returnsSetlist() {
        String token = loginAsAdmin();

        var response = restTemplate.exchange(
                baseUrl() + "/setlists",
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of("venueName", "Test Venue", "eventDate", "2026-06-01",
                                "songIds", List.of(songA.getId().toString())),
                        authHeaders(token)
                ),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("venueName")).isEqualTo("Test Venue");
        assertThat((List<?>) response.getBody().get("songs")).hasSize(1);
    }

    @Test
    void createSetlist_withDuplicateVenueAndDate_returnsConflict() {
        String token = loginAsAdmin();
        createSetlist(token, "Venue X", "2026-07-01", List.of(songA.getId().toString()));

        var response = restTemplate.exchange(
                baseUrl() + "/setlists",
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of("venueName", "Venue X", "eventDate", "2026-07-01",
                                "songIds", List.of(songB.getId().toString())),
                        authHeaders(token)
                ),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createSetlist_withEmptySongList_returnsBadRequest() {
        String token = loginAsAdmin();

        var response = restTemplate.exchange(
                baseUrl() + "/setlists",
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of("venueName", "Empty Venue", "eventDate", "2026-08-01", "songIds", List.of()),
                        authHeaders(token)
                ),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateSetlist_reordersSongs() {
        String token = loginAsAdmin();
        Map created = createSetlist(token, "Reorder Venue", "2026-09-01",
                List.of(songA.getId().toString(), songB.getId().toString()));
        String id = (String) created.get("id");

        var response = restTemplate.exchange(
                baseUrl() + "/setlists/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(
                        Map.of("venueName", "Reorder Venue", "eventDate", "2026-09-01",
                                "songIds", List.of(songB.getId().toString(), songA.getId().toString())),
                        authHeaders(token)
                ),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> songs = (List<Map<String, Object>>) response.getBody().get("songs");
        assertThat(songs.get(0).get("name")).isEqualTo("Song B");
        assertThat(songs.get(1).get("name")).isEqualTo("Song A");
    }

    @Test
    void deleteSetlist_removesFromList() {
        String token = loginAsAdmin();
        Map created = createSetlist(token, "Delete Venue", "2026-10-01",
                List.of(songA.getId().toString()));
        String id = (String) created.get("id");

        var deleteResponse = restTemplate.exchange(
                baseUrl() + "/setlists/" + id,
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(token)),
                Void.class
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var getResponse = restTemplate.exchange(
                baseUrl() + "/setlists",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                Map[].class
        );
        assertThat(getResponse.getBody()).isEmpty();
    }

    @Test
    void generateSetlist_withSufficientSongs_returnsSetlist() {
        String token = loginAsAdmin();

        var response = restTemplate.exchange(
                baseUrl() + "/setlists/generate",
                HttpMethod.POST,
                new HttpEntity<>(
                        Map.of("venueName", "Generated Venue", "eventDate", "2026-11-01",
                                "targetDurationSeconds", 300),
                        authHeaders(token)
                ),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat((List<?>) response.getBody().get("songs")).isNotEmpty();
    }

    @Test
    void downloadPdf_returnsContentTypePdf() {
        String token = loginAsAdmin();
        Map created = createSetlist(token, "PDF Venue", "2026-12-01",
                List.of(songA.getId().toString(), songB.getId().toString()));
        String id = (String) created.get("id");

        HttpHeaders headers = authHeaders(token);
        headers.setAccept(List.of(MediaType.APPLICATION_PDF));

        var response = restTemplate.exchange(
                baseUrl() + "/setlists/" + id + "/pdf",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getBody()).isNotEmpty();
    }
}
