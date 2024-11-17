package eb.javafx.klik.api;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eb.javafx.klik.model.UserCounter;

import java.io.IOException;

public class UserCounterDeserializer extends StdDeserializer<UserCounter> {

    public static final String JSON_USER_ID = "USER_ID";
    public static final String JSON_CLICK_COUNT = "CLICK_COUNT";
    public static final String JSON_UPDATED_AT = "UPDATED_AT";

    public UserCounterDeserializer() {
        this(null);
    }

    public UserCounterDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public UserCounter deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return new UserCounter(node.get(JSON_USER_ID).asText(), node.get(JSON_CLICK_COUNT).asInt(), node.get(JSON_UPDATED_AT).asText());
    }
}
