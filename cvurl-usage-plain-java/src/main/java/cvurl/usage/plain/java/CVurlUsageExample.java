package cvurl.usage.plain.java;

import coresearch.cvurl.io.constant.HttpHeader;
import coresearch.cvurl.io.constant.HttpStatus;
import coresearch.cvurl.io.constant.MIMEType;
import coresearch.cvurl.io.exception.UnexpectedResponseException;
import coresearch.cvurl.io.model.Response;
import coresearch.cvurl.io.request.CVurl;
import cvurl.usage.plain.java.model.*;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

public class CVurlUsageExample {

    private static final String HOST = "https://reqres.in/api/";
    private static final String USERS = "users";
    public static final int NON_EXISTENT_USER_ID = 23;

    private static final CVurl cVurl = new CVurl();

    /**
     * Make GET request to https://reqres.in/api/users to get list of users with query param page (result is paginated)
     * equals to passed parameterpage or 1 if none passed. Parse returned response body into {@link GetUsersDto}
     * if response status is OK.
     */
    public static GetUsersDto listUsers(Integer page) {
        return cVurl.GET(HOST + USERS)
                .queryParam("page", Objects.requireNonNullElse(page, 1).toString())
                .build()
                .asObject(GetUsersDto.class, HttpStatus.OK);
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user, parse response body
     * to {@link GetUsersDto} if response status is ok.
     */
    public static GetUserDto singleUser(Integer userId) {
        return cVurl.GET(HOST + USERS + "/" + userId)
                .build()
                .asObject(GetUserDto.class, HttpStatus.OK);
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user with id of not existent user.
     * It should throw {@link UnexpectedResponseException} as we declared that we expect response with status code
     * 200 but response with 400 will be returned.
     */
    public static GetUserDto singleUserNotFound() {
        return cVurl.GET(HOST + USERS + "/" + NON_EXISTENT_USER_ID)
                .build()
                .asObject(GetUserDto.class, HttpStatus.OK);
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user, get response as string.
     * If empty optional returned from asString which means that error happened during request sending then
     * throw RuntimeException. If response status is 200 return response body otherwise throw {@link RuntimeException}
     */
    public static String singleUserAsString(Integer userId) {
        Response<String> response = cVurl.GET(HOST + USERS + "/" + userId)
                .build()
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Bad response with status code " + response.status() +
                    " and body " + response.getBody());
        }
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user, map response body to {@link JSONObject}.
     * If empty optional returned from asString which means that error happened during request sending then throw
     * RuntimeException. If response status is 200 return response body otherwise throw {@link RuntimeException}
     */
    public static JSONObject singleUserAsJson(Integer userId) {
        Response<JSONObject> response = cVurl.GET(HOST + USERS + "/" + userId)
                .build()
                .map(JSONObject::new)
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Bad response with status code " + response.status() +
                    " and body " + response.getBody());
        }
    }


    /**
     * Make POST request to https://reqres.in/api/users to create user with request body parsed from {@link UserDto} object
     * with header Content-type = application-json, parse response body to {@link UserCreatedResponseDto} if response status code is CREATED.
     */
    public static UserCreatedResponseDto createUser(UserDto userDto) {
        return cVurl.POST(HOST + USERS)
                .body(userDto)
                .header(HttpHeader.CONTENT_TYPE, MIMEType.APPLICATION_JSON)
                .build()
                .asObject(UserCreatedResponseDto.class, HttpStatus.CREATED);
    }

    /**
     * Make POST request to https://reqres.in/api/users/{userId} to update user with userId equals to provided
     * userId using request body parsed from {@link UserDto} object with header Content-type = application-json,
     * parse response body to {@link UserCreatedResponseDto} if response status code is OK.
     */
    public static UserUpdatedResponseDto updateUser(UserDto userDto, Integer userId) {
        return cVurl.PUT(HOST + USERS + "/" + userId)
                .body(userDto)
                .headers(Map.of(HttpHeader.CONTENT_TYPE, MIMEType.APPLICATION_JSON))
                .build()
                .asObject(UserUpdatedResponseDto.class, HttpStatus.OK);
    }

    /**
     * Make DELETE request to https://reqres.in/api/users/{userId} to delete user with userId equals to provided userId.
     * If empty optional returned from asString which means that error happened during request sending then throw
     * RuntimeException.If response status equals NO_CONTENT then return true, else return false
     */
    public static boolean deleteUser(Integer userId) {
        Response<String> response = cVurl.DELETE(HOST + USERS + "/" + userId)
                .build()
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        return response.status() == HttpStatus.NO_CONTENT;
    }
}
