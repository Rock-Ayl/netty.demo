package cn.ayl.util.json;

import org.bson.json.JsonMode;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;

import java.io.Writer;

public class BsonDocWriter extends JsonWriter {

    public static JsonWriterSettings.Builder builder;

    static {
        builder = JsonWriterSettings.builder();
        builder.outputMode(JsonMode.RELAXED);
    }

    public BsonDocWriter(Writer writer) {
        super(writer, builder.build());
    }

    public void updateState(State value) {
        this.setState(value);
    }


}
