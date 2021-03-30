package github.repos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.joda.time.DateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static github.util.ApiUtil.*;

public class Commits extends Base{

    @DisplayName("GET /{repo}/commits")
    @Test
    public void getCommitsByAuthorAndPath() {
        Map<String,String> params = new HashMap<>();
        params.put("path","src/test/java/com/Poker/pages/AbstractPageBase.java");
        params.put("author","B20-Project");
        setQueryParam(params);

        ArrayNode commits = get("/repos/{owner}/{repo}/commits").extract().response().as(ArrayNode.class);
        StreamSupport.stream(commits.spliterator(),false)
               .map(commit->commit.get("author").get("login").asText()).collect(Collectors.toList())
               .forEach(login-> assertThat(login,equalTo("B20-Project")));
    }

    @Test
    public void getCommitsByDateRange(){
        Map<String,String> params = new HashMap<>();
        params.put("path","src/test/java/com/Poker/pages/AbstractPageBase.java");
        params.put("author","B20-Project");
        params.put("since","2020-10-13T06:00:00Z");
        params.put("until","2020-10-14T06:00:00Z");
        setQueryParam(params);

        get("/repos/{owner}/{repo}/commits").extract().jsonPath().getList("commit.author",JsonNode.class)
                .forEach(author-> {
                    DateTime date = DateTime.parse(author.get("date").asText());
                    assertThat(date,greaterThanOrEqualTo(DateTime.parse(params.get("since"))));
                    assertThat(date,lessThanOrEqualTo(DateTime.parse(params.get("until"))));
                });
    }

    @Test
    public void getCommitsAndVerifyPath(){
        Map<String,String> params = new HashMap<>();
        params.put("path","src/test/java/com/Poker/pages/AbstractPageBase.java");
        params.put("author","B20-Project");
        params.put("since","2020-10-13T06:00:00Z");
        params.put("until","2020-10-014T6:00:00Z");
        setQueryParam(params);

        List<JsonNode> list = get("/repos/{owner}/{repo}/commits")
                .extract().jsonPath().getList("",JsonNode.class);

        resetRequestSpec();
        Map<String,Object> pathParam = new HashMap<>();
        list.forEach(commit->{
            String sha = commit.get("sha").asText();
            pathParam.put("sha",sha);
            setCustomReqSpec(pathParam);
            List<String> files =  get("/repos/{owner}/{repo}/commits/{sha}")
                   .extract().jsonPath().getList("files.filename");
           assertThat(files,hasItem(params.get("path")));
        });
    }

    @Test
    public void getCommitVerifyAuthorVerification(){
        Map<String,Object> params = new HashMap<>();
        params.put("sha","34aba2e9fc2acdce4c5405d59243335dd3f995a5");
        setCustomReqSpec(params);

        get("/repos/{owner}/{repo}/commits/{sha}");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode commit = mapper.valueToTree(getResponse().extract().jsonPath().get("commit"));
        JsonNode author = commit.get("author");
        JsonNode verification = commit.get("verification");

        ObjectNode expectedAuthor = mapper.createObjectNode();
        expectedAuthor.put("name","B20-Project");
        expectedAuthor.put("email","dilyar.aji@hotmail.com");
        expectedAuthor.put("date","2020-10-13T19:28:46Z");

        assertThat(author,equalTo(expectedAuthor));

        assertThat(verification.get("verified").asBoolean(),is(false));
        assertThat(verification.get("reason").asText(),equalTo("unsigned"));

        assertThat(commit.get("message").asText(),equalTo("config"));
    }

