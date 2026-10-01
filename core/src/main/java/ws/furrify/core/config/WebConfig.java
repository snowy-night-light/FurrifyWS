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
