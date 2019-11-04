package cvurl.usage.quarkus;

import coresearch.cvurl.io.constant.HttpHeader;
import coresearch.cvurl.io.constant.HttpStatus;
import coresearch.cvurl.io.constant.MIMEType;
import coresearch.cvurl.io.exception.ResponseMappingException;
import coresearch.cvurl.io.mapper.BodyType;
import coresearch.cvurl.io.model.Response;
import coresearch.cvurl.io.multipart.MultipartBody;
import coresearch.cvurl.io.multipart.Part;
import coresearch.cvurl.io.request.CVurl;
import coresearch.cvurl.io.util.Url;
import cvurl.usage.quarkus.model.GetUsersDto;
import cvurl.usage.quarkus.model.User;
import cvurl.usage.quarkus.model.UserDto;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.ok;

@Path("/")
public class ExampleResource {

    private static final String HOST = "http://localhost:7000/";
    private static final String USERS = "users/";
    private static final String PHOTOS = "photos/";
    private static final int NON_EXISTENT_USER_ID = 23;

    private final CVurl cVurl;

    @Inject
    public ExampleResource(CVurl cVurl) {
        this.cVurl = cVurl;
    }

    /**
     * Make GET request to /users to get list of users with query param page (result is paginated) equals to passed parameter
     * page or 1 if none passed. Parse returned response body into {@link GetUsersDto} if response status is OK.
     */
    @GET
    @Produces(APPLICATION_JSON)
    @Path("/users")
    public GetUsersDto listUsers(@DefaultValue("1") @QueryParam("page") Integer page) {
        return cVurl.get(HOST + USERS)
                .queryParam("page", Objects.requireNonNullElse(page, 1).toString())
                .asObject(GetUsersDto.class);
    }

    /**
     * Make GET request to /users/{userId} to get single user, parse response body to {@link User}.
     */
    @GET
    @Produces(APPLICATION_JSON)
    @Path("/users/{userId}")
    public User singleUser(@PathParam("userId") Integer userId) {
        return cVurl.get(HOST + USERS + userId)
                .asObject(User.class);
    }

    /**
     * Make GET request to /users/{userId} to get single user with id of not existent user. It should
     * throw {@link ResponseMappingException} because response body would differ from what we expect it to be.
     * Exception is handled by {@link ResponseMappingExceptionMapper}.
     * defined above.
     */
    @GET
    @Path("/user-not-found")
    public User singleUserNotFound() {
        return cVurl.get(HOST + USERS + NON_EXISTENT_USER_ID)
                .asObject(User.class);
    }