    @Test
    public void getCommitVerifyStatsAndFiles(){
        Map<String,Object> params = new HashMap<>();
        params.put("sha","34aba2e9fc2acdce4c5405d59243335dd3f995a5");
        setCustomReqSpec(params);

        get("/repos/{owner}/{repo}/commits/{sha}");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode stats = mapper.valueToTree(getResponse().extract().path("stats"));
        assertThat(stats.get("total").asInt(),equalTo(25));
        assertThat(stats.get("additions").asInt(),equalTo(12));
        assertThat(stats.get("deletions").asInt(),equalTo(13));

        List<String> fileNames = Arrays.asList("AbstractPageBase","AbstractTestBase","BrowserUtils",
                                "ConfigurationReader","Driver","GlobalDataUtil","HelperUtil","XpathUtil");
        List<JsonNode> files = getResponse().extract().jsonPath().getList("files",JsonNode.class);
        for (int i = 0; i < files.size(); i++) {
            assertThat(files.get(i).get("filename").asText(),containsString(fileNames.get(i)));
        }
    }

    @Test
    public void compareCommits(){
        Map<String,Object> params = new HashMap<>();
        params.put("base","master");
        params.put("head","APItest");
        setCustomReqSpec(params);
        get("/repos/{owner}/{repo}/compare/{base}...{head}");

        getResponse().assertThat()
                .body("status",equalTo("diverged"))
                .body("ahead_by",is(1))
                .body("behind_by",is(2))
                .body("total_commits",is(1));

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode files = mapper.valueToTree(getResponse().extract().path("files"));
        List<String> fileNames = Arrays.asList("AbstractPageBase","Hooks","Message");
        for (int i = 0; i < files.size(); i++) {
            assertThat(files.get(i).get("filename").asText(),containsString(fileNames.get(i)));
        }
    }

    @Test
    public void createSimpleCommitComment(){
        String commit_sha = "93452d7d36c85771f5632ffbd8aa4da2abd148b1";
        String commentText = "this is a comment created remotely through github API";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("body",commentText);
        post("/repos/{owner}/{repo}/commits/"+commit_sha+"/comments",payload,201);
    }

    @Test
    public void createFullCommitComment(){
        String commit_sha = "93452d7d36c85771f5632ffbd8aa4da2abd148b1";
        String commentText = "this is a comment created remotely through github API  at line index 15";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("body",commentText);
        payload.put("path","src/test/java/com/Poker/pages/ActivityStreamPage.java");
        payload.put("position",15);

        post("/repos/{owner}/{repo}/commits/"+commit_sha+"/comments",payload,201);
        getResponse().assertThat()
                .body("position",is(15))
                .body("path",equalTo(payload.get("path").asText()))
                .body("body",equalTo(commentText));
    }

    @Test
    public void getCommitComment(){
        Map<String,Object> params = new HashMap<>();
        params.put("comment_id","48147689");
        setCustomReqSpec(params);

        get("/repos/{owner}/{repo}/comments/{comment_id}");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode comment = mapper.valueToTree(getResponse().extract().response().path(""));

        assertThat(comment.get("position").asInt(),is(11));
        assertThat(comment.get("line").asInt(),is(29));
        assertThat(comment.get("path").asText(),containsString("Driver.java"));
        assertThat(comment.get("body").asText(),equalTo("which line"));

        DateTime created_at = DateTime.parse("2021-03-11T18:09:14Z");
        assertThat(DateTime.parse(comment.get("created_at").asText()),is(created_at));
    }

    @Test
    public void listCommitComments(){
        String commit_sha = "93452d7d36c85771f5632ffbd8aa4da2abd148b1";
        get("/repos/{owner}/{repo}/commits/"+commit_sha+"/comments");
    }

    @Test
    public void deleteCommitComment(){
        String comment_id = "48156948";
        delete("/repos/{owner}/{repo}/comments/"+comment_id,204);

        get("/repos/{owner}/{repo}/comments/"+comment_id,404);
    }

    @Test
    public void updateCommitComment(){
        String comment_id = "48156876";
        Map<String,Object> payload = new HashMap<>();
        payload.put("body","this is a comment patch test for a given comment_id");
        patch("/repos/{owner}/{repo}/comments/"+comment_id,payload);
        getResponse().assertThat()
                .body("body",equalTo(payload.get("body")));
    }
}
