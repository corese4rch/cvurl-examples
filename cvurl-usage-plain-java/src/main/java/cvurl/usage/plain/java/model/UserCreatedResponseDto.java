package cvurl.usage.plain.java.model;

import lombok.Data;

@Data
public class UserCreatedResponseDto {
    private String name;
    private String job;
    private Integer id;
    private String createdAt;
}
