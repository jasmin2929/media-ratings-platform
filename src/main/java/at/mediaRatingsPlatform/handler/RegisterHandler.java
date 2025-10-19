package at.mediaRatingsPlatform.handler;

import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.service.AuthService;
import at.mediaRatingsPlatform.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class RegisterHandler extends AbstractHandler implements HttpHandler {

    private final AuthService authService;

    public RegisterHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Only POST method is allowed for registration
        if (!validateMethod(exchange, HttpMethodEnum.POST)) {
            return; // 405 already sent by validateMethod
        }

        handleSafely(exchange, () -> {
            // Parse request body
            Map<String, String> body = JsonUtil.readJson(exchange, Map.class);
            String username = body.get("username");
            String password = body.get("password");

            // Register new user
            User user = authService.register(username, password);

            // Respond with created user info
            respond(exchange, 201, Map.of(
                    "id", user.getId(),
                    "username", user.getUsername()
            ));

        });
    }
}
