package at.mediaRatingsPlatform.util;

import at.mediaRatingsPlatform.exception.UnauthorizedException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

public class TokenUtil {
    // A secret key used for signing and verifying JWTs.
    // This key is generated once when the class is loaded, using the HS256 algorithm.
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * Generates a JWT (JSON Web Token) for the given user id.
     * The token includes:
     *  - The user id as the subject.
     *  - The current timestamp as the issued time.
     *  - An expiration time set to 30 minutes from now.
     *  - A digital signature using the secret key and HS256 algorithm.
     *
     * @param userId The uuid to include as the subject in the JWT.
     * @return A signed JWT string.
     */
    public static String generateJwt(UUID userId) {
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + (1000 * 60 * 30); // 30 min expiration

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(new Date(expMillis))
                .signWith(key)
                .compact();  // Build and return the final JWT string
    }

    /**
     * Validates a given JWT token and extracts the userId (subject) from it.
     * The process includes:
     *  - Parsing the JWT using the same secret key that was used to sign it.
     *  - Verifying the token's signature and expiration time.
     *  - Returning the subject (userId) if validation is successful.
     *
     * If the token is invalid or expired, this method will throw an exception.
     *
     * @param token The JWT token string to be validated and parsed.
     * @return The username (subject) stored in the JWT.
     */
    public static UUID parseJwt(String token) {
        try {
            String id = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return UUID.fromString(id);
        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid or expired token");
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Malformed token");
        }
    }
}
