package github.repos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import github.pojos.PullRequest;
import github.util.GlobalDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static github.util.ApiUtil.*;
import static github.payloads.ApiPayloads.*;
import static github.steps.PullRequestSteps.*;

@Slf4j (topic = "Poker Pull Requests")
public class Pulls extends Base {

    @Test
    public void getPullsByState(){
        Map<String,String> params = new HashMap<>();
        params.put("state","closed");
        setQueryParam(params);
        getListOfPullRequests();
        log.info("list all closed pull requests");
        extractAsArrayNode("").forEach(each-> assertThat(each.get("state").asText(),equalTo(params.get("state"))));
    }

    @Test
    public void getPullsByHead(){
        Map<String,String> params = new HashMap<>();
        params.put("state","all");
        params.put("head","B20-Project:rerunAndScreenshot");
        setQueryParam(params);
        getListOfPullRequests();
        extractAsJsonNode("head").forEach(each->assertThat(each.get("label").asText(),equalTo(params.get("head"))));
    }

    @Test
    public void getPullsAndSort(){
        Map<String,String> params = new HashMap<>();
        params.put("state","all");
        params.put("sort","created");
        params.put("direction","desc");
        setQueryParam(params);
        getListOfPullRequests();
        List<String> created_at = StreamSupport.stream(extractAsJsonNode().spliterator(),false)
                                    .map(each->each.get("created_at").asText()).collect(Collectors.toList());
        Comparator comparator;
        if (params.get("direction").equals("desc")) comparator = Comparator.reverseOrder();
        else comparator = Comparator.naturalOrder();
        assertThat(created_at.stream().sorted(comparator).collect(Collectors.toList()).equals(created_at),is(true));
    }

    @Test
    public void createNewPullRequest(){
        getListOfPullRequests();
        List<Object> heads= extractList("head.ref");
        if (heads.contains("calendar")){
            System.out.println("pull request already exists");
        }else{
            PullRequest payload = PullRequestPayLoad("calendar","selectMultiplePeople",false);
            createPullRequest(payload);

            JsonNode response = extractAsJsonNode();
            assertThat(response.get("title").asText(),equalTo(payload.getTitle()));
            assertThat(response.get("head").get("ref").asText(),equalTo(payload.getHead()));
            assertThat(response.get("base").get("ref").asText(),equalTo(payload.getBase()));
            assertThat(response.get("body").asText(),equalTo(payload.getBody()));
            assertThat(response.get("draft").asBoolean(),is(payload.isDraft()));
        }
    }

    @Test
    public void getAPullRequest() throws JsonProcessingException {
        getPullRequestByNumber(33);
        getResponse().assertThat()
                .body("number",is(33));
    }

    @Test
    public void verifyPullReqBody(){
        getPullRequestByNumber(33);
        getResponse().assertThat()
                .body("number",is(33))
                .body("title",equalTo("this is from java RestAssured2"))
                .body("body",equalTo("merging calendar into selectMultiplePeople"))
                .body("mergeable",is(true))
                .body("commits",is(28));
    }

    @Test
    public void verifyPullReqHead(){
        getPullRequestByNumber(33);
        JsonNode head = extractAsJsonNode("head");
        JsonNode user = head.get("user");
        JsonNode repo = head.get("repo");
        assertThat(head.get("ref").asText(),equalTo("calendar"));
        assertThat(user.get("login").asText(),equalTo("B20-Project"));
        assertThat(repo.get("name").asText(),equalTo("Poker"));
        assertThat(repo.get("private").asBoolean(),is(false));
        assertThat(repo.get("language").asText(),equalTo("Java"));
        assertThat(repo.get("open_issues").asInt(),is(6));

        JsonNode owner = repo.get("owner");
        assertThat(owner.get("login").asText(),equalTo("B20-Project"));
    }

    @Test
    public void verifyPullReqBase(){
        getPullRequestByNumber(33);
        JsonNode base = extractAsJsonNode("base");
        JsonNode user = base.get("user");
        JsonNode repo = base.get("repo");

        assertThat(base.get("ref").asText(),equalTo("selectMultiplePeople"));
        assertThat(user.get("login").asText(),equalTo("B20-Project"));
        assertThat(repo.get("name").asText(),equalTo("Poker"));
        assertThat(repo.get("private").asBoolean(),is(false));
        assertThat(repo.get("language").asText(),equalTo("Java"));
        assertThat(repo.get("open_issues").asInt(),is(6));

        JsonNode owner = repo.get("owner");
        assertThat(owner.get("login").asText(),equalTo("B20-Project"));
    }

