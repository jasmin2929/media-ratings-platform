package at.mediaRatingsPlatform.test;

/**
 * RecommendationServiceTest.java
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
import at.mediaRatingsPlatform.model.Genre;
import at.mediaRatingsPlatform.model.Media;
import at.mediaRatingsPlatform.model.Rating;
import at.mediaRatingsPlatform.model.RatingStatusEnum;
import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.service.RecommendationService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


// -----------------------------------------------------------------------------
// Test class
// -----------------------------------------------------------------------------
// This class groups tests for one production component. Each @Test method focuses
// on ONE behavior and names it in a readable, sentence-like style.
public class RecommendationServiceTest {


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void getRecommendations_returnsEmptyList_whenNoLikedGenres() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        MediaDao mediaDao = mock(MediaDao.class);
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        RatingDao ratingDao = mock(RatingDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        RecommendationService service = new RecommendationService(mediaDao, ratingDao);

        // Create a domain model instance to use as input/output in the test.
        User u = new User();
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        // Set an ID on the model to mimic a persisted entity.
        u.setId(UUID.randomUUID());

        // Stub the mock: define what the dependency should return for this test input.
        when(ratingDao.getAll()).thenReturn(List.of()); // no ratings
        // Assertion: validate the expected behavior (value, state, or exception).
        assertEquals(List.of(), service.getRecommendations(u));
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void getRecommendations_returnsMatchingMedia_whenLikedGenresExist() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        MediaDao mediaDao = mock(MediaDao.class);
        // Mockito: create a test double so we don't hit real infrastructure (DB, network, etc.).
        RatingDao ratingDao = mock(RatingDao.class);
        // Create the service under test, injecting mocks so we can verify behavior in isolation.
        RecommendationService service = new RecommendationService(mediaDao, ratingDao);

        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID id = UUID.randomUUID();
        // Create a domain model instance to use as input/output in the test.
        User u = new User();
        u.setId(id);

        // Important: service currently uses '==' for UUID comparison.
        // So we must use the same UUID object reference to make the test pass.
        Rating r = new Rating();
        // Link this entity to the currently authenticated/active user.
        r.setUserId(id);
        r.setStars(5);
        r.setStatus(RatingStatusEnum.CONFIRMED);
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID ratedMediaId = UUID.randomUUID();
        r.setMediaId(ratedMediaId);

        // Create a domain model instance to use as input/output in the test.
        Genre sciFi = new Genre();
        sciFi.setName("SCIFI");

        // Create a domain model instance to use as input/output in the test.
        Media ratedMedia = new Media();
        ratedMedia.setId(ratedMediaId);
        // Create a mutable list used as test data.
        ratedMedia.setGenreList(new ArrayList<>(List.of(sciFi)));

        // Stub the mock: define what the dependency should return for this test input.
        when(ratingDao.getAll()).thenReturn(List.of(r));
        // Stub the mock: define what the dependency should return for this test input.
        when(mediaDao.getById(ratedMediaId)).thenReturn(ratedMedia);

        // Create a mutable list used as test data.
        // Create a domain model instance to use as input/output in the test.
        Media candidate1 = new Media(); candidate1.setGenreList(new ArrayList<>(List.of(sciFi)));
        // Create a mutable list used as test data.
        // Create a domain model instance to use as input/output in the test.
        Media candidate2 = new Media(); candidate2.setGenreList(new ArrayList<>(List.of(sciFi)));
        // Stub the mock: define what the dependency should return for this test input.
        when(mediaDao.getAll()).thenReturn(List.of(candidate1, candidate2));

        List<Media> recs = service.getRecommendations(u);
        // Assertion: validate the expected behavior (value, state, or exception).
        assertEquals(2, recs.size());
    }
}
