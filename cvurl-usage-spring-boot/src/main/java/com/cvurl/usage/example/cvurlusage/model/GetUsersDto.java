package com.cvurl.usage.example.cvurlusage.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class GetUsersDto {
    private Integer page;
    private Integer total;
    private List<User> data;

    @JsonAlias("total_pages")
    private Integer totalPages;

    @JsonAlias("per_page")
    private Integer perPage;
}
