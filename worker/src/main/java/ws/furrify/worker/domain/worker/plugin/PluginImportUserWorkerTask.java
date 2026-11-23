/*
 * furrify-worker-service - Furrify Workspace Project
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
package ws.furrify.worker.domain.worker.plugin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.worker.domain.worker.UserWorkerTask;

import java.util.UUID;
import org.hibernate.annotations.ColumnDefault;

@Entity
// Scheduler queries (UserWorkerTaskBaseEntityService): the 30 s poll filters status (+ start_at) ordered by
// created_at; the stuck-task check filters status + launch_id. The table grows with every task ever run.
@Table(indexes = {
        @Index(name = "idx_piuwt_status_created", columnList = "status, created_at"),
        @Index(name = "idx_piuwt_status_launch", columnList = "status, launch_id"),
        @Index(name = "idx_piuwt_owner", columnList = "owner_id")
})
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PluginImportUserWorkerTask extends UserWorkerTask {
    @Column(nullable = false)
    @NotNull
    UUID fileReferenceId;

    @Column(nullable = false)
    @NotBlank
    String provider;

    @Column(nullable = false)
    Boolean downloadExternalMedia;

    @Column(nullable = false)
    UUID destinationLibraryReferenceId;

    /**
     * Original name of the imported file (from the attachment, set on create) for display in task
     * details. Kept here because the attachment is deleted once the import completes. Nullable:
     * tasks created before this column existed have none.
     */
    @Column(nullable = true, length = 255)
    String fileName;

    /**
     * The imported archive was deleted from the attachment service (the task ended, nothing needs
     * it any more). The column default makes existing tasks false, so the cleanup job finds them.
     */
    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("false")
    Boolean fileRemoved = false;
}
