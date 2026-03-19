package com.bombazine.setlist_builder;

import com.bombazine.setlist_builder.entity.AppUser;
import com.bombazine.setlist_builder.entity.UserRole;
import com.bombazine.setlist_builder.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class BaseIntegrationTest {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedUsers() {
        if (userRepository.findByUsernameAndDeletedAtIsNull("admin").isEmpty()) {
            userRepository.save(AppUser.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .build());
        }
        if (userRepository.findByUsernameAndDeletedAtIsNull("member").isEmpty()) {
            userRepository.save(AppUser.builder()
                    .username("member")
                    .passwordHash(passwordEncoder.encode("member123"))
                    .role(UserRole.MEMBER)
                    .build());
        }
        SecurityContextHolder.clearContext();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TestcontainersConfig.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", TestcontainersConfig.POSTGRES::getUsername);
        registry.add("spring.datasource.password", TestcontainersConfig.POSTGRES::getPassword);
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @LocalServerPort
    protected int port;

    protected String baseUrl() {
        return "http://localhost:" + port + "/api";
    }

    protected String loginAsAdmin() {
        return login("admin", "admin123");
    }

    protected String loginAsMember() {
        return login("member", "member123");
    }

    protected String login(String username, String password) {
        var response = restTemplate.postForEntity(
                baseUrl() + "/auth/login",
                Map.of("username", username, "password", password),
                Map.class
        );

        return (String) response.getBody().get("token");
    }

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
