package at.mediaRatingsPlatform.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;


@Data // Generates getters, setters, toString(), equals(), and hashCode()
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all declared fields
@Builder // Enables the builder pattern for clean, fluent object creation
public class User {
    private UUID id;
    private String username;
    private String passwordHash;
    private LocalDateTime creationTime;
    private LocalDateTime lastUpdTime;
}
