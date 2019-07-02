package cvurl.usage.plain.java;

import coresearch.cvurl.io.exception.UnexpectedResponseException;
import cvurl.usage.plain.java.model.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cvurl.usage.plain.java.CVurlUsageExample.*;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Example of CVurl usage using plain java");

        var userDto = new UserDto("name", "job");

        logResult(listUsers(1));
        logResult(singleUser(1));

        try {
            logResult(singleUserNotFound());
        } catch (UnexpectedResponseException e) {
            LOGGER.error(e.getMessage());
        }

        logResult(singleUserAsString(1));
        logResult(singleUserAsJson(1));
        logResult(createUser(userDto));
        logResult(updateUser(userDto, 1));
        logResult(deleteUser(1));
    }

    private static void logResult(Object result) {
        LOGGER.info("Request result: {}", result);
    }
}
