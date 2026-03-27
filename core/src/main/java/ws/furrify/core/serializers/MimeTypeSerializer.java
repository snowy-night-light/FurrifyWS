package ws.furrify.core.serializers;

import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.util.MimeType;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

@JacksonComponent
public class MimeTypeSerializer extends ValueSerializer<MimeType> {

    @Override
    public void serialize(MimeType value, JsonGenerator gen, SerializationContext context) {
        gen.writeString(value.toString());
    }
}