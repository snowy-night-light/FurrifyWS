/*
 * furrify-attachment-service - Furrify Workspace Project
 * Copyright © 2026 FurrifyWS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.furrify.attachment.domain.file.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.ColumnDefault;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * AttachmentFile hash wrapper.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttachmentFileHash {

    /** Marks the constructor MapStruct maps with (it picks one annotated with any annotation named Default). */
    @Retention(java.lang.annotation.RetentionPolicy.CLASS)
    @Target(java.lang.annotation.ElementType.CONSTRUCTOR)
    @interface Default {
    }

    @NonNull
    @NotBlank
    private String hash;

    @NonNull
    private AttachmentFileHashType type;

    /** Hashes stored before conversion existed are of the uploaded bytes: the column default makes them ORIGINAL. */
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'ORIGINAL'")
    private AttachmentFileHashTarget target = AttachmentFileHashTarget.ORIGINAL;

    @Default
    public AttachmentFileHash(@NonNull String hash, @NonNull AttachmentFileHashType type, AttachmentFileHashTarget target) {
        this.hash = hash;
        this.type = type;
        this.target = target != null ? target : AttachmentFileHashTarget.ORIGINAL;
    }

    public AttachmentFileHash(@NonNull String hash, @NonNull AttachmentFileHashType type) {
        this(hash, type, AttachmentFileHashTarget.ORIGINAL);
    }
}