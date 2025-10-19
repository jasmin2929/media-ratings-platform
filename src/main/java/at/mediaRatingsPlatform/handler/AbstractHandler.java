package at.mediaRatingsPlatform.handler;

import at.mediaRatingsPlatform.exception.AppException;
import at.mediaRatingsPlatform.exception.ThrowingRunnable;
import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.service.AuthService;
import at.mediaRatingsPlatform.util.JsonUtil;
import at.mediaRatingsPlatform.util.QueryUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class AbstractHandler {

    // TODO: move to utility
    protected static enum HttpMethodEnum {
        GET, POST, PUT, DELETE;
        public static HttpMethodEnum fromString(String method) {
            try {
                return HttpMethodEnum.valueOf(method.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null; // Methode nicht erlaubt
            }
        }
    }

    /**
     * Authenticate the user from the request and execute the handler logic.
     * Catches auth errors and sends proper HTTP responses.
     */
    protected void withAuthenticatedUser(HttpExchange ex, AuthService authService, BiConsumer<HttpExchange, User> logic) throws IOException {
        try {
            String token = JsonUtil.getToken(ex);
            // Authenticate token and get User
            User user = authService.getUserByToken(token);

            // Execute the business logic passed as lambda
            logic.accept(ex, user);

        } catch (Exception e) {
            // If authentication fails, send 401 Unauthorized
            JsonUtil.error(ex, 401, "Unauthorized: " + e.getMessage());
        }
    }

    // TODO: move to utility
    /**
     * Safely parse an integer query parameter from the request.
     * Sends error response automatically if missing or invalid.
     */
    protected String getQueryParam(HttpExchange ex, String paramName) {
        Map<String, String> params = QueryUtil.parseQuery(ex.getRequestURI().getQuery());
        String value = params.get(paramName);
        if (value == null) {
            JsonUtil.error(ex, 400, "Missing " + paramName);
            return null;
        }
        return value;
    }

    protected void respond(HttpExchange ex, int status, Object data) throws IOException {
        JsonUtil.writeJson(ex, data, status);
    }

    // TODO: add logging
    protected void error(HttpExchange ex, int status, String message) throws IOException {
        JsonUtil.error(ex, status, message);
    }

    /** Safe wrapper for handling unexpected errors in handlers.
     * Can be called from catch blocks to avoid repeating nested try-catch everywhere.
     */
    // TODO: add logging
    protected void safeError(HttpExchange ex, int status, String message) {
        try {
            error(ex, status, message);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send error response", e);
        }
    }
    // Helper method for request validation
    protected boolean validateMethod(HttpExchange ex, HttpMethodEnum expectedMethod) throws IOException {
        HttpMethodEnum method = HttpMethodEnum.fromString(ex.getRequestMethod());
        if (method != expectedMethod) {
            error(ex, 405, "Method " + ex.getRequestMethod() + " not allowed. Expected: " + expectedMethod);
            return false;
        }
        return true;
    }

    protected boolean validateMethods(HttpExchange ex, EnumSet<HttpMethodEnum> allowedMethods) throws IOException {
        HttpMethodEnum method = HttpMethodEnum.fromString(ex.getRequestMethod());
        if (method == null || !allowedMethods.contains(method)) {
            error(ex, 405, "Method " + ex.getRequestMethod() + " not allowed. Allowed: " + allowedMethods);
            return false;
        }
        return true;
    }

    /**
     * Safely executes a block of handler logic and provides centralized exception handling.
     *
     * This method ensures that:
     * - All domain-specific exceptions (AppException and subclasses) are handled with the proper HTTP status code.
     * - Common validation issues (IllegalArgumentException) are converted into HTTP 400 responses.
     * - IO errors (e.g., during JSON parsing or writing) are converted into HTTP 500 responses.
     * - Any other unexpected exception is caught and reported as HTTP 500.
     *
     * By using this wrapper, handlers can remain clean and declarative without repetitive try/catch blocks.
     *
     * @param ex     the HTTP exchange
     * @param logic  the business logic to execute safely
     */
    protected void handleSafely(HttpExchange ex, ThrowingRunnable logic) {
        try {
            logic.run();
        } catch (AppException e) {
            safeError(ex, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            safeError(ex, 500, "Internal server error: " + e.getMessage());
        }
    }


}