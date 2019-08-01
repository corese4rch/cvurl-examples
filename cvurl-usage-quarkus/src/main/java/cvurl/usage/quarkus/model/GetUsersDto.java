package cvurl.usage.quarkus.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetUsersDto {
    private Integer page;
    private Integer total;
    private List<User> data;
    private Integer totalPages;
    private Integer perPage;
}
