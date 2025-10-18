package at.mediaRatingsPlatform.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class TokenUtil {
    // A secret key used for signing and verifying JWTs.
    // This key is generated once when the class is loaded, using the HS256 algorithm.
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * A placeholder function for password hashing.
     * Currently, it only prefixes the input with "HASHED-" instead of performing real hashing.
     * In production, this should be replaced with a secure hashing algorithm (e.g., BCrypt, Argon2).
     *
     * @param password The plain text password that should be hashed.
     * @return A placeholder "hashed" password string.
     */
    //TODO: check if needed
    public static String hashPassword(String password) {
        return "HASHED-" + password;
    }

    /**
     * Generates a JWT (JSON Web Token) for the given username.
     * The token includes:
     *  - The username as the subject.
     *  - The current timestamp as the issued time.
     *  - An expiration time set to 30 minutes from now.
     *  - A digital signature using the secret key and HS256 algorithm.
     *
     * @param username The username to include as the subject in the JWT.
     * @return A signed JWT string.
     */
    public static String generateJwt(String username) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + (1000 * 60 * 30); // 30 min expiration

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(new Date(expMillis))
                .signWith(key)
                .compact();  // Build and return the final JWT string
    }

    /**
     * Validates a given JWT token and extracts the username (subject) from it.
     * The process includes:
     *  - Parsing the JWT using the same secret key that was used to sign it.
     *  - Verifying the token's signature and expiration time.
     *  - Returning the subject (username) if validation is successful.
     *
     * If the token is invalid or expired, this method will throw an exception.
     *
     * @param token The JWT token string to be validated and parsed.
     * @return The username (subject) stored in the JWT.
     */
    public static String parseJwt(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token) // Parse and validate the JWT
                .getBody()
                .getSubject(); // Extract and return the subject (username)
    }
}
