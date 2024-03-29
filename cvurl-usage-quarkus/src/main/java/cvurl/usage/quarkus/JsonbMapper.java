package cvurl.usage.quarkus;

import coresearch.cvurl.io.exception.MappingException;
import coresearch.cvurl.io.mapper.BodyType;
import coresearch.cvurl.io.mapper.GenericMapper;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbException;

public class JsonbMapper extends GenericMapper {

    private Jsonb jsonb;

    public JsonbMapper(Jsonb jsonb) {
        this.jsonb = jsonb;
    }

    @Override
    public <T> T readValue(String value, Class<T> type) {
        try {
            return jsonb.fromJson(value, type);
        } catch (JsonbException e) {
            throw new MappingException(e.getMessage(), e);
        }
    }

    @Override
    public String writeValue(Object obj) {
        try {
            return jsonb.toJson(obj);
        } catch (JsonbException e) {
            throw new MappingException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T readValue(String str, BodyType<T> bodyType) {
        return jsonb.fromJson(str, bodyType.getType());
    }
}
