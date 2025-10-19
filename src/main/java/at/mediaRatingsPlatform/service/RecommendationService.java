package at.mediaRatingsPlatform.service;

import at.mediaRatingsPlatform.dao.MediaDao;
import at.mediaRatingsPlatform.dao.RatingDao;
import at.mediaRatingsPlatform.exception.NotFoundException;
import at.mediaRatingsPlatform.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {
    private final MediaDao mediaDao;
    private final RatingDao ratingDao;

    public RecommendationService(MediaDao mediaDao, RatingDao ratingDao) {
        this.mediaDao = mediaDao;
        this.ratingDao = ratingDao;
    }

    public List<Media> getRecommendations(User user) {
        if (user == null)
            throw new NotFoundException("User not found");

        // Define favorite genres of the user
        Set<GenreEnum> likedGenres = ratingDao.getAll().stream()
                .filter(r -> r.getUserId() == user.getId())
                .filter(r -> r.getStars() >= 4 && r.getStatus() == RatingStatusEnum.CONFIRMED)
                .map(r -> mediaDao.getById(r.getMediaId()))
                .filter(Objects::nonNull)
                .flatMap(m -> m.getGenreList().stream())
                .collect(Collectors.toSet());

        // recommend similar genres
        return mediaDao.getAll().stream()
                .filter(m -> !likedGenres.isEmpty() && m.getGenreList().stream().anyMatch(likedGenres::contains))
                .limit(3)
                .collect(Collectors.toList());
    }
}
