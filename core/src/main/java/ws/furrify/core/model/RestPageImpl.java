/*
 * Copyright © 2026 FurrifyWS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ws.furrify.core.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RestPageImpl<T> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestPageImpl(@JsonProperty("content") List<T> content,
                        @JsonProperty("number") int number,
                        @JsonProperty("size") int size,
                        @JsonProperty("totalElements") Long totalElements,
                        @JsonProperty("pageable") JsonNode pageable,
                        @JsonProperty("last") boolean last,
                        @JsonProperty("totalPages") int totalPages,
                        @JsonProperty("sort") JsonNode sort,
                        @JsonProperty("first") boolean first,
                        @JsonProperty("numberOfElements") int numberOfElements) {
        super(content, PageRequest.of(number, size == 0 ? 1 : size), totalElements == null ? 0 : totalElements);
    }

    public RestPageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public RestPageImpl(List<T> content) {
        super(content);
    }

    public RestPageImpl() {
        super(new ArrayList<>());
    }
}