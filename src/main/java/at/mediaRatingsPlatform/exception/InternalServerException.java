package at.mediaRatingsPlatform.exception;

// 500 Internal Server Error
public class InternalServerException extends AppException {
    public InternalServerException(String message) {
        super(message, 500);
    }
}
