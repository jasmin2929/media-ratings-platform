package at.mediaRatingsPlatform.service;

import at.mediaRatingsPlatform.dao.FavoriteDao;
import at.mediaRatingsPlatform.dao.MediaDao;
import at.mediaRatingsPlatform.dao.ProfileDao;
import at.mediaRatingsPlatform.model.Media;
import at.mediaRatingsPlatform.model.Profile;

import java.util.UUID;

public class FavoriteService {
    private final FavoriteDao dao;
    private final MediaDao mediaDao;
    private final ProfileDao profileDao;

    public FavoriteService(FavoriteDao dao, MediaDao mediaDao, ProfileDao profileDao){
        this.dao = dao;
        this.mediaDao = mediaDao;
        this.profileDao = profileDao;
    }

    // TODO: Add favourite should be in user, not profile
    // Or maybe move to mediaService and media
    public void add(UUID userId, UUID mediaId){
        dao.add(userId, mediaId);

        Media media = mediaDao.getById(mediaId);
        Profile profile = profileDao.getByUserId(userId);

        if (media != null && profile != null) {
            profile.addFavorite(media);
        }
    }

    public void remove(UUID userId, UUID mediaId){
        dao.remove(userId, mediaId);

        Media media = mediaDao.getById(mediaId);
        Profile profile = profileDao.getByUserId(userId);

        if (media != null && profile != null) {
            profile.removeFavorite(media);
        }
    }

    //TODO: get all favorites of a user: List<Media> getFavorites(User user)
}
