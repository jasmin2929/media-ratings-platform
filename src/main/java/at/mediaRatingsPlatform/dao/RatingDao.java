package at.mediaRatingsPlatform.dao;

import at.mediaRatingsPlatform.model.Rating;

import java.util.*;

public class RatingDao extends AbstractDao<Rating> {

    // Singleton instance
    private static final RatingDao instance = new RatingDao();

    private RatingDao() {}

    public static RatingDao getInstance() {
        return instance;
    }

    /**
     * Create a new Rating, assign a unique ID, and store it.
     */
    @Override
    public Rating create(Rating r) {
        r.setId(UUID.randomUUID());
        entities.put(r.getId(), r);
        return r;
    }

    /**
     * Get all ratings associated with a given media ID.
     */
    public List<Rating> getAllByMediaId(UUID mediaId) {
        List<Rating> result = new ArrayList<>();
        for (Rating r : entities.values()) {
            if (r.getMediaId().equals(mediaId)) result.add(r);
        }
        return result;
    }

    /**
     * Get all ratings associated with a given user ID.
     */
    public List<Rating> getAllByUserId(UUID userId) {
        List<Rating> result = new ArrayList<>();
        for (Rating r : entities.values()) {
            System.out.println("CHECK " + r.getUserId() + " vs " + userId);
            if (r.getUserId().equals(userId)) result.add(r);
        }
        System.out.println("Found " + result.size() + " ratings for userId " + userId);
        return result;
    }

}
