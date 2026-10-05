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
package ws.furrify.worker.dto.worker.book;

import org.mapstruct.Mapper;
import ws.furrify.core.entity.dto.BaseDTOMapper;
import ws.furrify.worker.domain.worker.book.BookFileUserWorkerTask;
import ws.furrify.worker.dto.worker.book.request.PatchBookFileUserWorkerTaskRequest;

@Mapper(
        config = BaseDTOMapper.class,
        uses = {}
)
public interface BookFileUserWorkerTaskDTOMapper extends BaseDTOMapper<BookFileUserWorkerTask, BookFileUserWorkerTaskDTO, PatchBookFileUserWorkerTaskRequest> {
}