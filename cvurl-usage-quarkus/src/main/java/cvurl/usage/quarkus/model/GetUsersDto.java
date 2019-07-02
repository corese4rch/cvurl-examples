package cvurl.usage.quarkus.model;

import lombok.Data;

import javax.json.bind.annotation.JsonbProperty;
import java.util.List;

@Data
public class GetUsersDto {
    private Integer page;
    private Integer total;
    private List<User> data;

    @JsonbProperty("total_pages")
    private Integer totalPages;

    @JsonbProperty("per_page")
    private Integer perPage;
}
