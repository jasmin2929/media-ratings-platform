package at.mediaRatingsPlatform.service;

import at.mediaRatingsPlatform.dao.ProfileDao;
import at.mediaRatingsPlatform.dao.UserDao;
import at.mediaRatingsPlatform.model.Profile;
import at.mediaRatingsPlatform.model.User;

import java.util.UUID;

public class ProfileService {
    private final ProfileDao profileDao;
    private final UserDao userDao;

    public ProfileService(ProfileDao profileDao, UserDao userDao) {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    /**
     * Returns the Profile for a given userId.
     * Creates a new one if it doesn’t exist yet.
     */
    public Profile getByUserId(UUID userId) {
        Profile profile = profileDao.getByUserId(userId);
        if (profile == null) {
            profile = profileDao.create(userId);
        }
        return profile;
    }

    /**
     * Updates the Profile for a given userId.
     */
    public Profile update(UUID userId, String bio, String avatarUrl) {
        Profile profile = profileDao.getByUserId(userId);
        if (profile == null) {
            profile = profileDao.create(userId);
        }

        profile.setBio(bio);
        profile.setAvatarUrl(avatarUrl);
        profileDao.update(profile);

        return profile;
    }

    public boolean delete(UUID userId) {
        Profile profile = profileDao.getByUserId(userId);
        if (profile == null) return false;

        profileDao.delete(profile.getId());
        return true;
    }
}
