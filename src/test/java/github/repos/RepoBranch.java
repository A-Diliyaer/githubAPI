package github.repos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import github.pojos.BranchProtection;
import github.payloads.ApiPayloads;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static github.util.ApiUtil.*;

public class RepoBranch extends Base{

    @DisplayName("GET /{repo}/branches")
    @Test
    public void getRepoBranches(){
        JsonPath jp = get("/repos/{owner}/{repo}/branches").extract().jsonPath();

        List<String> branches = jp.getList("name");
    }

    @DisplayName("GET /{repo}/branches protected")
    @Test
    public void getRepoProtectedBranches(){
        List<String> protectedBranches = get("/repos/{owner}/{repo}/branches")
                .extract().jsonPath().getList("findAll{it.protected==true}.name");

        assertThat(protectedBranches.size(),is(2));
        assertThat(protectedBranches,hasItems("calendar","master"));
    }

    @DisplayName("GET /{repo}/branches/{branch}")
    @Test
    public void getBranchByName(){
        setBranchReqSpec();
        Response response  = get("/repos/{owner}/{repo}/branches/{branch}").extract().response();

        assertThat(response.path("name"),equalTo("calendar"));
        ObjectMapper mapper = new ObjectMapper();
        JSONObject authorObj = new JSONObject();
        authorObj.put("name","Deenen");
        authorObj.put("email","68973633+Deenen@users.noreply.github.com");
        authorObj.put("date","2020-11-04T16:29:23Z");

        JsonNode author = mapper.valueToTree(response.path("commit.commit.author"));
        JsonNode expectedAuthor = mapper.valueToTree(authorObj);
        assertThat(author,equalTo(expectedAuthor));
    }

    @DisplayName("GET /{repo}/branches/{branch}/protection")
    @Test
    public void getBranchProtection(){
        setBranchReqSpec();
        get("/repos/{owner}/{repo}/branches/{branch}/protection").body(not(empty()));
    }

    @DisplayName("PUT /{branch}/protection")
    @Test
    public void updateBranchProtection(){
        BranchProtection bp = ApiPayloads.createBranchProtection();

        setBranchReqSpec();
        JsonNode node = put("/repos/{owner}/{repo}/branches/{branch}/protection",bp)
                .extract().response().body().as(JsonNode.class);

        assertThat(node.get("required_status_checks").get("strict").asBoolean(),is(bp.getRequired_status_checks().get("strict").booleanValue()));
        assertThat(node.get("required_status_checks").get("contexts"),equalTo(bp.getRequired_status_checks().get("contexts")));
        assertThat(node.get("enforce_admins").get("enabled").asBoolean(),is(bp.isEnforce_admins()));
        assertThat(node.get("required_linear_history").asBoolean(),is(bp.isRequired_linear_history()));
        assertThat(node.get("allow_force_pushes").get("enabled").asBoolean(),is(bp.isAllow_force_pushes()));
        assertThat(node.get("allow_deletions").get("enabled").asBoolean(),is(bp.isAllow_deletions()));
    }

    @DisplayName("PUT /{branch}/protection")
    @Test
    public void enableUserRestrictionNotAuthorized(){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode status_check = mapper.createObjectNode();
        status_check.put("strict",true);
        ArrayNode arrayNode = status_check.putArray("contexts");
        arrayNode.add("success");
        arrayNode.add("pending");
        ObjectNode restrictions = mapper.createObjectNode();
        ArrayNode users = restrictions.putArray("users");
        ArrayNode teams = restrictions.putArray("teams");
        users.add("Deenen");
        users.add("A-Diliyaer");

        BranchProtection bp = new BranchProtection
                (null,true,
                        null,
                        restrictions,
                        false,
                        true,
                        true);

        setBranchReqSpec();
        put("/repos/{owner}/{repo}/branches/{branch}/protection",bp,422)
                .body("message",equalTo("Validation Failed"))
                .body("errors[0]",equalTo("Only organization repositories can have users and team restrictions"));
    }

    @DisplayName("GET /branch/protection/enforce_admins")
    @Test
    public void deleteBranchEnforceAdmin(){
        setBranchReqSpec();
        post("/repos/{owner}/{repo}/branches/{branch}/protection/enforce_admins")
                .body("enabled",is(true));

        delete("/repos/{owner}/{repo}/branches/{branch}/protection/enforce_admins",204);

        get("/repos/{owner}/{repo}/branches/{branch}/protection/enforce_admins")
                .body("enabled",is(false));
    }

    @DisplayName("GET /branch/protection/required_pull_request_reviews")
    @Test
    public void getPullRequestReviewProtection(){
        setBranchReqSpec();
        get("/repos/{owner}/{repo}/branches/{branch}/protection/required_pull_request_reviews");
    }

    @DisplayName("PATCH /branch/protection/required_pull_request_reviews")
    @Test
    public void updatePullRequestReviewProtection(){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("dismiss_stale_reviews",true);
        node.put("require_code_owner_reviews",true);
        node.put("required_approving_review_count",3);

        setBranchReqSpec();
        patch("/repos/{owner}/{repo}/branches/{branch}/protection/required_pull_request_reviews",node)
                .body("dismiss_stale_reviews",is(node.get("dismiss_stale_reviews").asBoolean()));
    }

