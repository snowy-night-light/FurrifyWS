/*
 * furrify-storage-service - Furrify Workspace Project
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
package ws.furrify.storage.controllers.tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.furrify.core.controller.BaseEntityRestController;
import ws.furrify.core.entity.request.BaseRequestMapper;
import ws.furrify.core.service.BaseEntityCrudService;
import ws.furrify.storage.domain.tag.alias.TagAlias;
import ws.furrify.storage.dto.tag.alias.TagAliasDTO;
import ws.furrify.storage.dto.tag.alias.request.CreateTagAliasRequest;
import ws.furrify.storage.dto.tag.alias.request.PatchTagAliasRequest;

@RestController
@RequestMapping("/v1/tags/aliases")
class TagAliasV1RestController extends BaseEntityRestController<TagAlias, TagAliasDTO, CreateTagAliasRequest, PatchTagAliasRequest> {

    @Autowired
    public TagAliasV1RestController(BaseRequestMapper<TagAlias, TagAliasDTO, CreateTagAliasRequest> requestDtoMapper, BaseEntityCrudService<TagAlias, TagAliasDTO, PatchTagAliasRequest> entityCrudService) {
        super(requestDtoMapper, entityCrudService);
    }
}
