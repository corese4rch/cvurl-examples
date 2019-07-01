package com.cvurl.usage.example.cvurlusage.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class User {

    private Integer id;
    private String email;

    @JsonAlias("first_name")
    private String firstName;

    @JsonAlias("last_name")
    private String lastName;

    private String avatar;
}
