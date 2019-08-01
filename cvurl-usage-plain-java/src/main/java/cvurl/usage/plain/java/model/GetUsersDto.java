package cvurl.usage.plain.java.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetUsersDto {
    private Integer page;
    private Integer total;
    private List<User> data;
    private Integer totalPages;
    private Integer perPage;
}
