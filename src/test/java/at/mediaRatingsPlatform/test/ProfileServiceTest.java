package at.mediaRatingsPlatform.test;

/**
 * ProfileServiceTest.java
 *
 * This file contains JUnit 5 unit tests.
 * The tests are written with a classic Arrange / Act / Assert structure, and use Mockito
 * where mocking a dependency makes the test more focused and faster.
 *
 * Note: The production code is NOT modified here; we only add explanatory comments so that
 * readers can understand why each line exists and what behavior is being verified.
 */


// Imports: project classes, JUnit assertions/annotations, Mockito helpers, and Java stdlib types.
import at.mediaRatingsPlatform.dao.ProfileDao;
import at.mediaRatingsPlatform.dao.UserDao;
import at.mediaRatingsPlatform.exception.NotFoundException;
import at.mediaRatingsPlatform.model.Profile;
import at.mediaRatingsPlatform.service.ProfileService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


// -----------------------------------------------------------------------------
// Test class
// -----------------------------------------------------------------------------
// This class groups tests for one production component. Each @Test method focuses
// on ONE behavior and names it in a readable, sentence-like style.
public class ProfileServiceTest {


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void getByUserId_createsProfileIfMissing() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        ProfileDao profileDao = mock(ProfileDao.class);
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        UserDao userDao = mock(UserDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        ProfileService service = new ProfileService(profileDao, userDao);

        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID userId = UUID.randomUUID();

        // Stub the mock: define what the dependency should return for this test input.
        when(profileDao.getByUserId(userId)).thenReturn(null);

        Profile created = new Profile();
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        // Set an ID on the model to mimic a persisted entity.
        created.setId(UUID.randomUUID());
        // Link this entity to the currently authenticated/active user.
        created.setUserId(userId);

        // Stub the mock: define what the dependency should return for this test input.
        when(profileDao.create(userId)).thenReturn(created);

        Profile p = service.getByUserId(userId);
        // Assertion: validate the expected behavior (value, state, or exception).
        assertSame(created, p);
        // Verify interaction: ensure the service calls the dependency as expected.
        verify(profileDao).create(userId);
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void update_setsBioAndAvatar_andCallsDaoUpdate() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        ProfileDao profileDao = mock(ProfileDao.class);
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        UserDao userDao = mock(UserDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        ProfileService service = new ProfileService(profileDao, userDao);

        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID userId = UUID.randomUUID();

        Profile existing = new Profile();
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        // Set an ID on the model to mimic a persisted entity.
        existing.setId(UUID.randomUUID());
        // Link this entity to the currently authenticated/active user.
        existing.setUserId(userId);

        // Stub the mock: define what the dependency should return for this test input.
        when(profileDao.getByUserId(userId)).thenReturn(existing);

        Profile updated = service.update(userId, "bio", "http://img");
        // Assertion: validate the expected behavior (value, state, or exception).
        assertEquals("bio", updated.getBio());
        // Assertion: validate the expected behavior (value, state, or exception).
        assertEquals("http://img", updated.getAvatarUrl());
        // Verify interaction: ensure the service calls the dependency as expected.
        verify(profileDao).update(existing);
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void delete_throwsNotFound_whenProfileMissing() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        ProfileDao profileDao = mock(ProfileDao.class);
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        UserDao userDao = mock(UserDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        ProfileService service = new ProfileService(profileDao, userDao);

        // Stub the mock: define what the dependency should return for this test input.
        when(profileDao.getByUserId(any())).thenReturn(null);
        // Assertion: validate the expected behavior (value, state, or exception).
        // Assertion: the method is expected to throw an exception for this scenario.
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        assertThrows(NotFoundException.class, () -> service.delete(UUID.randomUUID()));
    }
}
