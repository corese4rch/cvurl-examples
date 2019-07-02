package cvurl.usage.quarkus.model;

import lombok.Data;

import javax.json.bind.annotation.JsonbProperty;

@Data
public class User {

    private Integer id;
    private String email;

    @JsonbProperty("first_name")
    private String firstName;

    @JsonbProperty("last_name")
    private String lastName;

    private String avatar;
}
