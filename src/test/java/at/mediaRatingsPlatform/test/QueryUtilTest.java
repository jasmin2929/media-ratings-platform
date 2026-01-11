package at.mediaRatingsPlatform.test;

/**
 * QueryUtilTest.java
 *
 * This file contains JUnit 5 unit tests.
 * The tests are written with a classic Arrange / Act / Assert structure, and use Mockito
 * where mocking a dependency makes the test more focused and faster.
 *
 * Note: The production code is NOT modified here; we only add explanatory comments so that
 * readers can understand why each line exists and what behavior is being verified.
 */


// Imports: project classes, JUnit assertions/annotations, Mockito helpers, and Java stdlib types.
import at.mediaRatingsPlatform.util.QueryUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


// -----------------------------------------------------------------------------
// Test class
// -----------------------------------------------------------------------------
// This class groups tests for one production component. Each @Test method focuses
// on ONE behavior and names it in a readable, sentence-like style.
public class QueryUtilTest {


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void parseQuery_nullReturnsEmptyMap() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        Map<String, String> m = QueryUtil.parseQuery(null);
        // Assertion: validate the expected behavior (value, state, or exception).
        assertNotNull(m);
        // Assertion: validate the expected behavior (value, state, or exception).
        assertTrue(m.isEmpty());
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void parseQuery_blankReturnsEmptyMap() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        Map<String, String> m = QueryUtil.parseQuery("   ");
        // Assertion: validate the expected behavior (value, state, or exception).
        assertTrue(m.isEmpty());
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void parseQuery_parsesMultiplePairs() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        Map<String, String> m = QueryUtil.parseQuery("mediaId=42&foo=bar");
        // Assertion: validate the expected behavior (value, state, or exception).
        assertEquals("42", m.get("mediaId"));
        // Assertion: validate the expected behavior (value, state, or exception).
        assertEquals("bar", m.get("foo"));
    }


    // -------------------------------------------------------------------------
    // Test case
    // -------------------------------------------------------------------------
    // Each test below follows:
    //   1) Arrange: create inputs + mocks/stubs
    //   2) Act: call the method under test
    //   3) Assert: verify outcomes / interactions
    @Test
    void parseQuery_preservesEqualsInValue() {
        // Arrange: set up the dependencies and input data for this scenario.
        // Act: invoke the method under test.
        // Assert: verify the returned value, thrown exception, and/or DAO interactions.
        Map<String, String> m = QueryUtil.parseQuery("q=a=b=c");
        // Assertion: validate the expected behavior (value, state, or exception).
        assertEquals("a=b=c", m.get("q"));
    }
}
