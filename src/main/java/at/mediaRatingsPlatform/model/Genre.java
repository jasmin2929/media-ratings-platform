package at.mediaRatingsPlatform.model;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre {
    private UUID id;
    private String name;
    private String description;
}
