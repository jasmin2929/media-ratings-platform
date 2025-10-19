package at.mediaRatingsPlatform.handler;

import at.mediaRatingsPlatform.model.Rating;
import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.service.AuthService;
import at.mediaRatingsPlatform.service.RatingService;
import at.mediaRatingsPlatform.util.JsonUtil;
import at.mediaRatingsPlatform.util.QueryUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.*;
import java.util.EnumSet;
import java.util.UUID;

public class RatingHandler extends AbstractHandler implements HttpHandler {

    private final AuthService authService;
    private final RatingService ratingService;

    public RatingHandler(AuthService authService, RatingService ratingService) {
        this.authService = authService;
        this.ratingService = ratingService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        // Validate allowed HTTP methods
        if (!validateMethods(ex, EnumSet.of(HttpMethodEnum.GET, HttpMethodEnum.POST, HttpMethodEnum.PUT))) {
            return; // 405 already handled
        }

        withAuthenticatedUser(ex, authService, (exchange, user) -> {
            try {
                HttpMethodEnum method = HttpMethodEnum.fromString(exchange.getRequestMethod());
                if (method == null) {
                    safeError(exchange, 405, "Method not allowed");
                    return;
                }

                String query = exchange.getRequestURI().getQuery();
                String path = exchange.getRequestURI().getPath();

                // Handle each HTTP method
                switch (method) {
                    // ============================================================
                    // POST -> Create a new rating
                    // ============================================================
                    case POST -> {
                        Map<String, Object> body = JsonUtil.readJson(exchange, Map.class);
                        String mediaIdStr = (String) body.get("mediaId");
                        if (mediaIdStr == null) {
                            safeError(exchange, 400, "Missing mediaId");
                            return;
                        }

                        UUID mediaId;
                        try {
                            mediaId = UUID.fromString(mediaIdStr);
                        } catch (IllegalArgumentException e) {
                            safeError(exchange, 400, "Invalid mediaId format (expected UUID)");
                            return;
                        }

                        int stars = (int) body.get("stars");
                        String comment = (String) body.get("comment");

                        Rating r = ratingService.create(mediaId, stars, comment, user);
                        respond(exchange, 201, r);
                    }

                    // ============================================================
                    // GET -> Retrieve ratings by mediaId or userId
                    // ============================================================
                    case GET -> {
                        if (query == null || query.isEmpty()) {
                            safeError(exchange, 400, "Missing query parameter (mediaId or userId required)");
                            return;
                        }

                        Map<String, String> params = QueryUtil.parseQuery(query);

                        if (query.contains("mediaId=")) {
                            String mediaIdParam = QueryUtil.parseQuery(query).get("mediaId");
                            if (mediaIdParam == null) {
                                safeError(ex, 400, "Missing mediaId");
                                return;
                            }
                            UUID mediaId = UUID.fromString(mediaIdParam);
                            List<Rating> ratings = ratingService.getAllByMediaId(mediaId);
                            respond(exchange, 200, ratings);
                        }
                        else if (query.contains("userId=")) {
                            String userIdParam = QueryUtil.parseQuery(query).get("userId");
                            if (userIdParam == null) {
                                safeError(exchange, 400, "Missing userId");
                                return;
                            }
                            UUID userId = UUID.fromString(userIdParam);
                            System.out.println("Received userId query: " + userId);

                            List<Rating> ratings = ratingService.getAllByUserId(userId);
                            respond(exchange, 200, ratings);
                        }
                        else {
                            safeError(exchange, 400, "Invalid query parameter (expected mediaId or userId)");
                        }
                    }

                    // ============================================================
                    // PUT -> Update, like, or confirm rating
                    // ============================================================
                    case PUT -> {
                        // TODO: put id into body
                        // TODO: split into ratingConfirm und ratingLike handler
                        // Path could be /api/ratings/{id} or /api/ratings/{id}/like or /api/ratings/{id}/confirm
                        // Extract {id} and sob-action-segment from path
                        // e.g. for parts: ["", "api", "ratings", "{id}", "like"]
                        String[] parts = path.split("/");
                        if (parts.length < 5) {
                            Map<String, Object> body = JsonUtil.readJson(exchange, Map.class);
                            String ratingIdStr = (String) body.get("ratingId");
                            if (ratingIdStr == null) {
                                safeError(exchange, 400, "Missing ratingId");
                                return;
                            }
                            UUID ratingId = UUID.fromString(ratingIdStr);
                            int stars = (int) body.get("stars");
                            String comment = (String) body.get("comment");
                            Rating updated = ratingService.update(ratingId, user, stars, comment);
                            respond(exchange, 200, updated);
                            return;
                        }

                        // Handle sub-actions like /api/ratings/{id}/like or /confirm
                        UUID ratingId = UUID.fromString(parts[3]);
                        String action = parts.length > 4 ? parts[4] : null;

                        if (action == null) {
                            safeError(exchange, 400, "Missing action segment");
                            return;
                        }

                        switch (action) {
                            case "like" -> {
                                ratingService.like(ratingId);
                                respond(exchange, 200, Map.of("status", "liked"));
                            }
                            case "confirm" -> {
                                Rating confirmed = ratingService.confirm(ratingId, user.getId());
                                respond(exchange, 200, confirmed);
                            }
                            default -> safeError(exchange, 400, "Unknown action: " + action);
                        }
                    }

                    default -> safeError(exchange, 405, "Method not allowed");
                }

            } catch (Exception e) {
                safeError(exchange, 400, e.getMessage());
            }
        });
    }
}
