package cvurl.usage.micronaut;

import coresearch.cvurl.io.request.CVurl;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class CVurlFactory {

    @Singleton
    public CVurl cVurl() {
        return new CVurl();
    }
}
