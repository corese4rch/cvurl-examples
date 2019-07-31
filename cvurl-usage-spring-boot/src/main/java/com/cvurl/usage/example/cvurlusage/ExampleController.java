package com.cvurl.usage.example.cvurlusage;

import com.cvurl.usage.example.cvurlusage.model.GetUsersDto;
import com.cvurl.usage.example.cvurlusage.model.User;
import com.cvurl.usage.example.cvurlusage.model.UserDto;
import coresearch.cvurl.io.constant.HttpHeader;
import coresearch.cvurl.io.constant.HttpStatus;
import coresearch.cvurl.io.constant.MIMEType;
import coresearch.cvurl.io.exception.ResponseMappingException;
import coresearch.cvurl.io.model.Response;
import coresearch.cvurl.io.multipart.MultipartBody;
import coresearch.cvurl.io.multipart.Part;
import coresearch.cvurl.io.request.CVurl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@RestController
public class ExampleController {

    private static final String HOST = "http://localhost:7000/";
    private static final String USERS = "users/";
    private static final String PHOTOS = "photos/";
    private static final int NON_EXISTENT_USER_ID = 23;

    private static final Logger logger = LoggerFactory.getLogger(ExampleController.class);

    private final CVurl cVurl;

    public ExampleController(CVurl cVurl) {
        this.cVurl = cVurl;
    }

    @ExceptionHandler(ResponseMappingException.class)
    public ResponseEntity handleException(ResponseMappingException exception) {
        var response = exception.getResponse();

        return ResponseEntity
                .status(response.status())
                .body(response.getBody());
    }

    /**
     * Make GET request to /users to get list of users with query param page (result is paginated) equals to passed parameter
     * page or 1 if none passed. Parse returned response body into {@link GetUsersDto} if response status is OK.
     */
    @GetMapping("/users")
    public GetUsersDto listUsers(@RequestParam(required = false) Integer page) {
        return cVurl.get(HOST + USERS)
                .queryParam("page", Objects.requireNonNullElse(page, 1).toString())
                .asObject(GetUsersDto.class);
    }

    /**
     * Make GET request to /users/{userId} to get single user, parse response body to {@link User}.
     */
    @GetMapping("/users/{userId}")
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
    @GetMapping("/user-not-found")
    public User singleUserNotFound() {
        return cVurl.get(HOST + USERS + NON_EXISTENT_USER_ID)
                .asObject(User.class);
    }

    /**
     * Make GET request to /users/{userId} to get single user, get response as string.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status is 200 return its body with status code 200, otherwise return response with given response status and response body.
     */
    @GetMapping("/user-as-string/{userId}")
    public ResponseEntity<String> singleUserAsString(@PathVariable String userId) {
        Response<String> response = cVurl.get(HOST + USERS + userId)
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response.getBody());
        } else {
            return ResponseEntity.status(response.status()).body(response.getBody());
        }
    }

    /**
     * Make GET request to /users/{userId} to get single user with AcceptEncoding header set to gzip by calling
     * acceptCompressed method, get uncompressed response as string.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status is 200 return its body with status code 200, otherwise return response with given response status and response body.
     */
    @GetMapping("/user-as-string-compressed/{userId}")
    public ResponseEntity<String> singleUserAsStringCompressed(@PathVariable String userId) {
        Response<String> response = cVurl.get(HOST + USERS + userId)
                .acceptCompressed()
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response.getBody());
        } else {
            return ResponseEntity.status(response.status()).body(response.getBody());
        }
    }

    /**
     * Make GET request to /users/{userId} to get single user, get response as input stream.
     * If empty optional returned from asStream which means that error happened during request sending then throw RuntimeException.
     * If response status is 200 return its body with status code 200, otherwise return response with given response status and response body.
     */
    @GetMapping("/user-as-is/{userId}")
    public ResponseEntity<byte[]> singleUserAsInputStream(@PathVariable String userId) throws IOException {
        Response<InputStream> response = cVurl.get(HOST + USERS + userId)
                .asStream()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody().readAllBytes());
        } else {
            return ResponseEntity.status(response.status()).body(response.getBody().readAllBytes());
        }
    }

    /**
     * Make POST request to /users to create user with request body parsed from {@link UserDto} object
     * with header Content-type = application/json, parse response body to {@link User} if response status code is CREATED,
     * otherwise empty Optional will be returned which means that some other response status code is arrived.
     */
    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    public User createUserFromJson(@RequestBody UserDto userDto) {
        return cVurl.post(HOST + USERS)
                .body(userDto)
                .header(HttpHeader.CONTENT_TYPE, MIMEType.APPLICATION_JSON)
                .asObject(User.class, HttpStatus.CREATED)
                .orElseThrow(() -> new RuntimeException("User can't be created"));
    }

    /**
     * Make POST request to /users to create user with request body as map with values userMap map
     * with header Content-type = application/x-www-form-urlencoded, parse response body to {@link User} if response status code is CREATED,
     * * otherwise empty Optional will be returned which means that some other response status code is arrived.
     */
    @PostMapping(value = "/users", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public User createUserFromFormUrlencoded(@RequestParam Map<String, String> userMap) {
        return cVurl.post(HOST + USERS)
                .formData(userMap)
                .header(HttpHeader.CONTENT_TYPE, MIMEType.APPLICATION_FORM)
                .asObject(User.class, HttpStatus.CREATED)
                .orElseThrow(() -> new RuntimeException("User can't be created"));
    }


    /**
     * Make PUT request to /users/{userId} to update user with userId equals to provided userId using request body parsed from {@link UserDto} object
     * with header Content-type = application/json, parse response body to {@link User}.
     */
    @PutMapping("/users/{userId}")
    public User updateUser(@RequestBody UserDto userDto, @PathVariable String userId) {
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
    @DeleteMapping("/users/{userId}")
    public ResponseEntity deleteUser(@PathVariable String userId) {
        Response<String> response = cVurl.delete(HOST + USERS + userId)
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.NO_CONTENT) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getBody());
        }
    }

    /**
     * Make POST request to /photos with body of content type multipart/form-data which consists of the given
     * photo file and title. Saves photo identified by title in in-memory database on the server.
     */
    @PostMapping("/photos")
    public ResponseEntity uploadPhoto(@RequestParam MultipartFile photo, @RequestParam String title) throws IOException {
        Response<String> response = cVurl.post(HOST + PHOTOS)
                .body(MultipartBody.create()
                        .formPart("title", Part.of(title))
                        .formPart("photo", Part.of(photo.getName(), photo.getContentType(), photo.getBytes())))
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        return ResponseEntity.status(response.status()).build();
    }

    /**
     * Create temporary file, and make GET request to /photos/{title} with BodyHandlers.ofFile writing
     * response content to created file. Returns response with code NOT_FOUND if no photo was found for
     * provided title or content of temporary file with content type from response. Delete temporary file.
     */
    @GetMapping("/photos/{title}")
    public ResponseEntity getPhoto(@PathVariable String title) throws IOException {
        Path path = Paths.get("src/main/resources/temp_photo");
        Files.createFile(path);
        Response<Path> response = cVurl.get(HOST + PHOTOS + title)
                .as(HttpResponse.BodyHandlers.ofFile(path))
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.NOT_FOUND) {
            return ResponseEntity.notFound().build();
        }
        String mediaType = response.getHeaderValue(HttpHeader.CONTENT_TYPE).orElseThrow(IllegalStateException::new);
        ResponseEntity<byte[]> responseEntity = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaType))
                .body(Files.readAllBytes(path));

        Files.delete(path);
        return responseEntity;
    }
}
