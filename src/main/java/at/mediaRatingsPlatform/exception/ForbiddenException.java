package at.mediaRatingsPlatform.exception;

// 403 Forbidden
public class ForbiddenException extends AppException {
    public ForbiddenException(String message) {
        super(message, 403);
    }
}
