package at.mediaRatingsPlatform.handler;

import at.mediaRatingsPlatform.model.Media;
import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.service.AuthService;
import at.mediaRatingsPlatform.service.MediaService;
import at.mediaRatingsPlatform.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MediaHandler extends AbstractHandler implements HttpHandler {
    private final AuthService authService;
    private final MediaService mediaService;

    public MediaHandler(AuthService authService, MediaService mediaService) {
        this.authService = authService;
        this.mediaService = mediaService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        // Check if method is valid
        if (!validateMethods(ex, EnumSet.of(HttpMethodEnum.POST, HttpMethodEnum.GET, HttpMethodEnum.PUT, HttpMethodEnum.DELETE))) {
            return; // 405 already handled by validateMethod
        }

        withAuthenticatedUser(ex, authService, (exchange, user) -> {
            try {
                HttpMethodEnum method = HttpMethodEnum.fromString(exchange.getRequestMethod());

                switch (method) {
                    case POST -> {
                        Media m = JsonUtil.readJson(exchange, Media.class);
                        Media created = mediaService.create(m, user);
                        respond(exchange, 201, created);
                    }
                    case GET -> {
                        String query = exchange.getRequestURI().getQuery();
                        if (query != null && query.startsWith("id=")) {
                            String idParam = query.split("=")[1];
                            UUID id;
                            try {
                                id = UUID.fromString(idParam);
                            } catch (IllegalArgumentException e) {
                                safeError(exchange, 400, "Invalid media id format (expected UUID)");
                                return;
                            }

                            Media m = mediaService.get(id);
                            if (m == null) {
                                error(exchange, 404, "Not found");
                            } else {
                                respond(exchange, 200, m);
                            }
                        } else {
                            List<Media> all = mediaService.list();
                            respond(exchange, 200, all);
                        }
                    }
                    case PUT -> {
                        Media m = JsonUtil.readJson(exchange, Media.class);
                        mediaService.update(m, user);
                        respond(exchange, 200, m);
                    }
                    case DELETE -> {
                        String query = exchange.getRequestURI().getQuery();
                        if (query != null && query.startsWith("id=")) {
                            String idParam = query.split("=")[1];
                            UUID id;
                            try {
                                id = UUID.fromString(idParam);
                            } catch (IllegalArgumentException e) {
                                safeError(exchange, 400, "Invalid media id format (expected UUID)");
                                return;
                            }
                            mediaService.delete(id, user);
                            respond(exchange, 200, Map.of("status", "deleted"));
                        } else {
                            error(exchange, 400, "Missing id");
                        }
                    }
                }

            }
            // TODO: seperate the different exceptions
            catch (Exception e) {
                safeError(exchange, 400, e.getMessage());
            }
        });
    }
}
