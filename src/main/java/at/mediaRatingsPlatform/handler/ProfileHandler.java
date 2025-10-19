package at.mediaRatingsPlatform.handler;

import at.mediaRatingsPlatform.model.Profile;
import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.service.AuthService;
import at.mediaRatingsPlatform.service.ProfileService;
import at.mediaRatingsPlatform.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

public class ProfileHandler extends AbstractHandler implements HttpHandler {

    private final AuthService authService;
    private final ProfileService profileService;

    public ProfileHandler(AuthService authService, ProfileService profileService) {
        this.authService = authService;
        this.profileService = profileService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        // Check if method is valid
        if (!validateMethods(ex, EnumSet.of(HttpMethodEnum.GET, HttpMethodEnum.PUT, HttpMethodEnum.DELETE))) {
            return; // 405 already handled
        }

        withAuthenticatedUser(ex, authService, (exchange, user) -> {
            try {
                HttpMethodEnum method = HttpMethodEnum.fromString(exchange.getRequestMethod());

                switch (method) {
                    case GET -> {
                        Profile profile = profileService.getByUserId(user.getId());
                        if (profile == null) {
                            safeError(exchange, 404, "Profile not found");
                        } else {
                            respond(exchange, 200, profile);
                        }
                    }

                    case PUT -> {
                        Map<String, Object> body = JsonUtil.readJson(exchange, Map.class);
                        String bio = (String) body.getOrDefault("bio", "");
                        String avatarUrl = (String) body.getOrDefault("avatarUrl", "");

                        Profile updated = profileService.update(user.getId(), bio, avatarUrl);
                        respond(exchange, 200, updated);
                    }

                    case DELETE -> {
                        boolean deleted = profileService.delete(user.getId());
                        if (deleted) {
                            respond(exchange, 200, Map.of("status", "deleted"));
                        } else {
                            safeError(exchange, 404, "Profile not found");
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
