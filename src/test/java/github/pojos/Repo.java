package github.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repo {
    private String name;
    private String description;
    private String homepage;
    @JsonProperty("private")
    private boolean repoPrivate;
    private boolean has_issues;
    private boolean has_projects;
    private boolean has_wiki;
    private boolean is_template;
    private boolean allow_squash_merge;
    private boolean allow_merge_commit;
    private boolean allow_rebase_merge;
    private boolean delete_branch_on_merge;

}


