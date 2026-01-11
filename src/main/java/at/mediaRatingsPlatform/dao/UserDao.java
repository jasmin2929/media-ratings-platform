/**
 * =============================================================
 * UserDao.java
 * =============================================================
 *
 * PURPOSE:
 * Concrete DAO implementation for a specific entity. Extends AbstractDao to reuse common persistence logic.
 *
 * This file is part of the persistence (DAO) layer.
 * DAO = Data Access Object
 *
 * The DAO layer is responsible for:
 *  - Talking directly to the database
 *  - Executing SQL queries (via JPA / Hibernate / EntityManager)
 *  - Returning entities to the service layer
 *
 * IMPORTANT:
 *  - DAOs contain NO business logic
 *  - They only fetch, store, update, or delete data
 *  - All decision-making happens in services
 *
 * =============================================================
 */

package at.mediaRatingsPlatform.dao;

import at.mediaRatingsPlatform.model.User;
import at.mediaRatingsPlatform.util.DbUtil;
import at.mediaRatingsPlatform.util.TokenUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserDao {
    private static final UserDao instance = new UserDao();
    private UserDao() {}
    public static UserDao getInstance() { return instance; }

    public User create(String username, String passwordHash) {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername(username);
        u.setPasswordHash(passwordHash);

        String sql =
            "INSERT INTO users(id, username, password_hash, creation_time, last_upd_time) "+
            "VALUES (?, ?, ?, ?, ?)";

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, u.getId());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPasswordHash());
            ps.setTimestamp(4, Timestamp.valueOf(u.getCreationTime()));
            ps.setTimestamp(5, Timestamp.valueOf(u.getLastUpdTime()));
            ps.executeUpdate();
            return u;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (create user): " + e.getMessage(), e);
        }
    }

    public User getById(UUID id) {
        String sql = "SELECT id, username, password_hash, creation_time, last_upd_time FROM users WHERE id = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapUser(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (get user by id): " + e.getMessage(), e);
        }
    }

    public User getByUsername(String username) {
        String sql = "SELECT id, username, password_hash, creation_time, last_upd_time FROM users WHERE username = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapUser(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (get user by username): " + e.getMessage(), e);
        }
    }

    public String storeToken(User u) {
        // JWT bleibt “stateless” (kein DB-Tokenstore)
        return TokenUtil.generateJwt(u.getId());
    }

    public User getByToken(String token) {
        try {
            UUID userId = TokenUtil.parseJwt(token);
            return getById(userId);
        } catch (Exception e) {
            return null;
        }
    }

    public Collection<User> getAll() {
        String sql = "SELECT id, username, password_hash, creation_time, last_upd_time FROM users";
        List<User> out = new ArrayList<>();

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(mapUser(rs));
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (get all users): " + e.getMessage(), e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id((UUID) rs.getObject("id"))
                .username(rs.getString("username"))
                .passwordHash(rs.getString("password_hash"))
                .creationTime(rs.getTimestamp("creation_time").toLocalDateTime())
                .lastUpdTime(rs.getTimestamp("last_upd_time").toLocalDateTime())
                .build();
    }
}
