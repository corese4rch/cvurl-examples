package cvurl.usage.micronaut.model;

import lombok.Data;

@Data
public class UserUpdatedResponseDto {
    private String name;
    private String job;
    private Integer id;
    private String updatedAt;
}
