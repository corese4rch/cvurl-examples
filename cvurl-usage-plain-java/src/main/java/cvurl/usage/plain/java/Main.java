package cvurl.usage.plain.java;

import coresearch.cvurl.io.exception.ResponseMappingException;
import cvurl.usage.plain.java.model.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static cvurl.usage.plain.java.CVurlUsageExample.*;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String PHOTO_PATH = "src/main/resources/test_photo.jpg";
    private static final String PHOTO_TITLE = "test-photo";
    private static final String GET_PHOTO_PATH = "src/main/resources/photo_from_server.jpg";

    public static void main(String[] args) throws IOException {
        LOGGER.info("Example of CVurl usage using plain java");

        var userDto = new UserDto("name", "job");

        logResult(listUsers(1));
        logResult(singleUser(1));

        try {
            logResult(singleUserNotFound());
        } catch (ResponseMappingException e) {
            var response = e.getResponse();
            LOGGER.error("Unexpected response with status {}, body {}", response.status(), response.getBody());
        }

        logResult(singleUserAsString(1));
        logResult(singleUserAsStringCompressed(1));
        logResult(new String(singleUserAsInputStream(1).readAllBytes()));
        logResult(createUserFromJson(userDto));
        logResult(createUserFromFormUrlencoded(Map.of("name", userDto.getName(), "email", userDto.getEmail())));
        logResult(uploadPhoto(Path.of(PHOTO_PATH), PHOTO_TITLE));
        logResult(getPhoto(PHOTO_TITLE, GET_PHOTO_PATH));
        logResult(getUsersAsList());
    }

    private static void logResult(Object result) {
        LOGGER.info("Request result: {}", result);
    }
}
