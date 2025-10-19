package at.mediaRatingsPlatform.util;

import at.mediaRatingsPlatform.exception.BadRequestException;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to parse query strings from URIs.
 */
public class QueryUtil {

    /**
     * Parses a query string like "mediaId=42&foo=bar" into a map of key-value pairs.
     *
     * @param query the raw query string from URI
     * @return a map of query parameter names to values
     */
    public static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) return params;

        try {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2); // limit=2 ensures value with '=' is preserved
                if (kv.length == 2) {
                    params.put(kv[0], kv[1]);
                }
            }
        } catch (Exception e) {
            throw new BadRequestException("Invalid query format: " + e.getMessage());
        }
        return params;
    }
}
