package ws.furrify.core.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public abstract class WebConfig {

    @Bean
    public OpenApiCustomizer operationIdCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    String tagName = (operation.getTags() != null && !operation.getTags().isEmpty())
                            ? operation.getTags().getFirst()
                            : "Default";

                    String originalId = operation.getOperationId();

                    String cleanTag = tagName.replaceAll("\\s+", "");

                    operation.setOperationId(cleanTag + "_" + originalId);
                })
        );
    }

    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }
}
