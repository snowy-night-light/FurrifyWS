/*
 * furrify-core - Furrify Workspace Project
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
package ws.furrify.core.config;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public abstract class WebConfig  {

    /**
     * Make sure docs for enums are generated as separate enum classes not inline enums.
     */
    @Bean
    public ModelConverter enumModelConverter() {
        return (type, context, chain) -> {
            if (chain.hasNext()) {
                Schema<?> resolvedSchema = chain.next().resolve(type, context, chain);
                if (resolvedSchema != null) {
                    Class<?> clazz = null;
                    if (type.getType() instanceof Class<?>) {
                        clazz = (Class<?>) type.getType();
                    } else if (type.getType() instanceof JavaType) {
                        clazz = ((JavaType) type.getType()).getRawClass();
                    }

                    if (clazz != null && clazz.isEnum()) {
                        resolvedSchema.setName(clazz.getSimpleName());
                        context.defineModel(clazz.getSimpleName(), resolvedSchema);
                        Schema<?> refSchema = new Schema<>();
                        refSchema.$ref("#/components/schemas/" + clazz.getSimpleName());
                        return refSchema;
                    }
                }
                return resolvedSchema;
            }
            return null;
        };
    }

    @Bean
    public OperationCustomizer customOperationId() {
        return (operation, handlerMethod) -> {
            String controllerName = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();

            operation.setOperationId(controllerName + "_" + methodName);
            return operation;
        };
    }

    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }
}
