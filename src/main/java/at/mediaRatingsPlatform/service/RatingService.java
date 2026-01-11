package at.mediaRatingsPlatform.service;

import at.mediaRatingsPlatform.dao.MediaDao;
import at.mediaRatingsPlatform.dao.RatingDao;
import at.mediaRatingsPlatform.exception.ForbiddenException;
import at.mediaRatingsPlatform.exception.NotFoundException;
import at.mediaRatingsPlatform.model.Media;
import at.mediaRatingsPlatform.model.Rating;
import at.mediaRatingsPlatform.model.RatingStatusEnum;
import at.mediaRatingsPlatform.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RatingService {
    private final RatingDao ratingDao;
    private final MediaDao mediaDao;

    public RatingService(RatingDao ratingDao, MediaDao mediaDao) {
        this.ratingDao = ratingDao;
        this.mediaDao = mediaDao;
    }


    public Rating create(UUID mediaId, int stars, String comment, User user){
        Media media = mediaDao.getById(mediaId);
        if (media == null)
            throw new NotFoundException("Media not found");

        // Enforce: one rating per user per media (editable)
        Rating existing = ratingDao.getAllByUserId(user.getId()).stream()
            .filter(r -> r.getMediaId().equals(mediaId))
            .findFirst()
            .orElse(null);

        if (existing != null) {
            existing.setStars(stars);
            existing.setComment(comment);
            existing.setStatus(RatingStatusEnum.PENDING); // comment must be re-confirmed after edit
            ratingDao.update(existing.getId(), existing);
            return existing;
        }
            Rating rating = new Rating();
            rating.setMediaId(mediaId);
            rating.setStars(stars);
            rating.setComment(comment);
            rating.setUserId(user.getId());
            rating.setStatus(RatingStatusEnum.PENDING);
        
        return ratingDao.create(rating);

    }

    // Confirm a rating (changes visibility)
    public Rating confirm(UUID ratingId, UUID userId) {
        Rating rating = ratingDao.getById(ratingId);
        if (rating == null)
            throw new NotFoundException("Rating not found");

        if (!rating.getUserId().equals(userId))
            throw new ForbiddenException("You are not allowed to confirm this rating");

        rating.setStatus(RatingStatusEnum.CONFIRMED);
        ratingDao.update(ratingId, rating);
        return rating;
    }

    // Edit a rating
    public Rating update(UUID ratingId, User user, int stars, String comment) {
        Rating rating = ratingDao.getById(ratingId);
        if (rating == null)
            throw new NotFoundException("Rating not found");
        if (!rating.getUserId().equals(user.getId()))
            throw new ForbiddenException("You can only edit your own ratings");

        rating.setStars(stars);
        rating.setComment(comment);
        ratingDao.update(ratingId, rating);
        return rating;
    }

    // Delete a rating
    public void delete(UUID ratingId, UUID userId) {
        Rating rating = ratingDao.getById(ratingId);
        if (rating == null)
            throw new NotFoundException("Rating not found");
        if (!rating.getUserId().equals(userId))
            throw new ForbiddenException("You can only delete your own ratings");

        ratingDao.delete(ratingId);
    }

    // Like a rating
    public Rating like(UUID ratingId) {
        Rating rating = ratingDao.getById(ratingId);
        if (rating == null)
            throw new NotFoundException("Rating not found");

        rating.incrementLikes();
        ratingDao.update(ratingId, rating);
        return rating;
    }

    // Get all ratings for a media
    public List<Rating> getAllByMediaId(UUID mediaId){
        return ratingDao.getAllByMediaId(mediaId);
    }

    // Get all confirmed ratings for a media
    public List<Rating> getAllConfirmedByMediaId(UUID mediaId) {
        return ratingDao.getAllByMediaId(mediaId).stream()
                .filter(r -> r.getStatus() == RatingStatusEnum.CONFIRMED)
                .collect(Collectors.toList());
    }

    // Get all ratings of a user
    public List<Rating> getAllByUserId(UUID userId) {
        return ratingDao.getAllByUserId(userId);
    }


}
