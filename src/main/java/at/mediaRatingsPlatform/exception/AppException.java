package at.mediaRatingsPlatform.exception;

/** Base class for all domain-specific exceptions. */
public abstract class AppException extends RuntimeException {
    private final int statusCode;

    protected AppException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
