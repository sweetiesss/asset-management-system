package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.model.Location;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.Gender;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import com.nashtech.rookies.oam.repository.LocationRepository;
import com.nashtech.rookies.oam.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ChangePasswordOptimisticLockingTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @MockitoBean
    private AssignmentServiceImpl assignmentService;


    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        locationRepository.deleteAll();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void testOptimisticLocking() {

        // Step 1: Save initial user
        User savedUser = saveUserInNewTransaction();
        UUID userId = savedUser.getId();

        // Step 2: Load two instances
        User user1 = loadUserInNewTransaction(userId);
        User user2 = loadUserInNewTransaction(userId);

        // Verify both have same version
        Assertions.assertEquals(user1.getVersion(), user2.getVersion());

        // Step 3: Modify and save first one
        user1.setHashedPassword("new-password-1");
        saveUserInNewTransaction(user1);

        // Step 4: Try to modify and save second with stale version
        user2.setHashedPassword("new-password-2");

        // Step 5: Assert that saving stale version fails
        Assertions.assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            saveUserInNewTransaction(user2);
        });
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    User saveUserInNewTransaction() {
        User user = createTestUser();
        entityManager.clear(); // Ensure insert, not merge
        return userRepository.saveAndFlush(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    User saveUserInNewTransaction(User user) {
        return userRepository.saveAndFlush(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    User loadUserInNewTransaction(UUID userId) {
        return userRepository.findById(userId).orElseThrow();
    }


    private User createTestUser() {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        Location testLocation = Location.builder()
                .code("HCM")
                .name("Ho Chi Minh")
                .build();
        testLocation = locationRepository.save(testLocation);
        return User.builder()
                .staffCode("U" + uniqueSuffix)
                .username("testuser-" + uniqueSuffix)
                .hashedPassword("old-password")
                .firstName("Test")
                .lastName("User")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .joinedOn(LocalDate.of(2020, 1, 1))
                .gender(Gender.MALE)
                .status(UserStatus.ACTIVE)
                .version(0L)
                .roles(new HashSet<>())
                .location(testLocation)
                .build();
    }
}