    @Test
    public void closePullRequest(){
        Map<String,String> params = new HashMap<>();
        params.put("state","open");
        setQueryParam(params);
        getListOfPullRequests();
        int pull_num = extractAsArrayNode("").get(0).get("number").asInt();

        resetRequestSpec();
        JSONObject payload = new JSONObject();
        payload.put("state","closed");
        updatePullRequest(pull_num,payload);
        getResponse().assertThat()
                .body("state",equalTo("closed"));
    }

    @Test
    public void openPullRequest(){
        Map<String,String> params = new HashMap<>();
        params.put("state","closed");
        setQueryParam(params);

        getListOfPullRequests();
        int pull_num = extractAsArrayNode("").get(0).get("number").asInt();

        resetRequestSpec();
        JSONObject payload = new JSONObject();
        payload.put("state","open");
        updatePullRequest(pull_num,payload);
        getResponse().assertThat()
                .body("state",equalTo("open"));
    }

    @Test
    public void changePullReqBaseBranch(){
        String payload = "{\n" +
                "\"base\": "+"\"selectMultiplePeople\"\n"+
                "}";
        updatePullRequest(33,payload);
        assertThat(extractAsJsonNode("base").get("ref").asText(),equalTo("selectMultiplePeople"));
    }

    @Test
    public void verifyFilesOnPullReq(){
        retrievePullRequestFiles(27);
        ArrayNode files = extractAsArrayNode("");
        List<String> fileNames =
                Arrays.asList("src/test/java/com/Poker/pages/ActivityStreamPage.java",
                              "src/test/java/com/Poker/runners/FailedRun.java",
                              "src/test/java/com/Poker/step_definitions/TaskStepDefinition.java");

        StreamSupport.stream(files.spliterator(),false).map(file->file.get("filename").asText())
                .collect(Collectors.toList()).stream().forEach(name-> assertThat(fileNames,hasItem(name)));
    }

    @Test
    public void mergeGivenPullRequest(){
        if (checkIfPullRequestIsMerged(34)){
            mergePullRequest();
            getResponse().assertThat()
                    .body("merged",is(true))
                    .body("message",equalTo("Pull Request successfully merged"));
        }else{
            System.out.println("pick a new pull request");
        }
    }

    @Test
    public void createPullReqReviewWithComment(){
        createPullRequestReviewWithComment(33,"src/test/java/com/Poker/runners/CucumberRunner.java",5);
        getResponse().assertThat()
                .body("body",equalTo(GlobalDataUtil.get().getPR_Review_body()))
                .body("state",equalTo("PENDING"));
        deletePullRequestReviewByID();

    }

    @Test
    public void getPullReqReview(){
        createPullRequestReviewNoComment(33);
        int review_id = getResponse().extract().response().path("id");
        retrievePullRequestReviewByID(200);
        getResponse().assertThat()
                .body("id",is(review_id));
        deletePullRequestReviewByID();
    }

    @Test
    public void deletePullReqReview(){
        createPullRequestReviewNoComment(33);
        deletePullRequestReviewByID();
        retrievePullRequestReviewByID(404);
    }

    @Test
    public void getCommentsPullReqReview(){
        createPullRequestReviewWithComment(33,"src/test/java/com/Poker/step_definitions/TaskStepDefinition.java",30);
        retrievePullRequestReviewComment();
        getResponse().assertThat()
                .body("[0].path",equalTo(globalDataUtil.getPR_Review_Path()))
                .body("[0].body",equalTo(globalDataUtil.getPR_Review_Comment_Body()));
        deletePullRequestReviewByID();
    }

    @Test
    @Ignore
    public void submitPullReqReview(){
        createPullRequestReviewNoComment(33);
        submitPullRequestReview();
        getResponse().assertThat()
                .body("state",equalTo(globalDataUtil.getPR_Review_State()))
                .body("submitted_at",is(notNullValue()));
    }

    @Test
    public void RepoReviewCommentsSort(){
        Map<String,String> params = new HashMap<>();
        params.put("sort","created");
        params.put("direction","asc");
        setQueryParam(params);
        getRepoReviewComments();

        Comparator comparator = null;
        if (params.get("direction").equals("desc")) comparator = Comparator.reverseOrder();
        else comparator = Comparator.naturalOrder();

        List<String> created_at = getResponse().extract().jsonPath().getList("created_at",String.class);
        assertThat(created_at.stream().sorted(comparator).collect(Collectors.toList()).equals(created_at),is(true));
    }

