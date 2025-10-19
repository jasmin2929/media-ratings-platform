package at.mediaRatingsPlatform.util;

import at.mediaRatingsPlatform.exception.BadRequestException;
import at.mediaRatingsPlatform.exception.InternalServerException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Parse JSON to object
    // TODO: throw json exceptions of MAPPER.readValue
    public static <T> T readJson(HttpExchange ex, Class<T> clazz) {
        try {
            return MAPPER.readValue(ex.getRequestBody(), clazz);
        } catch (JsonParseException | JsonMappingException e) {
            throw new BadRequestException("Invalid JSON format: " + e.getOriginalMessage());
        } catch (IOException e) {
            throw new InternalServerException("Failed to read JSON input: " + e.getMessage());
        }
    }

    // Parse object to JSON
    public static void writeJson(HttpExchange ex, Object obj, int status) {
        try {
            byte[] data = MAPPER.writeValueAsBytes(obj);
            ex.getResponseHeaders().set("Content-Type", "application/json");
            ex.sendResponseHeaders(status, data.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(data);
            }
        } catch (IOException e) {
            throw new InternalServerException("Failed to write JSON response: " + e.getMessage());
        }
    }

    // TODO: check if needed
    // Simple error
    public static void error(HttpExchange ex, int status, String message) {
        try {
            byte[] data = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "application/json");
            ex.sendResponseHeaders(status, data.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(data);
            }
        } catch (IOException ignored) {}
    }

    // Get token from header
    public static String getToken(HttpExchange ex) {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        return auth.substring("Bearer ".length()).trim();
    }
}
