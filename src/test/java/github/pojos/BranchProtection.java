package github.pojos;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BranchProtection {

    private ObjectNode required_status_checks;
    private boolean enforce_admins;
    private ObjectNode required_pull_request_reviews;
    private ObjectNode restrictions;
    private boolean required_linear_history;
    private boolean allow_force_pushes;
    private boolean allow_deletions;

}
