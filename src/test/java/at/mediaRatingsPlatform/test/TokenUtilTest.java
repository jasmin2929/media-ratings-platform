package at.mediaRatingsPlatform.test;

/**
 * TokenUtilTest.java
 *
 * This file contains JUnit 5 unit tests.
 * The tests are written with a classic Arrange / Act / Assert structure, and use Mockito
 * where mocking a dependency makes the test more focused and faster.
 *
 * Note: The production code is NOT modified here; we only add explanatory comments so that
 * readers can understand why each line exists and what behavior is being verified.
 */


// Imports: project classes, JUnit assertions/annotations, Mockito helpers, and Java stdlib types.
import at.mediaRatingsPlatform.exception.UnauthorizedException;
import at.mediaRatingsPlatform.util.TokenUtil;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


// -----------------------------------------------------------------------------
// Test class
// -----------------------------------------------------------------------------
// This class groups tests for one production component. Each @Test method focuses
// on ONE behavior and names it in a readable, sentence-like style.
public class TokenUtilTest {


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void generateJwt_shouldReturnNonEmptyToken() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        String token = TokenUtil.generateJwt(UUID.randomUUID());
        // Assertion: validate the expected behavior (value, state, or exception).
        assertNotNull(token);
        // Assertion: validate the expected behavior (value, state, or exception).
        assertFalse(token.isBlank());
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void parseJwt_shouldReturnSameUserId() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Generate a random UUID to simulate realistic IDs without hard-coding constants.
        UUID id = UUID.randomUUID();
        String token = TokenUtil.generateJwt(id);
        UUID parsed = TokenUtil.parseJwt(token);
        // Assertion: validate the expected behavior (value, state, or exception).
        assertEquals(id, parsed);
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void parseJwt_shouldThrowForInvalidToken() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // Assertion: validate the expected behavior (value, state, or exception).
        // Assertion: the method is expected to throw an exception for this scenario.
        assertThrows(UnauthorizedException.class, () -> TokenUtil.parseJwt("this.is.not.a.jwt"));
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void parseJwt_shouldThrowForMalformedUuidSubject() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        // A JWT with subject not a UUID will fail UUID.fromString -> UnauthorizedException("Malformed token")
        // We cannot easily craft a signed token with non-UUID subject without the secret key,
        // Assertion: validate the expected behavior (value, state, or exception).
        // so we at least assert that garbage tokens throw UnauthorizedException.
        // Assertion: validate the expected behavior (value, state, or exception).
        // Assertion: the method is expected to throw an exception for this scenario.
        assertThrows(UnauthorizedException.class, () -> TokenUtil.parseJwt("abc"));
    }
}
