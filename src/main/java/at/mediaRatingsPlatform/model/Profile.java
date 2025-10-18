package at.mediaRatingsPlatform.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;


@Data // Generates getters, setters, toString(), equals(), and hashCode() automatically for all fields
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor that includes all declared fields
public class Profile {

    // --- Basic Information ---
    private User user;
    private LocalDateTime creationTime;
    private LocalDateTime lastUpdTime;

    // --- Statistics ---
    private int totalRatings;
    private double averageScore;
    private GenreEnum favouriteGenre;
    private ArrayList<Media> favouriteMediaList;

    // --- Optional Fields ---
    private String bio;
    private String avatarUrl;

    /**
     * Adds a media item to the user's list of favorites, if it is not already included.
     *
     * @param media the media item to add to the favorites list
     */
    public void addFavorite(Media media) {
        if (!favouriteMediaList.contains(media)) {
            favouriteMediaList.add(media);
        }
    }

    /**
     * Removes a media item from the user's list of favorites, if it exists.
     *
     * @param media the media item to remove from the favorites list
     */
    public void removeFavorite(Media media) {
        favouriteMediaList.remove(media);
    }
}
