/**
 * =============================================================
 * RatingDao.java
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

import at.mediaRatingsPlatform.model.Rating;
import at.mediaRatingsPlatform.model.RatingStatusEnum;
import at.mediaRatingsPlatform.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RatingDao extends AbstractDao<Rating> {
    private static final RatingDao instance = new RatingDao();
    private RatingDao() {}
    public static RatingDao getInstance() { return instance; }

    @Override
    public Rating create(Rating r) {
        r.setId(UUID.randomUUID());
        if (r.getCreationTime() == null) r.setCreationTime(java.time.LocalDateTime.now());
        if (r.getLastUpdTime() == null) r.setLastUpdTime(java.time.LocalDateTime.now());
        if (r.getStatus() == null) r.setStatus(RatingStatusEnum.PENDING);

        String sql =
            "INSERT INTO ratings(id, media_id, user_id, stars, comment, status, creation_time, last_upd_time, total_likes) "+
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, r.getId());
            ps.setObject(2, r.getMediaId());
            ps.setObject(3, r.getUserId());
            ps.setInt(4, r.getStars());
            ps.setString(5, r.getComment());
            ps.setString(6, r.getStatus().name());
            ps.setTimestamp(7, Timestamp.valueOf(r.getCreationTime()));
            ps.setTimestamp(8, Timestamp.valueOf(r.getLastUpdTime()));
            ps.setInt(9, r.getTotalLikes());

            ps.executeUpdate();
            return r;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (create rating): " + e.getMessage(), e);
        }
    }

    @Override
    public Rating getById(UUID id) {
        String sql = "SELECT * FROM ratings WHERE id = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapRating(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (get rating): " + e.getMessage(), e);
        }
    }

    public List<Rating> getAllByMediaId(UUID mediaId) {
        String sql = "SELECT * FROM ratings WHERE media_id = ?";
        List<Rating> out = new ArrayList<>();

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, mediaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRating(rs));
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (ratings by media): " + e.getMessage(), e);
        }
    }

    public List<Rating> getAllByUserId(UUID userId) {
        String sql = "SELECT * FROM ratings WHERE user_id = ?";
        List<Rating> out = new ArrayList<>();

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRating(rs));
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (ratings by user): " + e.getMessage(), e);
        }
    }

    @Override
    public List<Rating> getAll() {
        String sql = "SELECT * FROM ratings";
        List<Rating> out = new ArrayList<>();

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) out.add(mapRating(rs));
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (list ratings): " + e.getMessage(), e);
        }
    }

    @Override
    public void update(UUID id, Rating r) {
        if (r == null) return;
        r.touch();

        String sql =
            "UPDATE ratings "+
            "SET stars = ?, comment = ?, status = ?, last_upd_time = ?, total_likes = ? "+
            "WHERE id = ?";

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, r.getStars());
            ps.setString(2, r.getComment());
            ps.setString(3, r.getStatus() == null ? RatingStatusEnum.PENDING.name() : r.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(r.getLastUpdTime()));
            ps.setInt(5, r.getTotalLikes());
            ps.setObject(6, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("DB error (update rating): " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM ratings WHERE id = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("DB error (delete rating): " + e.getMessage(), e);
        }
    }

    private Rating mapRating(ResultSet rs) throws SQLException {
        Rating r = new Rating();
        r.setId((UUID) rs.getObject("id"));
        r.setMediaId((UUID) rs.getObject("media_id"));
        r.setUserId((UUID) rs.getObject("user_id"));
        r.setStars(rs.getInt("stars"));
        
        String st = rs.getString("status");
        RatingStatusEnum status = (st == null ? RatingStatusEnum.PENDING : RatingStatusEnum.valueOf(st));
        r.setStatus(status);

        // Only show comments if CONFIRMED
        String comment = rs.getString("comment");
        r.setComment(status == RatingStatusEnum.CONFIRMED ? comment : null);


        r.setCreationTime(rs.getTimestamp("creation_time").toLocalDateTime());
        r.setLastUpdTime(rs.getTimestamp("last_upd_time").toLocalDateTime());
        r.setTotalLikes(rs.getInt("total_likes"));
        return r;
    }
}
