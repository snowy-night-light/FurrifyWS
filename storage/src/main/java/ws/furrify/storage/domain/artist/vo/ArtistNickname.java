package ws.furrify.storage.domain.artist.vo;


import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = PATTERN)
    private String nickname;

    @NonNull
    private Integer priority;

    /**
     * Artist nickname regex pattern
     */
    public final static String PATTERN = "^\\S+(?: \\S+)*$";

}