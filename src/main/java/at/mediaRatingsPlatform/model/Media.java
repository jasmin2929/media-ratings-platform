package at.mediaRatingsPlatform.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;


@Data // Generates getters, setters, toString(), equals(), and hashCode()
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor including all declared fields
@Builder // Enables the builder pattern for easy and readable object creation
public class Media {

    private int id;
    private int userId;
    private String title;
    private String description;
    private MediaTypeEnum mediaType;
    private int releaseYear;
    private ArrayList<GenreEnum> genreList;
    private int ageRestriction;
    private LocalDateTime creationTime;
    private LocalDateTime lastUpdTime;

    private int totalLikes;
    private ArrayList<Rating> ratingList;
}
