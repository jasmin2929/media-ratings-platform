package at.mediaRatingsPlatform.dao;

import at.mediaRatingsPlatform.model.Media;

public class MediaDao extends AbstractDao<Media> {
    /**
     * Create a new Media, assign a unique ID, and store it.
     */
    @Override
    public Media create(Media m) {
        m.setId(seq.getAndIncrement());
        entities.put(m.getId(), m);
        return m;
    }
}
