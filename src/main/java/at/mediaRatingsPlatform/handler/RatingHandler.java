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
                        int mediaId = (int) body.get("mediaId");
                        int stars = (int) body.get("stars");
                        String comment = (String) body.get("comment");

                        Rating r = ratingService.create(mediaId, stars, comment, user);
                        respond(ex, 201, r);
                    }

                    case GET -> {
                        // two cases: get ratings of a user or get ratings of a media
                        if (query != null && query.contains("mediaId=")) {
                            String mediaIdParam = QueryUtil.parseQuery(query).get("mediaId");
                            if (mediaIdParam == null) {
                                return;
                            }
                            int mediaId = Integer.parseInt(mediaIdParam);
                            List<Rating> ratings = ratingService.getAllByMediaId(mediaId);
                            respond(exchange, 200, ratings);
                        } else if (query != null && query.contains("userId=")) {
                            String userIdParam = QueryUtil.parseQuery(query).get("userId");
                            if (userIdParam == null) {
                                return;
                            }
                            int userId = Integer.parseInt(userIdParam);
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
                            int ratingId = (int) body.get("ratingId");
                            int stars = (int) body.get("stars");
                            String comment = (String) body.get("comment");
                            Rating updated = ratingService.update(ratingId, user, stars, comment);
                            respond(exchange, 200, updated);
                        }
                        // e.g.: "", "api", "ratings", "{id}", "like"
                        else {
                            int ratingId = Integer.parseInt(parts[4]);
                            String action = parts[5];
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
