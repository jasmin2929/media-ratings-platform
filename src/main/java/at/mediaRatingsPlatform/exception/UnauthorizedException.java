package at.mediaRatingsPlatform.exception;

// 401 Unauthorized
public class UnauthorizedException extends AppException {
    public UnauthorizedException(String message) {
        super(message, 401);
    }
}
