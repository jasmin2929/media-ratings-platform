package at.mediaRatingsPlatform.service;

import at.mediaRatingsPlatform.dao.RatingDao;
import at.mediaRatingsPlatform.dao.UserDao;
import at.mediaRatingsPlatform.model.Rating;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LeaderBoardService {
    private final UserDao userDao;
    private final RatingDao ratingDao;

    public LeaderBoardService(UserDao userDao, RatingDao ratingDao) {
        this.userDao = userDao;
        this.ratingDao = ratingDao;
    }

    /**
     * Returns a leaderboard as a list of maps:
     * [
     *   { "rank": 1, "username": "jess", "ratings": 10 },
     *   { "rank": 2, "username": "alex", "ratings": 7 }
     * ]
     */
    public List<Map<String, Object>> getLeaderBoard() {
        // Count ratings per user
        Map<UUID, Integer> counts = new HashMap<>();
        for (Rating r : ratingDao.getAll()) {
            counts.put(r.getUserId(), counts.getOrDefault(r.getUserId(), 0) + 1);
        }

        // Prepare result
        List<Map<String, Object>> result = new ArrayList<>();
        AtomicInteger rank = new AtomicInteger(1);

        userDao.getAll().stream()
                .sorted((a, b) -> counts.getOrDefault(b.getId(), 0) - counts.getOrDefault(a.getId(), 0))
                .forEach(u -> {
                    result.add(Map.of(
                            "rank", rank.getAndIncrement(),
                            "username", u.getUsername(),
                            "ratings", counts.getOrDefault(u.getId(), 0)
                    ));
                });

        return result;
    }
}
