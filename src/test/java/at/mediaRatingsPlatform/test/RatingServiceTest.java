package at.mediaRatingsPlatform.test;

/**
 * RatingServiceTest.java
 *
 * This file contains JUnit 5 unit tests.
 * The tests are written with a classic Arrange / Act / Assert structure, and use Mockito
 * where mocking a dependency makes the test more focused and faster.
 *
 * Note: The production code is NOT modified here; we only add explanatory comments so that
 * readers can understand why each line exists and what behavior is being verified.
 */


// Imports: project classes, JUnit assertions/annotations, Mockito helpers, and Java stdlib types.
import at.mediaRatingsPlatform.dao.MediaDao;
import at.mediaRatingsPlatform.dao.RatingDao;
import at.mediaRatingsPlatform.exception.ForbiddenException;
import at.mediaRatingsPlatform.exception.NotFoundException;
import at.mediaRatingsPlatform.model.Media;
import at.mediaRatingsPlatform.model.Rating;
import at.mediaRatingsPlatform.model.RatingStatusEnum;
import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.service.RatingService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


// -----------------------------------------------------------------------------
// Test class
// -----------------------------------------------------------------------------
// This class groups tests for one production component. Each @Test method focuses
// on ONE behavior and names it in a readable, sentence-like style.
public class RatingServiceTest {


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void create_throwsNotFound_whenMediaMissing() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        RatingDao ratingDao = mock(RatingDao.class);
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        MediaDao mediaDao = mock(MediaDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        RatingService service = new RatingService(ratingDao, mediaDao);

        // Stub the mock: define what the dependency should return for this test input.
        when(mediaDao.getById(any())).thenReturn(null);

        // Create a domain model instance to use as input/output in the test.
        User u = new User();
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        // Set an ID on the model to mimic a persisted entity.
        u.setId(UUID.randomUUID());

        // Assertion: validate the expected behavior (value, state, or exception).
        // Assertion: the method is expected to throw an exception for this scenario.
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        assertThrows(NotFoundException.class, () -> service.create(UUID.randomUUID(), 5, "hi", u));
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void confirm_throwsForbidden_whenNotMediaCreator() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        RatingDao ratingDao = mock(RatingDao.class);
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        MediaDao mediaDao = mock(MediaDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        RatingService service = new RatingService(ratingDao, mediaDao);

        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID ratingId = UUID.randomUUID();
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID mediaId = UUID.randomUUID();

        Rating r = new Rating();
        r.setId(ratingId);
        r.setMediaId(mediaId);
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        // Link this entity to the currently authenticated/active user.
        r.setUserId(UUID.randomUUID());
        r.setStatus(RatingStatusEnum.PENDING);

        // Create a domain model instance to use as input/output in the test.
        Media m = new Media();
        m.setId(mediaId);
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        // Link this entity to the currently authenticated/active user.
        m.setUserId(UUID.randomUUID()); // creator is someone else

        // Stub the mock: define what the dependency should return for this test input.
        when(ratingDao.getById(ratingId)).thenReturn(r);
        // Stub the mock: define what the dependency should return for this test input.
        when(mediaDao.getById(mediaId)).thenReturn(m);

        // Assertion: validate the expected behavior (value, state, or exception).
        // Assertion: the method is expected to throw an exception for this scenario.
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        assertThrows(ForbiddenException.class, () -> service.confirm(ratingId, UUID.randomUUID()));
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void update_throwsForbidden_whenNotOwner() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        RatingDao ratingDao = mock(RatingDao.class);
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        MediaDao mediaDao = mock(MediaDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        RatingService service = new RatingService(ratingDao, mediaDao);

        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID ratingId = UUID.randomUUID();
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID ownerId = UUID.randomUUID();
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID otherId = UUID.randomUUID();

        Rating r = new Rating();
        r.setId(ratingId);
        // Link this entity to the currently authenticated/active user.
        r.setUserId(ownerId);

        // Stub the mock: define what the dependency should return for this test input.
        when(ratingDao.getById(ratingId)).thenReturn(r);

        // Create a domain model instance to use as input/output in the test.
        User other = new User();
        other.setId(otherId);

        // Assertion: validate the expected behavior (value, state, or exception).
        // Assertion: the method is expected to throw an exception for this scenario.
        assertThrows(ForbiddenException.class, () -> service.update(ratingId, other, 1, "x"));
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void delete_throwsNotFound_whenMissing() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        RatingDao ratingDao = mock(RatingDao.class);
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        MediaDao mediaDao = mock(MediaDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        RatingService service = new RatingService(ratingDao, mediaDao);

        // Stub the mock: define what the dependency should return for this test input.
        when(ratingDao.getById(any())).thenReturn(null);

        // Assertion: validate the expected behavior (value, state, or exception).
        // Assertion: the method is expected to throw an exception for this scenario.
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        assertThrows(NotFoundException.class, () -> service.delete(UUID.randomUUID(), UUID.randomUUID()));
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void getAllConfirmedByMediaId_filtersOnlyConfirmed() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        RatingDao ratingDao = mock(RatingDao.class);
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        MediaDao mediaDao = mock(MediaDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        RatingService service = new RatingService(ratingDao, mediaDao);

        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID mediaId = UUID.randomUUID();

        Rating a = new Rating(); a.setStatus(RatingStatusEnum.CONFIRMED);
        Rating b = new Rating(); b.setStatus(RatingStatusEnum.PENDING);
        Rating c = new Rating(); c.setStatus(RatingStatusEnum.CONFIRMED);

        // Stub the mock: define what the dependency should return for this test input.
        when(ratingDao.getAllByMediaId(mediaId)).thenReturn(List.of(a, b, c));

        List<Rating> out = service.getAllConfirmedByMediaId(mediaId);
        // Assertion: validate the expected behavior (value, state, or exception).
        assertEquals(2, out.size());
        // Assertion: validate the expected behavior (value, state, or exception).
        assertTrue(out.contains(a));
        // Assertion: validate the expected behavior (value, state, or exception).
        assertTrue(out.contains(c));
        // Assertion: validate the expected behavior (value, state, or exception).
        assertFalse(out.contains(b));
    }
}
