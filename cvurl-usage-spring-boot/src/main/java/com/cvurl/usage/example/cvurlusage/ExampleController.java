package com.cvurl.usage.example.cvurlusage;

import com.cvurl.usage.example.cvurlusage.model.*;
import coresearch.cvurl.io.constant.HttpHeader;
import coresearch.cvurl.io.constant.HttpStatus;
import coresearch.cvurl.io.constant.MIMEType;
import coresearch.cvurl.io.exception.UnexpectedResponseException;
import coresearch.cvurl.io.model.Response;
import coresearch.cvurl.io.request.CVurl;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
public class ExampleController {

    private static final String HOST = "https://reqres.in/api/";
    private static final String USERS = "users";
    private static final int NON_EXISTENT_USER_ID = 23;

    private static final Logger logger = LoggerFactory.getLogger(ExampleController.class);

    private final CVurl cVurl;

    public ExampleController(CVurl cVurl) {
        this.cVurl = cVurl;
    }

    @ExceptionHandler(UnexpectedResponseException.class)
    public ResponseEntity handleException(UnexpectedResponseException exception) {
        var response = exception.getResponse();

        return ResponseEntity
                .status(response.status())
                .body(response.getBody());
    }

    /**
     * Make GET request to https://reqres.in/api/users to get list of users with query param page (result is paginated) equals to passed parameter
     * page or 1 if none passed. Parse returned response body into {@link GetUsersDto} if response status is OK.
     */
    @GetMapping("/users")
    public GetUsersDto listUsers(@RequestParam(required = false) Integer page) {
        return cVurl.GET(HOST + USERS)
                .queryParam("page", Objects.requireNonNullElse(page, 1).toString())
                .build()
                .asObject(GetUsersDto.class, HttpStatus.OK);
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user, parse response body to {@link GetUsersDto}
     * if response status is ok.
     */
    @GetMapping("/users/{userId}")
    public GetUserDto singleUser(@PathVariable Integer userId) {
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
    @GetMapping("/user-not-found")
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
    @GetMapping("/user-as-string/{userId}")
    public ResponseEntity<String> singleUserAsString(@PathVariable String userId) {
        Response<String> response = cVurl.GET(HOST + USERS + "/" + userId)
                .build()
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response.getBody());
        } else {
            return ResponseEntity.status(response.status()).body(response.getBody());
        }
    }

    /**
     * Make GET request to https://reqres.in/api/users/{userId} to get single user, map response body to {@link JSONObject}.
     * If empty optional returned from asString which means that error happened during request sending then throw RuntimeException.
     * If response status is OK return json object parsed to string with status code OK, otherwise return response with given response status and response body
     * parsed to string.
     */
    @GetMapping("/user-as-json/{userId}")
    public ResponseEntity<String> singleUserAsJson(@PathVariable String userId) {
        Response<JSONObject> response = cVurl.GET(HOST + USERS + "/" + userId)
                .build()
                .map(JSONObject::new)
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(response.getBody().toString());
        } else {
            return ResponseEntity.status(response.status()).body(response.getBody().toString());
        }
    }


    /**
     * Make POST request to https://reqres.in/api/users to create user with request body parsed from {@link UserDto} object
     * with header Content-type = application-json, parse response body to {@link UserCreatedResponseDto} if response status code is CREATED.
     */
    @PostMapping("/users")
    public UserCreatedResponseDto createUser(@RequestBody UserDto userDto) {
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
    @PutMapping("/users/{userId}")
    public UserUpdatedResponseDto updateUser(@RequestBody UserDto userDto, @PathVariable String userId) {
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
    @DeleteMapping("/users/{userId}")
    public ResponseEntity deleteUser(@RequestBody UserDto userDto, @PathVariable String userId) {
        Response<String> response = cVurl.DELETE(HOST + USERS + "/" + userId)
                .build()
                .asString()
                .orElseThrow(() -> new RuntimeException("Some error happened during request execution"));

        if (response.status() == HttpStatus.NO_CONTENT) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getBody());
        }
    }
}
