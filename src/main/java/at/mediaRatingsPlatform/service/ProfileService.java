package at.mediaRatingsPlatform.service;

import at.mediaRatingsPlatform.dao.ProfileDao;
import at.mediaRatingsPlatform.dao.UserDao;
import at.mediaRatingsPlatform.model.Profile;
import at.mediaRatingsPlatform.model.User;

public class ProfileService {
    private final ProfileDao profileDao;
    private final UserDao userDao;

    public ProfileService(ProfileDao profileDao, UserDao userDao) {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    // TODO: why not get by id?
    public Profile getByUsername(String username) {
        User user = userDao.getByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        Profile profile = profileDao.getByUserId(user.getId());
        if (profile == null) {
            // TODO: seperate create method
            // Falls beim Registrieren kein Profil erstellt wurde, hier fallback
            profile = profileDao.create(user);
        }
        return profile;
    }

    public Profile update(String username, String bio, String avatarUrl) {
        User user = userDao.getByUsername(username);
        if (user == null) throw new RuntimeException("User not found");

        Profile profile = profileDao.getByUserId(user.getId());
        if (profile == null) {
            profile = profileDao.create(user);
        }

        profile.setBio(bio);
        profile.setAvatarUrl(avatarUrl);
        profileDao.update(profile);

        return profile;
    }
}
