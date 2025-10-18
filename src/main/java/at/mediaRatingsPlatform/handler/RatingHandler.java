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
import java.util.List;
import java.util.Map;
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
    public void handle(HttpExchange exchange) throws IOException {

        // validate methods
        if (!validateMethods(exchange, EnumSet.of(HttpMethodEnum.GET, HttpMethodEnum.POST, HttpMethodEnum.PUT))) {
            return; // 405 already handled by validateMethod
        }

        withAuthenticatedUser(exchange, authService, (ex, user) -> {
            try {
                HttpMethodEnum method = HttpMethodEnum.fromString(ex.getRequestMethod());

                if (method == null) {
                    safeError(ex, 405, "Method not allowed");
                    return;
                }

                String query = exchange.getRequestURI().getQuery();
                String path = exchange.getRequestURI().getPath();

                // Handle each HTTP method
                switch (method) {
                    case POST -> {
                        // Parse request body for new rating
                        Map<String, Object> body = JsonUtil.readJson(ex, Map.class);
                        // Parse UUID mediaId
                        String mediaIdStr = (String) body.get("mediaId");
                        if (mediaIdStr == null) {
                            safeError(ex, 400, "Missing mediaId");
                            return;
                        }

                        UUID mediaId;
                        try {
                            mediaId = UUID.fromString(mediaIdStr);
                        } catch (IllegalArgumentException e) {
                            safeError(ex, 400, "Invalid mediaId format (expected UUID)");
                            return;
                        }

                        int stars = (int) body.get("stars");
                        String comment = (String) body.get("comment");

                        Rating r = ratingService.create(mediaId, stars, comment, user);
                        respond(ex, 201, r);
                    }

                    case GET -> {
                        // two cases: get ratings of a user or get ratings of a media
                        if (query != null && query.contains("mediaId=")) {

                            String mediaIdParam = QueryUtil.parseQuery(query).get("mediaId");
                            if (mediaIdParam == null) return;

                            UUID mediaId;
                            try {
                                mediaId = UUID.fromString(mediaIdParam);
                            } catch (IllegalArgumentException e) {
                                safeError(ex, 400, "Invalid mediaId format (expected UUID)");
                                return;
                            }

                            List<Rating> ratings = ratingService.getAllByMediaId(mediaId);
                            respond(exchange, 200, ratings);
                        } else if (query != null && query.contains("userId=")) {
                            String userIdParam = QueryUtil.parseQuery(query).get("userId");
                            if (userIdParam == null) return;

                            UUID userId;
                            try {
                                userId = UUID.fromString(userIdParam);
                            } catch (IllegalArgumentException e) {
                                safeError(ex, 400, "Invalid userId format (expected UUID)");
                                return;
                            }

                            List<Rating> ratings = ratingService.getAllByUserId(userId);
                            respond(exchange, 200, ratings);
                        } else {
                            error(exchange, 400, "Need mediaId or userId query parameter");
                        }
                    }

                    case PUT -> {
                        // TODO: put id into body
                        // TODO: split into ratingConfirm und ratingLike handler
                        // Path could be /api/ratings/{id} or /api/ratings/{id}/like or /api/ratings/{id}/confirm
                        // Extract {id} and sob-action-segment from path
                        String[] parts = path.split("/");
                        // e.g.: "", "api", "ratings", "{id}"
                        if (parts.length < 5) {
                            // no sub path -> basic update
                            Map<String, Object> body = JsonUtil.readJson(exchange, Map.class);

                            String ratingIdStr = (String) body.get("ratingId");
                            if (ratingIdStr == null) {
                                safeError(exchange, 400, "Missing ratingId");
                                return;
                            }

                            UUID ratingId;
                            try {
                                ratingId = UUID.fromString(ratingIdStr);
                            } catch (IllegalArgumentException e) {
                                safeError(exchange, 400, "Invalid ratingId format (expected UUID)");
                                return;
                            }
                            int stars = (int) body.get("stars");
                            String comment = (String) body.get("comment");
                            Rating updated = ratingService.update(ratingId, user, stars, comment);
                            respond(exchange, 200, updated);
                        }
                        // e.g.: "", "api", "ratings", "{id}", "like"
                        else {
                            // Sub-action like /api/ratings/{id}/like or /confirm
                            UUID ratingId;
                            try {
                                ratingId = UUID.fromString(parts[4]);
                            } catch (IllegalArgumentException e) {
                                safeError(exchange, 400, "Invalid ratingId format (expected UUID)");
                                return;
                            }

                            String action = parts.length > 5 ? parts[5] : null;
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
                    }

                    default -> safeError(ex, 405, "Method not allowed");
                }

            } catch (Exception e) {
                safeError(ex, 400, e.getMessage());
            }
        });
    }
}
