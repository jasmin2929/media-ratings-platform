package at.mediaRatingsPlatform.dao;

import java.util.*;

/**
 * DAO for managing user favorites.
 * Stores favorites as "userId:mediaId" strings in a Set.
 */
public class FavoriteDao {
    // Storage for favorites in the format "userId:mediaId"
    private final Set<String> favorites = new HashSet<>();

    public void add(int userId, int mediaId) {
        favorites.add(userId + ":" + mediaId);
    }

    public void remove(int userId, int mediaId) {
        favorites.remove(userId + ":" + mediaId);
    }

}
