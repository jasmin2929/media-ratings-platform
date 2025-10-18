package at.mediaRatingsPlatform.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Parse JSON to object
    // TODO: throw json exceptions of MAPPER.readValue
    public static <T> T readJson(HttpExchange ex, Class<T> clazz) throws IOException {
        return MAPPER.readValue(ex.getRequestBody(), clazz);
    }

    // Parse object to JSON
    public static void writeJson(HttpExchange ex, Object obj, int status) throws IOException {
        byte[] data = MAPPER.writeValueAsBytes(obj);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    // TODO: check if needed
    // Simple error
    public static void error(HttpExchange ex, int status, String message) {
        try {
            byte[] data = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "application/json");
            ex.sendResponseHeaders(status, data.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(data); }
        } catch (IOException ignored) {}
    }

    // Get token from header
    public static String getToken(HttpExchange ex) {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        return auth.substring("Bearer ".length()).trim();
    }
}
