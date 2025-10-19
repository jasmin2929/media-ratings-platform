package at.mediaRatingsPlatform.util;

import at.mediaRatingsPlatform.exception.BadRequestException;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Hash a plain text password
    public static String hash(String password) {
        if (password == null || password.isBlank())
            throw new BadRequestException("Password cannot be empty");

        // The "12" is the work factor (higher = slower, but more secure)
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Verify if a plain text password matches the stored hash
    public static boolean verify(String password, String hashed) {
        if (password == null || hashed == null)
            throw new BadRequestException("Password or hash cannot be null");

        return BCrypt.checkpw(password, hashed);
    }

}
