package at.mediaRatingsPlatform.dao;

import at.mediaRatingsPlatform.model.Media;

import java.util.UUID;

public class MediaDao extends AbstractDao<Media> {

    private static final MediaDao instance = new MediaDao();

    private MediaDao() {}

    public static MediaDao getInstance() {
        return instance;
    }

    /**
     * Create a new Media, assign a unique ID, and store it.
     */
    @Override
    public Media create(Media m) {
        m.setId(UUID.randomUUID());
        entities.put(m.getId(), m);
        return m;
    }
}
