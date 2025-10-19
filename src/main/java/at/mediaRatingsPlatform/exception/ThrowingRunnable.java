package at.mediaRatingsPlatform.exception;

/**
 * Functional interface similar to {@link Runnable}, but allows throwing checked exceptions.
 *
 * This enables cleaner lambda expressions that can call methods
 * which declare checked exceptions (e.g. IO operations),
 * without forcing repetitive try/catch blocks at the call site.
 */
@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}
