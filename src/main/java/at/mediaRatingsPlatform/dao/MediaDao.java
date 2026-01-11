/**
 * =============================================================
 * MediaDao.java
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
import at.mediaRatingsPlatform.model.MediaType;
import at.mediaRatingsPlatform.model.Rating;
import at.mediaRatingsPlatform.util.DbUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MediaDao extends AbstractDao<Media> {
    private static final MediaDao instance = new MediaDao();
    private MediaDao() {}
    public static MediaDao getInstance() { return instance; }

    @Override
    public Media create(Media m) {
        m.setId(UUID.randomUUID());
        if (m.getCreationTime() == null) m.setCreationTime(java.time.LocalDateTime.now());
        if (m.getLastUpdTime() == null) m.setLastUpdTime(java.time.LocalDateTime.now());

        String sql =
            "INSERT INTO media(id, user_id, title, description, media_type, release_year, genres, age_restriction, creation_time, last_upd_time, total_likes) "+
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, m.getId());
            ps.setObject(2, m.getUserId());
            ps.setString(3, m.getTitle());
            ps.setString(4, m.getDescription());
            ps.setString(5, m.getMediaType() == null ? null : m.getMediaType().getName());
            ps.setInt(6, m.getReleaseYear());

            String[] genres = (m.getGenreList() == null) ? null :
                    m.getGenreList().stream().map(g -> g == null ? null : g.getName()).toArray(String[]::new);
            if (genres == null) ps.setArray(7, null);
            else ps.setArray(7, c.createArrayOf("text", genres));

            ps.setInt(8, m.getAgeRestriction());
            ps.setTimestamp(9, Timestamp.valueOf(m.getCreationTime()));
            ps.setTimestamp(10, Timestamp.valueOf(m.getLastUpdTime()));
            ps.setInt(11, m.getTotalLikes());

            ps.executeUpdate();
            return m;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (create media): " + e.getMessage(), e);
        }
    }

    @Override
    public Media getById(UUID id) {
        String sql = "SELECT * FROM media WHERE id = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Media m = mapMedia(rs);

                // ratingsList laden (für API Output)
                List<Rating> ratings = RatingDao.getInstance().getAllByMediaId(m.getId());
                m.setRatingList(new ArrayList<>(ratings));

                return m;
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error (get media): " + e.getMessage(), e);
        }
    }

    @Override
    public List<Media> getAll() {
        String sql = "SELECT * FROM media";
        List<Media> out = new ArrayList<>();

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Media m = mapMedia(rs);
                // optional: ratings laden, damit list() vollständige Media liefert
                List<Rating> ratings = RatingDao.getInstance().getAllByMediaId(m.getId());
                m.setRatingList(new ArrayList<>(ratings));
                out.add(m);
            }
            return out;

        } catch (SQLException e) {
            throw new RuntimeException("DB error (list media): " + e.getMessage(), e);
        }
    }

    @Override
    public void update(UUID id, Media m) {
        if (m == null) return;
        m.touch();

        String sql =
            "UPDATE media "+
            "SET title = ?, description = ?, media_type = ?, release_year = ?, genres = ?, age_restriction = ?, last_upd_time = ?, total_likes = ? "+
            "WHERE id = ?";

        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, m.getTitle());
            ps.setString(2, m.getDescription());
            ps.setString(3, m.getMediaType() == null ? null : m.getMediaType().getName());
            ps.setInt(4, m.getReleaseYear());

            String[] genres = (m.getGenreList() == null) ? null :
                    m.getGenreList().stream().map(g -> g == null ? null : g.getName()).toArray(String[]::new);
            if (genres == null) ps.setArray(5, null);
            else ps.setArray(5, c.createArrayOf("text", genres));

            ps.setInt(6, m.getAgeRestriction());
            ps.setTimestamp(7, Timestamp.valueOf(m.getLastUpdTime()));
            ps.setInt(8, m.getTotalLikes());
            ps.setObject(9, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("DB error (update media): " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(UUID id) {
        String sql = "DELETE FROM media WHERE id = ?";
        try (Connection c = DbUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("DB error (delete media): " + e.getMessage(), e);
        }
    }

    private Media mapMedia(ResultSet rs) throws SQLException {
        Media m = new Media();
        m.setId((UUID) rs.getObject("id"));
        m.setUserId((UUID) rs.getObject("user_id"));
        m.setTitle(rs.getString("title"));
        m.setDescription(rs.getString("description"));

        String mt = rs.getString("media_type");
        if (mt != null) m.setMediaType(MediaType.builder().name(mt).build());

        m.setReleaseYear(rs.getInt("release_year"));
        m.setAgeRestriction(rs.getInt("age_restriction"));
        m.setCreationTime(rs.getTimestamp("creation_time").toLocalDateTime());
        m.setLastUpdTime(rs.getTimestamp("last_upd_time").toLocalDateTime());
        m.setTotalLikes(rs.getInt("total_likes"));

        Array genresArr = rs.getArray("genres");
        ArrayList<Genre> genres = new ArrayList<>();
        if (genresArr != null) {
            String[] names = (String[]) genresArr.getArray();
            if (names != null) {
                for (String g : names) {
                    if (g != null) genres.add(Genre.builder().name(g).build());
                }
            }
        }
        m.setGenreList(genres);

        return m;
    }
}
