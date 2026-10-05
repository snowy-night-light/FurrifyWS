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
package ws.furrify.attachment.domain.file;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.attachment.domain.file.vo.AttachmentFileHash;
import ws.furrify.core.converters.URIConverter;
import ws.furrify.core.entity.UserScopedEntity;

import java.net.URI;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AttachmentFile extends UserScopedEntity {
    @Column(nullable = false, length = 255)
    @NotBlank
    private String fileName;

    @Column(nullable = false, length = 255)
    @NotBlank
    private String fileExtension;

    @Column(nullable = true)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "attachment_file_hashes", joinColumns = @JoinColumn(name = "attachment_file_id"))
    private List<AttachmentFileHash> fileHashes;

    @Column(nullable = true, length = 255)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private FileUploadStatus uploadStatus;

    @Column(nullable = true)
    private Long fileSize;

    @Column(nullable = true)
    @Convert(converter = URIConverter.class)
    private URI fileUri;

    @Column(nullable = true)
    @Convert(converter = URIConverter.class)
    private URI thumbnailUri;

    @Column(nullable = true)
    private String storageServiceId;


}