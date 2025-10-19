package at.mediaRatingsPlatform.handler;

import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.service.AuthService;
import at.mediaRatingsPlatform.service.RecommendationService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.EnumSet;

public class RecommendationHandler extends AbstractHandler implements HttpHandler {

    private final RecommendationService recommendationService;
    private final AuthService authService;

    public RecommendationHandler(AuthService authService, RecommendationService recommendationService) {
        this.authService = authService;
        this.recommendationService = recommendationService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Validate method
        if (!validateMethod(exchange, HttpMethodEnum.GET)) {
            return; // 405 already handled by validateMethod
        }

        withAuthenticatedUser(exchange, authService, (ex, user) -> {
            handleSafely(ex, () -> {
                // Get personalized recommendations for the authenticated user
                var recommendations = recommendationService.getRecommendations(user);
                respond(ex, 200, recommendations);

            });
        });
    }
}
