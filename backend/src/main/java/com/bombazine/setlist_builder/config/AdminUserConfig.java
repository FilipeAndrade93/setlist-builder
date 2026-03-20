package com.bombazine.setlist_builder.config;

import com.bombazine.setlist_builder.entity.AppUser;
import com.bombazine.setlist_builder.entity.UserRole;
import com.bombazine.setlist_builder.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminUserConfig {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initDatabase(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if(!appUserRepository.existsByUsernameAndDeletedAtIsNull(adminUsername)) {
                AppUser admin = new AppUser();
                admin.setUsername(adminUsername);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setRole(UserRole.ADMIN);
                appUserRepository.save(admin);
                System.out.println("Admin user '"+ adminUsername + "' has been created");
            } else {
                System.out.println("Admin user '"+ adminUsername + "' already exists. Skipping creation...");
            }
        };
    }
}
