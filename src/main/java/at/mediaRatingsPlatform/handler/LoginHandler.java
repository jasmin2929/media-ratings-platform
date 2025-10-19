package at.mediaRatingsPlatform.handler;

import at.mediaRatingsPlatform.service.AuthService;
import at.mediaRatingsPlatform.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class LoginHandler extends AbstractHandler implements HttpHandler {
    private final AuthService authService;

    public LoginHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        // Check if method is valid
        if (!validateMethod(ex, AbstractHandler.HttpMethodEnum.POST)) {
            return; // 405 already handled by validateMethod
        }

        handleSafely(ex, () -> {
            Map<String, String> body = JsonUtil.readJson(ex, Map.class);
            String username = body.get("username");
            String password = body.get("password");

            String token = authService.login(username, password);
            respond(ex, 200, Map.of("token", token));

        });
    }
}
