package at.mediaRatingsPlatform.handler;

import at.mediaRatingsPlatform.exception.BadRequestException;
import at.mediaRatingsPlatform.exception.NotFoundException;
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
            handleSafely(exchange, () -> {
                HttpMethodEnum method = HttpMethodEnum.fromString(exchange.getRequestMethod());
                if (method == null)
                    throw new BadRequestException("Unsupported method");

                switch (method) {
                    case POST -> {
                        Media media = JsonUtil.readJson(exchange, Media.class);
                        Media created = mediaService.create(media, user);
                        respond(exchange, 201, created);
                    }
                    case GET -> {
                        String query = exchange.getRequestURI().getQuery();
                        if (query != null && query.startsWith("id=")) {
                            UUID id = UUID.fromString(query.split("=")[1]);
                            Media media = mediaService.get(id);
                            if (media == null)
                                throw new NotFoundException("Media not found");
                            respond(exchange, 200, media);
                        } else {
                            List<Media> all = mediaService.list();
                            respond(exchange, 200, all);
                        }
                    }
                    case PUT -> {
                        Media media = JsonUtil.readJson(exchange, Media.class);
                        mediaService.update(media, user);
                        respond(exchange, 200, media);
                    }
                    case DELETE -> {
                        String query = exchange.getRequestURI().getQuery();
                        if (query == null)
                            throw new BadRequestException("Missing id");

                        UUID id = UUID.fromString(query.split("=")[1]);
                        mediaService.delete(id, user);
                        respond(exchange, 200, Map.of("status", "deleted"));
                    }
                }
            });

        });
    }
}
