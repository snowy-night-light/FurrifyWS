package ws.furrify.storage.domain.artist.vo;


import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

/**
 * Artist nickname wrapper.
 *
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class ArtistNickname {

    private final static short MAX_LENGTH = 256;

    @NonNull
    @NotBlank
    @Length(max = MAX_LENGTH)
    @jakarta.validation.constraints.Pattern(regexp = PATTERN)
    private String nickname;

    @NonNull
    private Integer priority;

    /**
     * Artist nickname regex pattern
     */
    public final static String PATTERN = "^[A-Za-z0-9._\\-]+$";

}