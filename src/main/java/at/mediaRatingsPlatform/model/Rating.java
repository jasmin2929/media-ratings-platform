package at.mediaRatingsPlatform.model;

import lombok.*;
import java.time.LocalDateTime;


@Data // Generates getters, setters, toString(), equals(), and hashCode() automatically for all fields
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor that includes all declared fields
@Builder // Allows building objects in a clean, readable way using the builder pattern
public class Rating {

    private int id;
    private int mediaId;
    private int userId;
    private int stars;
    private String comment;

    private RatingStatusEnum status;
    private LocalDateTime creationTime;
    private LocalDateTime lastUpdTime;
    private int totalLikes;

    //Increments the total number of likes by one.
    public void incrementLikes() {
        this.totalLikes++;
    }
}
