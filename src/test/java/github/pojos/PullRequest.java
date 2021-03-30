package github.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequest {

    private int id;
    private String title;
    private String head;
    private String base;
    private String body;
    private boolean draft;
    private String state;
    private String number;
    private User user;
}
