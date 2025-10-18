package at.mediaRatingsPlatform.handler;

import at.mediaRatingsPlatform.model.Profile;
import at.mediaRatingsPlatform.service.ProfileService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class ProfileHandler extends AbstractHandler implements HttpHandler {

    private final ProfileService profileService;

    public ProfileHandler(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Validate method
        if (!validateMethod(exchange, HttpMethodEnum.GET)) {
            return; // 405 already handled by validateMethod
        }

        try {
            String username = getQueryParam(exchange, "username");
            if (username == null) return;

            Profile profile = profileService.getByUsername(username);
            if (profile == null) {
                error(exchange, 404, "Profile not found");
            } else {
                respond(exchange, 200, profile);
            }

        } catch (Exception e) {
            error(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
}
