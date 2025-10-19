package at.mediaRatingsPlatform.dao;

import at.mediaRatingsPlatform.model.Profile;
import at.mediaRatingsPlatform.model.User;

import java.util.*;

// TODO: extend from AbstractDao and use unique ids for profile
public class ProfileDao {
    private static final ProfileDao instance = new ProfileDao();

    private ProfileDao() {}

    public static ProfileDao getInstance() {
        return instance;
    }

    private final Map<UUID, Profile> profiles = new HashMap<>();

    public Profile create(UUID userId) {
        Profile profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUserId(userId);
        profile.setCreationTime(java.time.LocalDateTime.now());
        profile.setLastUpdTime(java.time.LocalDateTime.now());
        profiles.put(profile.getId(), profile);
        return profile;
    }

    public Profile getById(UUID id) {
        return profiles.get(id);
    }

    public Profile getByUserId(UUID userId) {
        return profiles.values()
                .stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }
    /*
    public List<Profile> getAll() {
        return new ArrayList<>(profiles.values());
    }
    */

    public void update(Profile profile) {
        if (profile == null || profile.getId() == null) return;
        profile.touch();
        profiles.put(profile.getId(), profile);
    }

    public void delete(UUID id) {
        profiles.remove(id);
    }
}
