package com.nashtech.rookies.oam.config;

import com.nashtech.rookies.oam.model.Location;
import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.Gender;
import com.nashtech.rookies.oam.model.enums.LocationCode;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import com.nashtech.rookies.oam.repository.LocationRepository;
import com.nashtech.rookies.oam.repository.RoleRepository;
import com.nashtech.rookies.oam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LocationRepository locationRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-admin.username}")
    private String defaultAdminUsername;

    @Value("${app.default-admin.password}")
    private String defaultAdminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByUsername(defaultAdminUsername).isEmpty()) {
            Role adminRole = roleRepository.findByName((RoleName.ADMIN.getName()))
                    .orElseThrow(() -> {
                        log.debug("Admin role not found in the database");
                        return new RuntimeException("Admin role not found. Please ensure the role exists in the database");
                    });

            Location location = locationRepository.findByCode(LocationCode.HCM.getName())
                    .orElseThrow(() -> {
                        log.debug("LocationCode not found in the database");
                        return new RuntimeException("LocationCode not found. Please ensure the location exists in the database");
                    });

            User admin = User.builder()
                    .username(defaultAdminUsername)
                    .firstName("System")
                    .lastName("Admin")
                    .hashedPassword(passwordEncoder.encode(defaultAdminPassword))
                    .dateOfBirth(LocalDate.of(2000, 1, 1))
                    .joinedOn(LocalDate.of(2000, 1, 2))
                    .roles(Set.of(adminRole))
                    .status(UserStatus.ACTIVE)
                    .gender(Gender.MALE)
                    .location(location)
                    .staffCode("ADMIN01")
                    .build();

            userRepository.save(admin);
            log.info("Default admin user created.");
        }
    }
}
