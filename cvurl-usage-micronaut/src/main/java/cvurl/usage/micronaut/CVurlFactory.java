package cvurl.usage.micronaut;

import coresearch.cvurl.io.request.CVurl;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class CVurlFactory {

    @Singleton
    public CVurl cVurl() {
        return new CVurl();

        /*
        other ways you can create CVurl:
        new CVurl(Configuration.builder(httpClient)
                .genericMapper(genericMapper)
                .build());
        new CVurl(Configuration.builder()
                .executor(Executors.newFixedThreadPool(3))
                .requestTimeout(Duration.ofSeconds(5))
                .build());
        */
    }
}
