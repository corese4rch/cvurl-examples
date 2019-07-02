package com.cvurl.usage.example.cvurlusage.model;

import lombok.Data;

@Data
public class UserCreatedResponseDto {
    private String name;
    private String job;
    private Integer id;
    private String createdAt;
}
