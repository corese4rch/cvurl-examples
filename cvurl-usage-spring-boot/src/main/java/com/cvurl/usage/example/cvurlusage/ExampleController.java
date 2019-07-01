package com.cvurl.usage.example.cvurlusage;

import com.cvurl.usage.example.cvurlusage.model.*;
import coresearch.cvurl.io.exception.UnexpectedResponseException;
import coresearch.cvurl.io.model.Response;
import coresearch.cvurl.io.request.CVurl;
import coresearch.cvurl.io.util.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity
                .status(400)
                .body(exception.getMessage());
    }

    @GetMapping("/users")
    public GetUsersDto listUsers(@RequestParam(required = false) Integer page) {
        return cVurl.GET(HOST + USERS)
                .queryParam("page", Objects.requireNonNullElse(page, 1).toString())
                .build()
                .asObject(GetUsersDto.class, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public GetUserDto singleUser(@PathVariable Integer userId) {
        return cVurl.GET(HOST + USERS + "/" + userId)
                .build()
                .asObject(GetUserDto.class, HttpStatus.OK);
    }

    @GetMapping("/user-not-found")
    public GetUserDto singleUserNotFound() {
        return cVurl.GET(HOST + USERS + "/" + NON_EXISTENT_USER_ID)
                .build()
                .asObject(GetUserDto.class, HttpStatus.OK);
    }

    @PostMapping("/users")
    public UserCreatedResponseDto createUser(@RequestBody UserDto userDto) {
        return cVurl.POST(HOST + USERS)
                .body(userDto)
                .build()
                .asObject(UserCreatedResponseDto.class, HttpStatus.CREATED);
    }

    @PutMapping("/users/{userId}")
    public UserUpdatedResponseDto updateUser(@RequestBody UserDto userDto, @PathVariable String userId) {
        return cVurl.PUT(HOST + USERS + "/" + userId)
                .body(userDto)
                .build()
                .asObject(UserUpdatedResponseDto.class, HttpStatus.OK);
    }


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
