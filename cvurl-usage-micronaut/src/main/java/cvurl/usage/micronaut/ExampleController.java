package cvurl.usage.micronaut;

import coresearch.cvurl.io.constant.HttpHeader;
import coresearch.cvurl.io.constant.HttpStatus;
import coresearch.cvurl.io.exception.ResponseMappingException;
import coresearch.cvurl.io.mapper.BodyType;
import coresearch.cvurl.io.model.Response;
import coresearch.cvurl.io.multipart.MultipartBody;
import coresearch.cvurl.io.multipart.Part;
import coresearch.cvurl.io.request.CVurl;
import coresearch.cvurl.io.util.Url;
import cvurl.usage.micronaut.model.GetUsersDto;
import cvurl.usage.micronaut.model.User;
import cvurl.usage.micronaut.model.UserDto;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ExampleController {

    private static final String HOST = "http://localhost:7000/";
    private static final String USERS = "users/";
    private static final String PHOTOS = "photos/";
    private static final int NON_EXISTENT_USER_ID = 23;

    private final CVurl cVurl;

    public ExampleController(CVurl cVurl) {
        this.cVurl = cVurl;
    }

    @Error(ResponseMappingException.class)
    public HttpResponse handleException(ResponseMappingException exception) {
        var response = exception.getResponse();
        return HttpResponse.status(getStatus(response)).body(response.getBody());
    }

    /**
     * Make GET request to /users to get list of users with query param page (result is paginated) equals to passed parameter
     * page or 1 if none passed. Parse returned response body into {@link GetUsersDto} if response status is OK.
     */
    @Get("/users")
    public GetUsersDto listUsers(@QueryValue Optional<Integer> page) {
        return cVurl.get(HOST + USERS)
                .queryParam("page", page.orElse(1).toString())
                .asObject(GetUsersDto.class);
    }

    /**
     * Make GET request to /users/{userId} to get single user, parse response body to {@link User}.
     */
    @Get("/users/{userId}")
    public User singleUser(@PathVariable Integer userId) {
        return cVurl.get(HOST + USERS + userId)
                .asObject(User.class);
    }

    /**
     * Make GET request to /users/{userId} to get single user with id of not existent user. It should
     * throw {@link ResponseMappingException} because response body would differ from what we expect it to be.
     * Exception is handled by {@link #handleException(ResponseMappingException)} method
     * defined above.
     */
    @Get("/user-not-found")
    public User singleUserNotFound() {
        return cVurl.get(HOST + USERS + NON_EXISTENT_USER_ID)
                .asObject(User.class);
    }

    /**
     * Make GET request to /users/{userId} to get single user, get response as string.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status is 200 return its body with status code 200, otherwise return response with given response status and response body.
     */
    @Get("/user-as-string/{userId}")
    public HttpResponse singleUserAsString(@PathVariable String userId) {
        Response<String> response = cVurl.get(HOST + USERS + userId)
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return HttpResponse.ok().contentType(MediaType.APPLICATION_JSON).body(response.getBody());
        } else {
            return HttpResponse.status(getStatus(response)).body(response.getBody());
        }
    }

    private io.micronaut.http.HttpStatus getStatus(Response<?> response) {
        return io.micronaut.http.HttpStatus.valueOf(response.status());
    }

    /**
     * Make GET request to /users/{userId} to get single user with AcceptEncoding header set to gzip by calling
     * acceptCompressed method, get uncompressed response as string.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status is 200 return its body with status code 200, otherwise return response with given response status and response body.
     */
    @Get("/user-as-string-compressed/{userId}")
    public HttpResponse singleUserAsStringCompressed(@PathVariable String userId) {
        Response<String> response = cVurl.get(HOST + USERS + userId)
                .acceptCompressed()
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return HttpResponse.ok().contentType(MediaType.APPLICATION_JSON).body(response.getBody());
        } else {
            return HttpResponse.status(getStatus(response)).body(response.getBody());
        }
    }

    /**
     * Make GET request to /users/{userId} to get single user, get response as input stream.
     * If empty optional returned from asStream which means that error happened during request sending then throw RuntimeException.
     * If response status is 200 return its body with status code 200, otherwise return response with given response status and response body.
     */
    @Get("/user-as-is/{userId}")
    public HttpResponse singleUserAsInputStream(@PathVariable String userId) throws IOException {
        Response<InputStream> response = cVurl.get(HOST + USERS + userId)
                .asStream()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return HttpResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody().readAllBytes());
        } else {
            return HttpResponse.status(getStatus(response)).body(response.getBody().readAllBytes());
        }
    }

    /**
     * Make POST request to /users to create user with request body parsed from {@link UserDto} object
     * with header Content-type = application/json, parse response body to {@link User} if response status code is CREATED,
     * otherwise empty Optional will be returned which means that some other response status code is arrived.
     */
    @Post("/users")
    @Consumes(MediaType.APPLICATION_JSON)
    public User createUserFromJson(@Body UserDto userDto) {
        return cVurl.post(HOST + USERS)
                .body(userDto)
                .header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .asObject(User.class, HttpStatus.CREATED)
                .orElseThrow(() -> new RuntimeException("User can't be created"));
    }

    /**
     * Make POST request to /users to create user with request body as map with values userMap map
     * with header Content-type = application/x-www-form-urlencoded, parse response body to {@link User} if response status code is CREATED,
     * * otherwise empty Optional will be returned which means that some other response status code is arrived.
     */
    @Post("/users")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public User createUserFromFormUrlencoded(@Body Map<String, String> userMap) {
        return cVurl.post(HOST + USERS)
                .formData(userMap)
                .asObject(User.class, HttpStatus.CREATED)
                .orElseThrow(() -> new RuntimeException("User can't be created"));
    }


    /**
     * Make PUT request to /users/{userId} to update user with userId equals to provided userId using request body parsed from {@link UserDto} object
     * with header Content-type = application/json, parse response body to {@link User}.
     */
    @Put("/users/{userId}")
    public User updateUser(@Body UserDto userDto, @PathVariable String userId) {
        return cVurl.put(HOST + USERS + userId)
                .body(userDto)
                .headers(Map.of(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON))
                .asObject(User.class);
    }

    /**
     * Make DELETE request to /users/{userId} to delete user with userId equals to provided userId.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status equals NO_CONTENT then return response with same content and return response with BAD_REQUEST status code
     * and body from given response.
     */
    @Delete("/users/{userId}")
    public HttpResponse deleteUser(@PathVariable String userId) {
        Response<String> response = cVurl.delete(HOST + USERS + userId)
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.NO_CONTENT) {
            return HttpResponse.noContent();
        } else {
            return HttpResponse.badRequest().body(response.getBody());
        }
    }

    /**
     * Make POST request to /photos with body of content type multipart/form-data which consists of the given
     * photo file and title. Saves photo identified by title in in-memory database on the server.
     */
    @Post("/photos")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public HttpResponse uploadPhoto(CompletedFileUpload photo, String title) throws IOException {
        MediaType mediaType = photo.getContentType()
                .orElseThrow(() -> new RuntimeException("Photo Content-type header should be set"));

        Response<String> response = cVurl.post(HOST + PHOTOS)
                .body(MultipartBody.create()
                        .formPart("title", coresearch.cvurl.io.multipart.Part.of(title))
                        .formPart("photo", Part.of(photo.getName(), mediaType.getName(), photo.getBytes())))
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        return HttpResponse.status(getStatus(response));
    }

    /**
     * Create temporary file, and make GET request to /photos/{title} with BodyHandlers.ofFile writing
     * response content to created file. Returns response with code NOT_FOUND if no photo was found for
     * provided title or content of temporary file with content type from response. Delete temporary file.
     */
    @Get("/photos/{title}")
    public HttpResponse getPhoto(@PathVariable String title) throws IOException {
        Path path = Paths.get("src/main/resources/temp_photo");
        Files.createFile(path);
        Response<Path> response = cVurl.get(HOST + PHOTOS + title)
                .as(java.net.http.HttpResponse.BodyHandlers.ofFile(path))
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.NOT_FOUND) {
            return HttpResponse.notFound();
        }
        String mediaType = response.getHeaderValue(HttpHeader.CONTENT_TYPE).orElseThrow(IllegalStateException::new);
        HttpResponse httpResponse = HttpResponse.ok()
                .contentType(mediaType)
                .body(Files.readAllBytes(path));

        Files.delete(path);
        return httpResponse;
    }

    /**
     * Makes GET request to /users/list endpoint that return json array of objects.
     * Parses it to List<User> by using BodyType.
     *
     * @return List of users
     */
    @Get("/users/list")
    public List<User> getUsersAsList() {
        return cVurl.get(Url.of(HOST).path(USERS).path("list").create())
                .asObject(new BodyType<>() {});
    }

}
