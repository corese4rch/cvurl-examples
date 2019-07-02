package cvurl.usage.quarkus;

import coresearch.cvurl.io.exception.UnexpectedResponseException;
import coresearch.cvurl.io.model.Response;
import coresearch.cvurl.io.request.CVurl;
import coresearch.cvurl.io.util.HttpHeader;
import coresearch.cvurl.io.util.HttpStatus;
import coresearch.cvurl.io.util.MIMEType;
import cvurl.usage.quarkus.model.*;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/users")
public class ExampleResource {

    private static final String HOST = "https://reqres.in/api/";
    private static final String USERS = "users";
    private static final int NON_EXISTENT_USER_ID = 23;

    private final CVurl cVurl;

    @Inject
    public ExampleResource(CVurl cVurl) {
        this.cVurl = cVurl;
    }

    /**
     * Make GET request to https://reqres.in/api/users to get list of users with query param page (result is paginated) equals to passed parameter
     * page or 1 if none passed. Parse returned response body into {@link GetUsersDto} if response status is OK.
     */
    @GET
    @Produces(APPLICATION_JSON)
    public GetUsersDto listUsers(@DefaultValue("1") @QueryParam("page") Integer page) {
        return cVurl.GET(HOST + USERS)
                .queryParam("page", page.toString())
                .build()
                .asObject(GetUsersDto.class, HttpStatus.OK);
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user, parse response body to {@link GetUsersDto}
     * if response status is ok.
     */
    @GET
    @Path("/{userId}")
    @Produces(APPLICATION_JSON)
    public GetUserDto singleUser(@PathParam("userId") Integer userId) {
        return cVurl.GET(HOST + USERS + "/" + userId)
                .build()
                .asObject(GetUserDto.class, HttpStatus.OK);
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user with id of not existent user. It should
     * throw {@link UnexpectedResponseException} as we declared that we expect response with status code 200 but response
     * with 400 will be returned. Exception is handled by {@link UnexpectedResponseExceptionMapper}
     */
    @GET
    @Path("/not-found")
    @Produces(APPLICATION_JSON)
    public GetUserDto singleUserNotFound() {
        return cVurl.GET(HOST + USERS + "/" + NON_EXISTENT_USER_ID)
                .build()
                .asObject(GetUserDto.class, HttpStatus.OK);
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user, get response as string.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status is 200 return its body with status code 200, otherwise return response with given response status and response body.
     */
    @GET
    @Path("/as-string/{userId}")
    public javax.ws.rs.core.Response singleUserAsString(@PathParam("userId") String userId) {
        Response<String> response = cVurl.GET(HOST + USERS + "/" + userId)
                .build()
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return javax.ws.rs.core.Response
                    .ok(response.getBody(), MediaType.APPLICATION_JSON_TYPE)
                    .build();
        } else {
            return javax.ws.rs.core.Response
                    .status(response.status())
                    .entity(response.getBody())
                    .build();
        }
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user, map response body to {@link JSONObject}.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status is OK return json object parsed to string with status code OK, otherwise return response with given response status and response body
     * parsed to string.
     */
    @GET
    @Path("/as-json/{userId}")
    public javax.ws.rs.core.Response singleUserAsJson(@PathParam("userId") String userId) {
        Response<JSONObject> response = cVurl.GET(HOST + USERS + "/" + userId)
                .build()
                .map(JSONObject::new)
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return javax.ws.rs.core.Response
                    .ok(response.getBody().toString(), MediaType.APPLICATION_JSON_TYPE)
                    .build();
        } else {
            return javax.ws.rs.core.Response
                    .status(response.status())
                    .entity(response.getBody().toString())
                    .build();
        }
    }


    /**
     * Make POST request to https://reqres.in/api/users to create user with request body parsed from {@link UserDto} object
     * with header Content-type = application-json, parse response body to {@link UserCreatedResponseDto} if response status code is CREATED.
     */
    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public UserCreatedResponseDto createUser(UserDto userDto) {
        return cVurl.POST(HOST + USERS)
                .body(userDto)
                .header(HttpHeader.CONTENT_TYPE, MIMEType.APPLICATION_JSON)
                .build()
                .asObject(UserCreatedResponseDto.class, HttpStatus.CREATED);
    }

    /**
     * Make POST request to https://reqres.in/api/users/{userId} to update user with userId equals to provided userId using request body parsed from {@link UserDto} object
     * with header Content-type = application-json, parse response body to {@link UserCreatedResponseDto} if response status code is OK.
     */
    @PUT
    @Path("/{userId}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public UserUpdatedResponseDto updateUser(UserDto userDto, @PathParam("userId") String userId) {
        return cVurl.PUT(HOST + USERS + "/" + userId)
                .body(userDto)
                .headers(Map.of(HttpHeader.CONTENT_TYPE, MIMEType.APPLICATION_JSON))
                .build()
                .asObject(UserUpdatedResponseDto.class, HttpStatus.OK);
    }

    /**
     * Make DELETE request to https://reqres.in/api/users/{userId} to delete user with userId equals to provided userId.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status equals NO_CONTENT then return response with same content and return response with BAD_REQUEST status code
     * and body from given response.
     */
    @DELETE
    @Path("/{userId}")
    public javax.ws.rs.core.Response deleteUser(@PathParam("userId") String userId) {
        Response<String> response = cVurl.DELETE(HOST + USERS + "/" + userId)
                .build()
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


}