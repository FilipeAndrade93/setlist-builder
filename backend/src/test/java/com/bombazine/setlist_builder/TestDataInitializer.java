package com.bombazine.setlist_builder;

import com.bombazine.setlist_builder.entity.AppUser;
import com.bombazine.setlist_builder.entity.UserRole;
import com.bombazine.setlist_builder.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@RequiredArgsConstructor
public class TestDataInitializer implements ApplicationRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.findByUsernameAndDeletedAtIsNull("admin").isEmpty()){
            userRepository.save(AppUser.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .build());
        }
        if(userRepository.findByUsernameAndDeletedAtIsNull("member").isEmpty()) {
            userRepository.save(AppUser.builder()
                    .username("member")
                    .passwordHash(passwordEncoder.encode("member123"))
                    .role(UserRole.MEMBER)
                    .build());
        }
    }
}
