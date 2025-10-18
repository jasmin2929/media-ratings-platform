package at.mediaRatingsPlatform.handler;

import at.mediaRatingsPlatform.service.AuthService;
import at.mediaRatingsPlatform.service.FavoriteService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;

/**
 * Handles HTTP requests for adding or removing media favorites.
 *
 * This handler delegates authentication and token validation to HandlerUtil.withAuth().
 * After authentication, a BiConsumer lambda is executed containing the business logic
 * for the specific request (adding/removing favorites). This keeps the handler clean
 * and avoids repeating authentication and validation logic in every handler.
 */
public class FavoriteHandler extends AbstractHandler implements HttpHandler {

    private final AuthService authService;
    private final FavoriteService favoriteService;

    public FavoriteHandler(AuthService authService, FavoriteService favoriteService) {
        this.authService = authService;
        this.favoriteService = favoriteService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        // Check if methods are valid
        if (!validateMethods(ex, EnumSet.of(HttpMethodEnum.POST, HttpMethodEnum.DELETE))) {
            return; // 405 already handled by validateMethod
        }

        // make sure user is authorized
        withAuthenticatedUser(ex, authService, (exchange, user) -> {

            // Use HandlerUtil helper to safely get the mediaId query parameter
            // Returns null and sends a 400 Bad Request if missing/invalid
            Integer mediaId = Integer.parseInt(getQueryParam(exchange, "mediaId"));
            if (mediaId == null) return; // error already sent by helper

            try {
                HttpMethodEnum method = HttpMethodEnum.fromString(exchange.getRequestMethod());
                // Route the request based on HTTP method
                switch (method) {
                    case POST -> {
                        // Business logic: add media to user's favorites
                        favoriteService.add(user.getId(), mediaId);
                        respond(exchange, 201, Map.of("status", "added"));
                    }
                    case DELETE -> {
                        // Business logic: remove media from user's favorites
                        favoriteService.remove(user.getId(), mediaId);
                        respond(exchange, 200, Map.of("status", "removed"));
                    }
                }
            } catch (Exception e) {
                // Handle unexpected errors
                safeError(exchange, 500, "Internal server error: " + e.getMessage());
            }
        });
    }
}
