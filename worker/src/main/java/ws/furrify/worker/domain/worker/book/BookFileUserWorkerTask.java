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
package ws.furrify.worker.domain.worker.book;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ws.furrify.worker.domain.worker.UserWorkerTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookFileUserWorkerTask extends UserWorkerTask {

    @Column(nullable = false)
    UUID sourceBookReferenceId;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_file_user_worker_task_format_id_map",
            joinColumns = {@JoinColumn(name = "book_file_user_work_task_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "format")
    Map<String, UUID> formatReferenceIds = new HashMap<>();
}
