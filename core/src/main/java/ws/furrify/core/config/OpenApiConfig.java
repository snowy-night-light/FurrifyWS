package ws.furrify.core.config;

import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
public class OpenApiConfig {

    @Bean
    public ObjectMapperProvider springdocObjectMapperProvider(SpringDocConfigProperties springDocConfigProperties) {
        ObjectMapperProvider objectMapperProvider = new ObjectMapperProvider(springDocConfigProperties);
        objectMapperProvider.jsonMapper().registerModule(new JsonNullableModule());
        return objectMapperProvider;
    }
}
