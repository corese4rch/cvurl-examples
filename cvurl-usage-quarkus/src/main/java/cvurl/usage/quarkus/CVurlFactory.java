package cvurl.usage.quarkus;

import coresearch.cvurl.io.request.CVurl;
import javax.enterprise.inject.Produces;
import javax.json.bind.JsonbBuilder;

public class CVurlFactory {

    @Produces
    public CVurl cVurl() {
        return new CVurl(new JsonbMapper(JsonbBuilder.create()));

        /*
        other ways you can create CVurl:
        new CVurl(Configuration.builder(httpClient)
                .genericMapper(new JsonbMapper(JsonbBuilder.create()))
                .build());
        new CVurl(Configuration.builder()
                .executor(Executors.newFixedThreadPool(3))
                .requestTimeout(Duration.ofSeconds(5))
                .build());
        */

    }
}
