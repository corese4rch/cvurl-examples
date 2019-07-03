package cvurl.usage.quarkus;

import coresearch.cvurl.io.request.CVurl;
import javax.enterprise.inject.Produces;
import javax.json.bind.JsonbBuilder;

public class CVurlFactory {

    @Produces
    public CVurl cVurl() {
        return new CVurl(new JsonbMapper(JsonbBuilder.create()));
    }
}
