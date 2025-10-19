package at.mediaRatingsPlatform.exception;

// 404 Not Found
public class NotFoundException extends AppException {
    public NotFoundException(String message) {
        super(message, 404);
    }
}
