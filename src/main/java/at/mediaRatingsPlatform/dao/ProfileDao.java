/**
 * =============================================================
 * ProfileDao.java
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

import at.mediaRatingsPlatform.model.Genre;
import at.mediaRatingsPlatform.model.Media;
import at.mediaRatingsPlatform.model.Profile;
import at.mediaRatingsPlatform.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProfileDao {
    private static final ProfileDao instance = new ProfileDao();
    private ProfileDao() {}
    public static ProfileDao getInstance() { return instance; }

    public Profile create(UUID userId) {
        Profile p = new Profile();
        p.setId(UUID.randomUUID());
        p.setUserId(userId);
        p.setCreationTime(java.time.LocalDateTime.now());
        p.setLastUpdTime(java.time.LocalDateTime.now());

        String sql =
            "INSERT INTO profiles(id, user_id, creation_time, last_upd_time, total_ratings, average_score, favourite_genre, bio, avatar_url) "+
            "VALUES (?, ?, ?, ?, 0, 0, NULL, NULL, NULL)";

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, p.getId());
            ps.setObject(2, p.getUserId());
            ps.setTimestamp(3, Timestamp.valueOf(p.getCreationTime()));
            ps.setTimestamp(4, Timestamp.valueOf(p.getLastUpdTime()));
            ps.executeUpdate();
            return p;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (create profile): " + e.getMessage(), e);
        }
    }

    public Profile getById(UUID id) {
        String sql = "SELECT * FROM profiles WHERE id = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Profile p = mapProfile(rs);
                p.setFavouriteMediaList(loadFavoriteMedia(p.getUserId()));
                return p;
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (get profile by id): " + e.getMessage(), e);
        }
    }

    public Profile getByUserId(UUID userId) {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Profile p = mapProfile(rs);
                p.setFavouriteMediaList(loadFavoriteMedia(userId));
                return p;
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (get profile by userId): " + e.getMessage(), e);
        }
    }

    public void update(Profile profile) {
        if (profile == null || profile.getId() == null) return;
        profile.touch();

        String sql =
            "UPDATE profiles"+
            "SET last_upd_time = ?, total_ratings = ?, average_score = ?, favourite_genre = ?, bio = ?, avatar_url = ? "+
            "WHERE id = ?";

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(profile.getLastUpdTime()));
            ps.setInt(2, profile.getTotalRatings());
            ps.setDouble(3, profile.getAverageScore());
            ps.setString(4, profile.getFavouriteGenre() == null ? null : profile.getFavouriteGenre().getName());
            ps.setString(5, profile.getBio());
            ps.setString(6, profile.getAvatarUrl());
            ps.setObject(7, profile.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("DB error (update profile): " + e.getMessage(), e);
        }
    }

    public void delete(UUID id) {
        String sql = "DELETE FROM profiles WHERE id = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("DB error (delete profile): " + e.getMessage(), e);
        }
    }

    private Profile mapProfile(ResultSet rs) throws SQLException {
        Profile p = new Profile();
        p.setId((UUID) rs.getObject("id"));
        p.setUserId((UUID) rs.getObject("user_id"));
        p.setCreationTime(rs.getTimestamp("creation_time").toLocalDateTime());
        p.setLastUpdTime(rs.getTimestamp("last_upd_time").toLocalDateTime());
        p.setTotalRatings(rs.getInt("total_ratings"));
        p.setAverageScore(rs.getDouble("average_score"));

        String favGenre = rs.getString("favourite_genre");
        if (favGenre != null) p.setFavouriteGenre(Genre.builder().name(favGenre).build());

        p.setBio(rs.getString("bio"));
        p.setAvatarUrl(rs.getString("avatar_url"));

        // favouriteMediaList laden wir separat
        return p;
    }

    private ArrayList<Media> loadFavoriteMedia(UUID userId) {
        // Favoriten kommen aus favorites table, Media laden wir über MediaDao.
        FavoriteDao favDao = FavoriteDao.getInstance();
        MediaDao mediaDao = MediaDao.getInstance();

        ArrayList<Media> out = new ArrayList<>();
        for (UUID mediaId : favDao.getMediaIdsByUserId(userId)) {
            Media m = mediaDao.getById(mediaId);
            if (m != null) out.add(m);
        }
        return out;
    }
}
