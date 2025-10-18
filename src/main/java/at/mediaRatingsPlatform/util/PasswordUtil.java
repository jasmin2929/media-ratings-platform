package at.mediaRatingsPlatform.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Hash a plain text password
    public static String hash(String password) {
        // The "12" is the work factor (higher = slower, but more secure)
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Verify if a plain text password matches the stored hash
    public static boolean verify(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }

}
