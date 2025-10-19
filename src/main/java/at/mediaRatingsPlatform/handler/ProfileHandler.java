package at.mediaRatingsPlatform.handler;

import at.mediaRatingsPlatform.exception.BadRequestException;
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
            handleSafely(exchange, () -> {
                HttpMethodEnum method = HttpMethodEnum.fromString(exchange.getRequestMethod());
                if (method == null)
                    throw new BadRequestException("Unsupported method");

                switch (method) {
                    case GET -> respond(exchange, 200, profileService.getByUserId(user.getId()));

                    case PUT -> {
                        Map<String, Object> body = JsonUtil.readJson(exchange, Map.class);
                        String bio = (String) body.getOrDefault("bio", "");
                        String avatarUrl = (String) body.getOrDefault("avatarUrl", "");

                        Profile updated = profileService.update(user.getId(), bio, avatarUrl);
                        respond(exchange, 200, updated);
                    }

                    case DELETE -> {
                        profileService.delete(user.getId());
                        respond(exchange, 200, Map.of("status", "deleted"));
                    }
                }
            });
        });
    }
}
