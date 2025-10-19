package at.mediaRatingsPlatform.dao;

import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.util.TokenUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DAO for User entities.
 * Handles user creation, lookup by username, and JWT token management.
 */
public class UserDao {

    private static final UserDao instance = new UserDao();

    private UserDao() {}

    public static UserDao getInstance() {
        return instance;
    }

    private final Map<UUID, User> users = new HashMap<>();

    /**
     * Create a new User with a username and hashed password.
     */
    public User create(String username, String passwordHash) {
        User u = new User();
        u.setId(UUID.randomUUID());
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
        return TokenUtil.generateJwt(u.getId());
    }

    /**
     * Retrieve a user by their JWT token.
     * Returns null if the token is invalid.
     */
    public User getByToken(String token) {
        try {
            UUID userId = TokenUtil.parseJwt(token);
            return users.get(userId);
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
