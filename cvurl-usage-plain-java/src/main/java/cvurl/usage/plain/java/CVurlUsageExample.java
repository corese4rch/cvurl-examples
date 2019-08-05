package cvurl.usage.plain.java;

import coresearch.cvurl.io.constant.HttpHeader;
import coresearch.cvurl.io.constant.HttpStatus;
import coresearch.cvurl.io.constant.MIMEType;
import coresearch.cvurl.io.exception.ResponseMappingException;
import coresearch.cvurl.io.model.Response;
import coresearch.cvurl.io.multipart.MultipartBody;
import coresearch.cvurl.io.multipart.Part;
import coresearch.cvurl.io.request.CVurl;
import cvurl.usage.plain.java.model.GetUsersDto;
import cvurl.usage.plain.java.model.User;
import cvurl.usage.plain.java.model.UserDto;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CVurlUsageExample {

    private static final String HOST = "http://localhost:7000/";
    private static final String USERS = "users/";
    private static final String PHOTOS = "photos/";
    private static final int NON_EXISTENT_USER_ID = 23;

    private static final CVurl cVurl = new CVurl();

    /**
     * Make GET request to /users to get list of users with query param page (result is paginated) equals to passed parameter
     * page or 1 if none passed. Parse returned response body into {@link GetUsersDto} if response status is OK.
     */
    public static GetUsersDto listUsers(Integer page) {
        return cVurl.get(HOST + USERS)
                .queryParam("page", Objects.requireNonNullElse(page, 1).toString())
                .asObject(GetUsersDto.class);
    }

    /**
     * Make GET request to /users/{userId} to get single user, parse response body to {@link User}.
     */
    public static User singleUser(Integer userId) {
        return cVurl.get(HOST + USERS + userId)
                .asObject(User.class);
    }

    /**
     * Make GET request to /users/{userId} to get single user with id of not existent user. It should
     * throw {@link ResponseMappingException} because response body would differ from what we expect it to be.
     */
    public static User singleUserNotFound() {
        return cVurl.get(HOST + USERS + NON_EXISTENT_USER_ID)
                .asObject(User.class);
    }

    /**
     * Make GET request to /users/{userId} to get single user, get response as string.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * <p>
     * If response status is 200 return its body, otherwise throw RuntimeException
     */
    public static String singleUserAsString(Integer userId) {
        Response<String> response = cVurl.get(HOST + USERS + userId)
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        return getBody(response);
    }

    /**
     * Make GET request to /users/{userId} to get single user with AcceptEncoding header set to gzip by calling
     * acceptCompressed method, get uncompressed response as string.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status is 200 return its body, otherwise throw RuntimeException.
     */
    public static String singleUserAsStringCompressed(Integer userId) {
        Response<String> response = cVurl.get(HOST + USERS + userId)
                .acceptCompressed()
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        return getBody(response);
    }

    /**
     * Make GET request to /users/{userId} to get single user, get response as input stream.
     * If empty optional returned from asStream which means that error happened during request sending then throw RuntimeException.
     * If response status is 200 return its body, otherwise throw RuntimeException..
     */
    public static InputStream singleUserAsInputStream(Integer userId) throws IOException {
        Response<InputStream> response = cVurl.get(HOST + USERS + userId)
                .asStream()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        return getBody(response);
    }

    /**
     * Make POST request to /users to create user with request body parsed from {@link UserDto} object
     * with header Content-type = application/json, parse response body to {@link User} if response status code is CREATED,
     * otherwise empty Optional will be returned which means that some other response status code is arrived.
     */
    public static User createUserFromJson(UserDto userDto) {
        return cVurl.post(HOST + USERS)
                .body(userDto)
                .header(HttpHeader.CONTENT_TYPE, MIMEType.APPLICATION_JSON)
                .asObject(User.class, HttpStatus.CREATED)
                .orElseThrow(() -> new RuntimeException("User can't be created"));
    }

    /**
     * Make POST request to /users to create user with request body as map with values userMap map
     * with header Content-type = application/x-www-form-urlencoded, parse response body to {@link User} if response status code is CREATED,
     * * otherwise empty Optional will be rseturned which means that some other response status code is arrived.
     */
    public static User createUserFromFormUrlencoded(Map<String, String> userMap) {
        return cVurl.post(HOST + USERS)
                .formData(userMap)
                .asObject(User.class, HttpStatus.CREATED)
                .orElseThrow(() -> new RuntimeException("User can't be created"));
    }


    /**
     * Make PUT request to /users/{userId} to update user with userId equals to provided userId using request body parsed from {@link UserDto} object
     * with header Content-type = application/json, parse response body to {@link User}.
     */
    public static User updateUser(UserDto userDto, String userId) {
        return cVurl.put(HOST + USERS + userId)
                .body(userDto)
                .headers(Map.of(HttpHeader.CONTENT_TYPE, MIMEType.APPLICATION_JSON))
                .asObject(User.class);
    }

    /**
     * Make DELETE request to /users/{userId} to delete user with userId equals to provided userId.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status equals NO_CONTENT then return response with same content and return response with BAD_REQUEST status code
     * and body from given response.
     */
    public static boolean deleteUser(String userId) {
        Response<String> response = cVurl.delete(HOST + USERS + userId)
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        return response.status() == HttpStatus.NO_CONTENT;
    }

    /**
     * Make POST request to /photos with body of content type multipart/form-data which consists of the given
     * photo file and title. Saves photo identified by title in in-memory database on the server.
     */
    public static boolean uploadPhoto(Path photo, String title) throws IOException {
        Response<String> response = cVurl.post(HOST + PHOTOS)
                .body(MultipartBody.create()
                        .formPart("title", Part.of(title))
                        .formPart("photo", Part.of(photo)))
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        return response.status() == HttpStatus.NO_CONTENT;
    }

    /**
     * Create temporary file, and make GET request to /photos/{title} with BodyHandlers.ofFile writing
     * response content to created file. Creates new file with photo got from the servrer.
     */
    public static boolean getPhoto(String title, String getPhotoPath) throws IOException {
        Path path = Paths.get(getPhotoPath);
        Files.deleteIfExists(path);
        Files.createFile(path);
        Response<Path> response = cVurl.get(HOST + PHOTOS + title)
                .as(HttpResponse.BodyHandlers.ofFile(path))
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        return response.status() == HttpStatus.OK;
    }

    private static <T> T getBody(Response<T> response) {
        if (response.status() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Bad response with status code " + response.status() +
                    " and body " + response.getBody());
        }
    }
}