    /**
     * Make GET request to /users/{userId} to get single user, get response as string.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status is 200 return its body with status code 200, otherwise return response with given response status and response body.
     */
    @GET
    @Path("/user-as-string/{userId}")
    public javax.ws.rs.core.Response singleUserAsString(@PathParam("userId") String userId) {
        Response<String> response = cVurl.get(HOST + USERS + userId)
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return ok(response.getBody(), APPLICATION_JSON).build();
        } else {
            return javax.ws.rs.core.Response.status(response.status()).entity(response.getBody()).build();
        }
    }

    /**
     * Make GET request to /users/{userId} to get single user with AcceptEncoding header set to gzip by calling
     * acceptCompressed method, get uncompressed response as string.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status is 200 return its body with status code 200, otherwise return response with given response status and response body.
     */
    @GET
    @Path("/user-as-string-compressed/{userId}")
    public javax.ws.rs.core.Response singleUserAsStringCompressed(@PathParam("userId") String userId) {
        Response<String> response = cVurl.get(HOST + USERS + userId)
                .acceptCompressed()
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return ok(response.getBody(), APPLICATION_JSON).build();
        } else {
            return javax.ws.rs.core.Response.status(response.status()).entity(response.getBody()).build();
        }
    }

    /**
     * Make GET request to /users/{userId} to get single user, get response as input stream.
     * If empty optional returned from asStream which means that error happened during request sending then throw RuntimeException.
     * If response status is 200 return its body with status code 200, otherwise return response with given response status and response body.
     */
    @GET
    @Path("/user-as-is/{userId}")
    public javax.ws.rs.core.Response singleUserAsInputStream(@PathParam("userId") String userId) throws IOException {
        Response<InputStream> response = cVurl.get(HOST + USERS + userId)
                .asStream()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return ok(response.getBody().readAllBytes(), APPLICATION_JSON).build();
        } else {
            return javax.ws.rs.core.Response.status(response.status()).entity(response.getBody().readAllBytes()).build();
        }
    }

    /**
     * Make POST request to /users to create user with request body parsed from {@link UserDto} object
     * with header Content-type = application/json, parse response body to {@link User} if response status code is CREATED,
     * otherwise empty Optional will be returned which means that some other response status code is arrived.
     */
    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Path("/users")
    public User createUserFromJson(UserDto userDto) {
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
    @POST
    @Consumes(APPLICATION_FORM_URLENCODED)
    @Produces(APPLICATION_JSON)
    @Path("/users")
    public User createUserFromFormUrlencoded(Map<String, String> userMap) {
        return cVurl.post(HOST + USERS)
                .formData(userMap)
                .asObject(User.class, HttpStatus.CREATED)
                .orElseThrow(() -> new RuntimeException("User can't be created"));
    }


    /**
     * Make PUT request to /users/{userId} to update user with userId equals to provided userId using request body parsed from {@link UserDto} object
     * with header Content-type = application/json, parse response body to {@link User}.
     */
    @PUT
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Path("/users/{userId}")
    public User updateUser(UserDto userDto, @PathParam("userId") String userId) {
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
    @DELETE
    @Path("/{userId}")
    public javax.ws.rs.core.Response deleteUser(@PathParam("userId") String userId) {
        Response<String> response = cVurl.delete(HOST + USERS + "/" + userId)
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.NO_CONTENT) {
            return javax.ws.rs.core.Response.noContent().build();
        } else {
            return javax.ws.rs.core.Response
                    .status(HttpStatus.BAD_REQUEST)
                    .entity(response.getBody())
                    .build();
        }
    }

    /**
     * Make POST request to /photos with body of content type multipart/form-data which consists of the given
     * photo file and title. Saves photo identified by title in in-memory database on the server.
     */
    @POST
    @Consumes(MULTIPART_FORM_DATA)
    @Path("/photos")
    public javax.ws.rs.core.Response uploadPhoto(MultipartFormDataInput multipartInput) throws IOException {
        Map<String, List<InputPart>> formDataMap = multipartInput.getFormDataMap();

        String title = formDataMap.get("title").get(0).getBodyAsString();
        InputPart photoInputPart = formDataMap.get("photo").get(0);
        byte[] content = photoInputPart.getBody(new GenericType<>(byte[].class));
        String contentType = photoInputPart.getMediaType().toString();

        Response<String> response = cVurl.post(HOST + PHOTOS)
                .body(MultipartBody.create()
                        .formPart("title", Part.of(title))
                        .formPart("photo", Part.of(title, contentType, content)))
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        return javax.ws.rs.core.Response.status(response.status()).build();
    }

    /**
     * Create temporary file, and make GET request to /photos/{title} with BodyHandlers.ofFile writing
     * response content to created file. Returns response with code NOT_FOUND if no photo was found for
     * provided title or content of temporary file with content type from response. Delete temporary file.
     */
    @GET
    @Path("/photos/{title}")
    public javax.ws.rs.core.Response getPhoto(@PathParam("title") String title) throws IOException {
        java.nio.file.Path path = Paths.get("temp-photo");
        Files.deleteIfExists(path);
        Files.createFile(path);
        Response<java.nio.file.Path> response = cVurl.get(HOST + PHOTOS + title)
                .as(HttpResponse.BodyHandlers.ofFile(path))
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.NOT_FOUND) {
            return javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
        }
        String mediaType = response.getHeaderValue(HttpHeader.CONTENT_TYPE).orElseThrow(IllegalStateException::new);
        javax.ws.rs.core.Response responseEntity = ok(Files.readAllBytes(path), mediaType).build();

        Files.delete(path);
        return responseEntity;
    }

    /**
     * Makes GET request to /users/list endpoint that return json array of objects.
     * Parses it to List<User> by using BodyType.
     *
     * @return List of users
     */
    @GET
    @Path("/users/list")
    @Produces(APPLICATION_JSON)
    public List<User> getUsersAsList() {
        return cVurl.get(Url.of(HOST).path(USERS).path("list").create())
                .asObject(new BodyType<>() {
                });
    }


}