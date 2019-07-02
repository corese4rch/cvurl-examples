package cvurl.usage.micronaut;

import coresearch.cvurl.io.exception.UnexpectedResponseException;
import coresearch.cvurl.io.model.Response;
import coresearch.cvurl.io.request.CVurl;
import coresearch.cvurl.io.util.HttpHeader;
import coresearch.cvurl.io.util.HttpStatus;
import coresearch.cvurl.io.util.MIMEType;
import cvurl.usage.micronaut.model.*;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;

@Controller
public class ExampleController {

    private static final String HOST = "https://reqres.in/api/";
    private static final String USERS = "users";
    private static final int NON_EXISTENT_USER_ID = 23;

    private final CVurl cVurl;

    public ExampleController(CVurl cVurl) {
        this.cVurl = cVurl;
    }

    @Error(UnexpectedResponseException.class)
    public HttpResponse handleException(UnexpectedResponseException exception) {
        return HttpResponse
                .badRequest(exception.getMessage());
    }

    /**
     * Make GET request to https://reqres.in/api/users to get list of users with query param page (result is paginated) equals to passed parameter
     * page or 1 if none passed. Parse returned response body into {@link GetUsersDto} if response status is OK.
     */
    @Get("/users")
    public GetUsersDto listUsers(@QueryValue Optional<Integer> page) {
            return cVurl.GET(HOST + USERS)
                .queryParam("page", page.orElse(1).toString())
                .build()
                .asObject(GetUsersDto.class, HttpStatus.OK);
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user, parse response body to {@link GetUsersDto}
     * if response status is ok.
     */
    @Get("/users/{userId}")
    public GetUserDto singleUser(@QueryValue Integer userId) {
        return cVurl.GET(HOST + USERS + "/" + userId)
                .build()
                .asObject(GetUserDto.class, HttpStatus.OK);
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user with id of not existent user. It should
     * throw {@link UnexpectedResponseException} as we declared that we expect response with status code 200 but response
     * with 400 will be returned. Exception is handled by {@link #handleException(UnexpectedResponseException)} method
     * defined above.
     */
    @Get("/user-not-found")
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
    @Get("/user-as-string/{userId}")
    public HttpResponse singleUserAsString(@PathVariable String userId) {
        Response<String> response = cVurl.GET(HOST + USERS + "/" + userId)
                .build()
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return HttpResponse
                    .ok(response.getBody())
                    .contentType(MediaType.APPLICATION_JSON_TYPE);
        } else {
            return HttpResponse
                    .status(io.micronaut.http.HttpStatus.valueOf(response.status()))
                    .body(response.getBody());
        }
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user, map response body to {@link JSONObject}.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status is OK return json object parsed to string with status code OK, otherwise return response with given response status and response body
     * parsed to string.
     */
    @Get("/user-as-json/{userId}")
    public HttpResponse singleUserAsJson(@PathVariable String userId) {
        Response<JSONObject> response = cVurl.GET(HOST + USERS + "/" + userId)
                .build()
                .map(JSONObject::new)
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return HttpResponse
                    .ok(response.getBody().toString())
                    .contentType(MediaType.APPLICATION_JSON_TYPE);
        } else {
            return HttpResponse
                    .status(io.micronaut.http.HttpStatus.valueOf(response.status()))
                    .body(response.getBody().toString());
        }
    }


    /**
     * Make POST request to https://reqres.in/api/users to create user with request body parsed from {@link UserDto} object
     * with header Content-type = application-json, parse response body to {@link UserCreatedResponseDto} if response status code is CREATED.
     */
    @Post("/users")
    public UserCreatedResponseDto createUser(@Body UserDto userDto) {
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
    @Put("/users/{userId}")
    public UserUpdatedResponseDto updateUser(@Body UserDto userDto, @PathVariable String userId) {
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
    @Delete("/users/{userId}")
    public HttpResponse deleteUser(@PathVariable String userId) {
        Response<String> response = cVurl.DELETE(HOST + USERS + "/" + userId)
                .build()
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.NO_CONTENT) {
            return HttpResponse.noContent();
        } else {
            return HttpResponse.badRequest(response.getBody());
        }
    }
}
