package com.bombazine.setlist_builder;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Test
    void login_withValidCredentials_returnsToken() {
        var response = restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                Map.of("username", "admin", "password", "admin123"),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("token");
        assertThat(response.getBody().get("token")).isNotNull();
    }

    @Test
    void login_withInvalidPassword_returnsUnauthorized() {
        var response = restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                Map.of("username", "admin", "password", "wrongpassword"),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_withUnknownUser_returnsUnauthorized() {
        var response = restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                Map.of("username", "nobody", "password", "password1223"),
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void register_asAdmin_createsUser() {
        String token = loginAsAdmin();

        var response = restTemplate.exchange(
                baseUrl() + "/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("username", "newuser", "password", "newpassword123"), authHeaders(token)),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void register_asMember_returnsForbidden() {
        String token = loginAsMember();

        var response = restTemplate.exchange(
                baseUrl() + "/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("username", "anotheruser", "password", "password123"), authHeaders(token)),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void register_withExistingUsername_returnsBadRequest() {
        String token = loginAsAdmin();

        var response = restTemplate.exchange(
                baseUrl() + "/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("username", "admin", "password", "admin123"), authHeaders(token)),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getUsers_asAdmin_returnsUserList() {
        String token = loginAsAdmin();

        var response = restTemplate.exchange(
                baseUrl() + "/auth/users",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                Object[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getUsers_asMember_returnsForbidden() {
        String token = loginAsMember();

        var response = restTemplate.exchange(
                baseUrl() + "/auth/users",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getUsers_withoutToken_returnsUnauthorized() {
        var response = restTemplate.getForEntity(
                baseUrl() + "/auth/users",
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
