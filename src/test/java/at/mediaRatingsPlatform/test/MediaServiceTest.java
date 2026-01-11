package at.mediaRatingsPlatform.test;

/**
 * MediaServiceTest.java
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
import at.mediaRatingsPlatform.exception.ForbiddenException;
import at.mediaRatingsPlatform.exception.NotFoundException;
import at.mediaRatingsPlatform.model.Genre;
import at.mediaRatingsPlatform.model.Media;
import at.mediaRatingsPlatform.model.MediaType;
import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.service.MediaService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


// -----------------------------------------------------------------------------
// Test class
// -----------------------------------------------------------------------------
// This class groups tests for one production component. Each @Test method focuses
// on ONE behavior and names it in a readable, sentence-like style.
public class MediaServiceTest {


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void create_setsUserId_andDelegatesToDao() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        MediaDao dao = mock(MediaDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        MediaService service = new MediaService(dao);

        // Create a domain model instance to use as input/output in the test.
        User u = new User();
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        // Set an ID on the model to mimic a persisted entity.
        u.setId(UUID.randomUUID());

        // Create a domain model instance to use as input/output in the test.
        Media m = new Media();
        // Stub the mock: define what the dependency should return for this test input.
        when(dao.create(any(Media.class))).thenAnswer(inv -> inv.getArgument(0));

        // Create a domain model instance to use as input/output in the test.
        Media created = service.create(m, u);

        // ArgumentCaptor lets us capture parameters passed to the mock for deeper assertions.
        ArgumentCaptor<Media> cap = ArgumentCaptor.forClass(Media.class);
        // Verify interaction: ensure the service calls the dependency as expected.
        verify(dao).create(cap.capture());
        // Assertion: validate the expected behavior (value, state, or exception).
        assertEquals(u.getId(), cap.getValue().getUserId());
        // Assertion: validate the expected behavior (value, state, or exception).
        assertEquals(u.getId(), created.getUserId());
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void get_throwsNotFound_whenDaoReturnsNull() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        MediaDao dao = mock(MediaDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        MediaService service = new MediaService(dao);
        // Stub the mock: define what the dependency should return for this test input.
        when(dao.getById(any())).thenReturn(null);

        // Assertion: validate the expected behavior (value, state, or exception).
        // Assertion: the method is expected to throw an exception for this scenario.
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        assertThrows(NotFoundException.class, () -> service.get(UUID.randomUUID()));
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void update_throwsForbidden_whenNotCreator() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        MediaDao dao = mock(MediaDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        MediaService service = new MediaService(dao);

        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID creatorId = UUID.randomUUID();
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID otherId = UUID.randomUUID();

        // Create a domain model instance to use as input/output in the test.
        Media existing = new Media();
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        // Set an ID on the model to mimic a persisted entity.
        existing.setId(UUID.randomUUID());
        // Link this entity to the currently authenticated/active user.
        existing.setUserId(creatorId);

        // Create a domain model instance to use as input/output in the test.
        Media update = new Media();
        update.setId(existing.getId());

        // Stub the mock: define what the dependency should return for this test input.
        when(dao.getById(existing.getId())).thenReturn(existing);

        // Create a domain model instance to use as input/output in the test.
        User other = new User();
        other.setId(otherId);

        // Assertion: validate the expected behavior (value, state, or exception).
        // Assertion: the method is expected to throw an exception for this scenario.
        assertThrows(ForbiddenException.class, () -> service.update(update, other));
        // Verify interaction: ensure the service calls the dependency as expected.
        verify(dao, never()).update(any(), any());
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void delete_throwsForbidden_whenNotCreator() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        MediaDao dao = mock(MediaDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        MediaService service = new MediaService(dao);

        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID creatorId = UUID.randomUUID();
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID otherId = UUID.randomUUID();
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID mediaId = UUID.randomUUID();

        // Create a domain model instance to use as input/output in the test.
        Media existing = new Media();
        existing.setId(mediaId);
        // Link this entity to the currently authenticated/active user.
        existing.setUserId(creatorId);

        // Stub the mock: define what the dependency should return for this test input.
        when(dao.getById(mediaId)).thenReturn(existing);

        // Create a domain model instance to use as input/output in the test.
        User other = new User();
        other.setId(otherId);

        // Assertion: validate the expected behavior (value, state, or exception).
        // Assertion: the method is expected to throw an exception for this scenario.
        assertThrows(ForbiddenException.class, () -> service.delete(mediaId, other));
        // Verify interaction: ensure the service calls the dependency as expected.
        verify(dao, never()).delete(any());
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void filterByGenre_filtersCaseInsensitive() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        MediaDao dao = mock(MediaDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        MediaService service = new MediaService(dao);

        // Create a domain model instance to use as input/output in the test.
        Genre sciFi = new Genre();
        sciFi.setName("SCIFI");
        // Create a domain model instance to use as input/output in the test.
        Genre comedy = new Genre();
        comedy.setName("COMEDY");

        // Create a domain model instance to use as input/output in the test.
        Media m1 = new Media();
        // Create a mutable list used as test data.
        m1.setGenreList(new ArrayList<>(List.of(sciFi)));
        // Create a domain model instance to use as input/output in the test.
        Media m2 = new Media();
        // Create a mutable list used as test data.
        m2.setGenreList(new ArrayList<>(List.of(comedy)));

        // Stub the mock: define what the dependency should return for this test input.
        when(dao.getAll()).thenReturn(List.of(m1, m2));

        // Create a domain model instance to use as input/output in the test.
        Genre query = new Genre();
        query.setName("scifi");

        List<Media> out = service.filterByGenre(query);
        // Assertion: validate the expected behavior (value, state, or exception).
        assertEquals(1, out.size());
        // Assertion: validate the expected behavior (value, state, or exception).
        assertSame(m1, out.get(0));
    }
}
