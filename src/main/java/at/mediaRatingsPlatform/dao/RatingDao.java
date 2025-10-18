package at.mediaRatingsPlatform.dao;

import at.mediaRatingsPlatform.model.Rating;

import java.util.*;

public class RatingDao extends AbstractDao<Rating> {
    /**
     * Create a new Rating, assign a unique ID, and store it.
     */
    @Override
    public Rating create(Rating r) {
        r.setId(seq.getAndIncrement());
        entities.put(r.getId(), r);
        return r;
    }

    /**
     * Get all ratings associated with a given media ID.
     */
    public List<Rating> getAllByMediaId(int mediaId) {
        List<Rating> result = new ArrayList<>();
        for (Rating r : entities.values()) {
            if (r.getMediaId() == mediaId) result.add(r);
        }
        return result;
    }

}
