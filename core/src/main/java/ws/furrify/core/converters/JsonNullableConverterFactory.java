package ws.furrify.core.converters;

import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class JsonNullableConverterFactory implements ConverterFactory<Object, JsonNullable<?>> {

    @Override
    public <T extends JsonNullable<?>> Converter<Object, T> getConverter(Class<T> targetType) {
        return new JsonNullableConverter();
    }

    private static class JsonNullableConverter<T> implements Converter<Object, JsonNullable<T>> {
        @Override
        public JsonNullable<T> convert(Object source) {
            return JsonNullable.of((T) source);
        }
    }
}