    @Test
    public void RepoReviewCommentsDate(){
        Map<String,String> params = new HashMap<>();
        params.put("sort","created");
        params.put("direction","asc");
        params.put("since","2021-01-01T00:00:00Z");
        setQueryParam(params);
        getRepoReviewComments();

        extractAsArrayNode("").forEach(comment->
                assertThat(DateTime.parse(comment.get("created_at").asText()),greaterThan(DateTime.parse(params.get("since")))));
    }

    @Test
    public void getReviewCommentPR(){
        createPullRequestReviewWithComment(33,"src/test/java/com/Poker/step_definitions/TaskStepDefinition.java",35);
        get("/repos/{owner}/{repo}/pulls/comments/593829881").assertThat()
                .body("id",is(593829881));
    }

    @Test
    public void updateReviewCommentPR(){
        String payload = "{\n" +
                         "\"body\": "+ "\"this is a review comment update from java\"" +
                         "}";
        patch("/repos/{owner}/{repo}/pulls/comments/593829881",payload).assertThat()
                .body("body",equalTo("this is a review comment update from java"));
    }

    @Test
    public void deleteReviewCommentPR(){
        delete("/repos/{owner}/{repo}/pulls/comments/593811198",204);
    }

    @Test
    public void listReviewCommentsPRSort(){
        Map<String,String> params = new HashMap<>();
        params.put("sort","created");
        params.put("direction","asc");
        params.put("since","2021-01-01T00:00:00Z");
        setQueryParam(params);
        get("/repos/{owner}/{repo}/pulls/33/comments");

        Comparator comparator = null;
        if (params.get("direction").equals("desc")) comparator = Comparator.reverseOrder();
        else comparator = Comparator.naturalOrder();

        List<String> created_at = getResponse().extract().jsonPath().getList("created_at",String.class);
        assertThat(created_at.stream().sorted(comparator).collect(Collectors.toList()).equals(created_at),is(true));
    }

    @Test
    public void listReviewCommentsPRDate(){
        Map<String,String> params = new HashMap<>();
        params.put("sort","created");
        params.put("direction","asc");
        params.put("since","2021-01-01T00:00:00Z");
        setQueryParam(params);
        get("/repos/{owner}/{repo}/pulls/33/comments");
        extractAsArrayNode("").forEach(comment->
                assertThat(DateTime.parse(comment.get("created_at").asText()),greaterThan(DateTime.parse(params.get("since")))));
    }

    @Test
    public void createReviewCommentPR(){
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode payload = new ObjectNode(factory);
        payload.put("body","this body is generated from jsonNodeFactory");
        payload.put("commit_id","d20f5cb8957b417945a140381090d22f81cf9c63");
        payload.put("path","src/test/resources/features/Task.feature");
        payload.put("position",30);
        post("/repos/{owner}/{repo}/pulls/33/comments",payload,201).assertThat()
                .body("path",equalTo(payload.get("path").asText()))
                .body("body",equalTo(payload.get("body").asText()))
                .body("commit_id",equalTo(payload.get("commit_id").asText()))
                .body("position",is(payload.get("position").asInt()));
    }

    @Test
    public void replyToReviewComment(){
        String payload = "{\n" +
                         "\"body\": "+ "\"this is a reply\"" +
                         "\n}";
        post("/repos/{owner}/{repo}/pulls/33/comments/593958178/replies",payload,201).assertThat()
                .body("body",equalTo("this is a reply"));
    }

    @Test
    public void requestReviewersPR(){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        ArrayNode reviewers = payload.putArray("reviewers");
        reviewers.add("elv-krgb");

        post("/repos/{owner}/{repo}/pulls/33/requested_reviewers",payload,201);
        extractAsArrayNode("requested_reviewers").forEach(reviewer->{
            assertThat(reviewer.get("login").asText(),equalTo("elv-krgb"));
        });
    }

    @Test
    public void deleteReviewerPR(){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        ArrayNode reviewers = payload.putArray("reviewers");
        reviewers.add("elv-krgb");
        delete("/repos/{owner}/{repo}/pulls/33/requested_reviewers",payload,200);

        get("/repos/{owner}/{repo}/pulls/33/requested_reviewers");
        extractAsArrayNode("users").forEach(user-> assertThat(user.get("login").asText(),not(equalTo("elv-krgb"))));
    }

}
