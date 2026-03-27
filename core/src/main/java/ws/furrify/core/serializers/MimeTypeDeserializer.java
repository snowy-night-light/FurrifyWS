package ws.furrify.core.serializers;

import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.util.MimeType;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

@JacksonComponent
public class MimeTypeDeserializer extends ValueDeserializer<MimeType> {

    @Override
    public MimeType deserialize(JsonParser p, DeserializationContext context) {
        String value = p.getValueAsString();
        return (value != null) ? MimeType.valueOf(value) : null;
    }
}