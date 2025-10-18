package at.mediaRatingsPlatform.dao;

import at.mediaRatingsPlatform.model.Profile;
import at.mediaRatingsPlatform.model.User;

import java.util.*;

// TODO: extend from AbstractDao and use unique ids for profile
public class ProfileDao {
    private final Map<UUID, Profile> profiles = new HashMap<>();

    public Profile create(User user) {
        Profile profile = new Profile();
        profile.setUser(user);
        profiles.put(user.getId(), profile);
        return profile;
    }

    public Profile getByUserId(UUID userId) {
        return profiles.get(userId);
    }
    /*
    public List<Profile> getAll() {
        return new ArrayList<>(profiles.values());
    }
    */

    // TODO: change why sometimes param id and sometimes profile or user
    public void update(Profile profile) {
        profiles.put(profile.getUser().getId(), profile);
    }

    public void delete(UUID userId) {
        profiles.remove(userId);
    }
}