    @DisplayName("PATCH /branch/protection/required_pull_request_reviews 422")
    @Test
    public void updatePullRequestReviewWithDimissal(){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        ObjectNode dismissal_resttrict = mapper.createObjectNode();
        ArrayNode users = dismissal_resttrict.putArray("users");
        ArrayNode teams = dismissal_resttrict.putArray("teams");
        node.set("dismissal_restrictions",dismissal_resttrict);
        node.put("dismiss_stale_reviews",true);
        node.put("require_code_owner_reviews",true);
        node.put("required_approving_review_count",3);

        setBranchReqSpec();
        patch("/repos/{owner}/{repo}/branches/{branch}/protection/required_pull_request_reviews",node,422)
                .body("message",equalTo("Dismissal restrictions are supported only for repositories owned by an organization."));
    }

    @DisplayName("GET /{branch}/protection/required_status_checks")
    @Test
    public void getStatusCheckProtection(){
        setBranchReqSpec();
        get("/repos/{owner}/{repo}/branches/{branch}/protection/required_status_checks")
                .body("contexts",hasItems("success","pending"));
    }

    @DisplayName("PATCH /{branch}/protection/required_status_checks")
    @Test
    public void updateStatusCheckProtection() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        List<String> lst = Arrays.asList("continous integration", "actions", "commits");
        node.put("strict", true);
        ArrayNode context = node.putArray("contexts");
        lst.forEach(context::add);

        setBranchReqSpec();
        List<String> actual = patch("/repos/{owner}/{repo}/branches/{branch}/protection/required_status_checks",node)
                .extract().response().jsonPath().getList("contexts");

        actual.forEach(each-> assertThat(lst,hasItem(each)));
    }

    @DisplayName("DELETE /{branch}/protection/required_status_checks")
    @Test
    public void removeStatusCheckProtection(){
        setBranchReqSpec();
        delete("/repos/{owner}/{repo}/branches/{branch}/protection/required_status_checks",204);
    }

    @DisplayName("DELETE /{branch}/protection/required_status_checks 404")
    @Test
    public void removeStatusCheckProtectionDisabled(){
        setBranchReqSpec();
        delete("/repos/{owner}/{repo}/branches/{branch}/protection/required_status_checks",404)
                .body("message",equalTo("Required status checks not enabled"));
    }

    @DisplayName("GET /{branch}/protection/required_status_checks")
    @Test
    public void getAllStatusCheckContexts(){
        setBranchReqSpec();
        get("/repos/{owner}/{repo}/branches/{branch}/protection/required_status_checks/contexts")
                .body("",hasItems("continuous integration","actions","commits"));
    }

    @DisplayName("POST /{branch}/protection/required_status_checks/contexts")
    @Test
    public void addStatusCheckContexts(){
        String[] contexts = {"continuous integration","actions","commits","build"};
        setBranchReqSpec();
        String[] actual = post("/repos/{owner}/{repo}/branches/{branch}/protection/required_status_checks/contexts",contexts)
                .extract().response().as(String[].class);

        Arrays.stream(contexts).forEach(each->assertThat(contexts,hasItemInArray(each)));
    }

    @DisplayName("POST /{branch}/protection/required_status_checks/contexts")
    @Test
    public void addExistingStatusChecks(){
        String[] contexts = {"continuous integration","actions"};
        setBranchReqSpec();
        post("/repos/{owner}/{repo}/branches/{branch}/protection/required_status_checks/contexts",contexts,422)
                .body("errors[0].code",equalTo("already_exists"));
    }

    @DisplayName("DELETE /{branch}/protection/required_status_checks/contexts")
    @Test
    public void removeStatusCheckContext(){
        String[] arr = {"actions","build"};
        setBranchReqSpec();
        Response response = delete("/repos/{owner}/{repo}/branches/{branch}/protection/required_status_checks/contexts",arr)
                .extract().response();

        Arrays.stream(response.as(String[].class)).forEach(each->assertThat(arr,not(hasItemInArray(each))));
    }

    @DisplayName("GET /{branch}/protection/restrictions/users")
    @Test
    public void getPushRestrictionsNotEnabled(){
        setBranchReqSpec();
        get("/repos/{owner}/{repo}/branches/{branch}/protection/restrictions/users")
                .body("message",equalTo("Push restrictions not enabled"));
    }

    @DisplayName("POST /{branch}/protection/restrictions/users")
    @Test
    public void addUserAccessFailed(){
        String[] users = {"Deenen","B20-Project"};
        setBranchReqSpec();
        post("/repos/{owner}/{repo}/branches/{branch}/protection/restrictions/users",users,404)
                .body("message",equalTo("Push restrictions not enabled"));
    }

    @DisplayName("POST /{branch}/rename")
    @Test
    public void renameBranch(){
        JSONObject new_name = new JSONObject();
        new_name.put("new_name","calendar");
        setBranchReqSpec();
        post("/repos/{owner}/{repo}/branches/{branch}/rename",new_name,201)
                .body("name",equalTo("calendar"));
    }
}
