package at.mediaRatingsPlatform.exception;

// 400 Bad Request
public class BadRequestException extends AppException {
    public BadRequestException(String message) {
        super(message, 400);
    }
}

