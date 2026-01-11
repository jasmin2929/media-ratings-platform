/**
 * =============================================================
 * FavoriteDao.java
 * =============================================================
 *
 * PURPOSE:
 * Concrete DAO implementation for a specific entity. Extends AbstractDao to reuse common persistence logic.
 *
 * =============================================================
 */

package at.mediaRatingsPlatform.dao;

import at.mediaRatingsPlatform.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FavoriteDao {
    private static final FavoriteDao instance = new FavoriteDao();
    private FavoriteDao() {}
    public static FavoriteDao getInstance() { return instance; }

    public void add(UUID userId, UUID mediaId) {
        String sql = 
            "INSERT INTO favorites(user_id, media_id) "+
            "VALUES (?, ?) "+
            "ON CONFLICT DO NOTHING";

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, userId);
            ps.setObject(2, mediaId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("DB error (add favorite): " + e.getMessage(), e);
        }
    }

    public void remove(UUID userId, UUID mediaId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND media_id = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, userId);
            ps.setObject(2, mediaId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("DB error (remove favorite): " + e.getMessage(), e);
        }
    }

    public List<UUID> getMediaIdsByUserId(UUID userId) {
        String sql = "SELECT media_id FROM favorites WHERE user_id = ?";
        List<UUID> out = new ArrayList<>();

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add((UUID) rs.getObject("media_id"));
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (list favorites): " + e.getMessage(), e);
        }
    }
}
