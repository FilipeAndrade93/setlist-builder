package com.bombazine.setlist_builder;

import com.bombazine.setlist_builder.entity.Song;
import com.bombazine.setlist_builder.entity.SongSource;
import com.bombazine.setlist_builder.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SongIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SongRepository songRepository;

    @BeforeEach
    void cleanSongs() {
        songRepository.deleteAll();
    }

    private Song createSong(String name, int duration, int popularity) {
        Song song = new Song();
        song.setName(name);
        song.setDurationSeconds(duration);
        song.setSource(SongSource.MANUAL);
        song.setPopularity(popularity);
        return songRepository.save(song);
    }

    @Test
    void getAllSongs_returnsSongsOrderedByPopularity() {
        createSong("Song1", 180, 5);
        createSong("Song2", 200, 36);
        createSong("Song3", 240, 13);

        String token = loginAsAdmin();
        var response = restTemplate.exchange(
                baseUrl() + "/songs",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                Map[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody()[0].get("name")).isEqualTo("Song2");
        assertThat(response.getBody()[2].get("name")).isEqualTo("Song1");
    }

    @Test
    void createSong_withValidData_returnsSong() {
        String token = loginAsAdmin();

        var response = restTemplate.exchange(
                baseUrl() + "/songs",
                HttpMethod.POST,
                new HttpEntity<>(java.util.Map.of("name", "New song", "durationSeconds", 200), authHeaders(token)),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("name")).isEqualTo("New song");
        assertThat(response.getBody().get("source")).isEqualTo("manual");
    }

    @Test
    void createSong_asArrangement_inheritsPopularityFromOriginal() {
        Song original = createSong("Original song", 200, 42);

        String token = loginAsAdmin();

        var response = restTemplate.exchange(
                baseUrl() + "/songs",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("name", "Arrangement", "durationSeconds", 210, "originalSongId", original.getId().toString()), authHeaders(token)),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().get("popularity")).isEqualTo(42);
        assertThat(response.getBody().get("source")).isEqualTo("arrangement");
    }

    @Test
    void updateSong_withValidData_returnsUpdatedSong() {
        Song song = createSong("Old Name", 180, 10);
        String token = loginAsAdmin();

        var response = restTemplate.exchange(
                baseUrl() + "/songs/" + song.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("name", "New Name", "durationSeconds", 200), authHeaders(token)),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("name")).isEqualTo("New Name");
        assertThat(response.getBody().get("durationSeconds")).isEqualTo(200);
    }

    @Test
    void deleteSong_softDeletesAndExcludesFromResults() {
        Song song = createSong("To Delete", 180, 5);
        String token = loginAsAdmin();

        var deleteResponse = restTemplate.exchange(
                baseUrl() + "/songs/" + song.getId(),
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(token)),
                Void.class
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        var getResponse = restTemplate.exchange(
                baseUrl() + "/songs",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                Map[].class
        );
        assertThat(getResponse.getBody()).isEmpty();
    }

    @Test
    void deleteSong_withNonExistentId_returnsNotFound() {
        String token = loginAsAdmin();

        var response = restTemplate.exchange(
                baseUrl() + "/songs/00000000-0000-0000-0000-000000000000",
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(token)),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getSongs_withoutToken_returnsUnauthorized() {
        var response = restTemplate.getForEntity(baseUrl() + "/songs", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
