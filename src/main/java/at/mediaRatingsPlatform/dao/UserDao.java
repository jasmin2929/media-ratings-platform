package at.mediaRatingsPlatform.dao;

import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.util.TokenUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DAO for User entities.
 * Handles user creation, lookup by username, and JWT token management.
 */
public class UserDao {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<String, String> tokens = new HashMap<>();
    private final AtomicInteger seq = new AtomicInteger(1);

    /**
     * Create a new User with a username and hashed password.
     */
    public User create(String username, String passwordHash) {
        User u = new User();
        u.setId(seq.getAndIncrement());
        u.setUsername(username);
        u.setPasswordHash(passwordHash);
        users.put(u.getId(), u);
        return u;
    }

    /**
     * Get a User by their username.
     */
    public User getByUsername(String username) {
        return users.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst().orElse(null);
    }

    /**
     * Generate and store a JWT token for a user.
     */
    public String storeToken(User u) {
        return TokenUtil.generateJwt(u.getUsername());
    }

    /**
     * Retrieve a user by their JWT token.
     * Returns null if the token is invalid.
     */
    public User getByToken(String token) {
        try {
            String username = TokenUtil.parseJwt(token);
            return getByUsername(username);
        } catch (Exception e) {
            return null; // invalid token
        }
    }

    /**
     * Get all stored users.
     */
    public Collection<User> getAll() {
        return users.values();
    }
}
