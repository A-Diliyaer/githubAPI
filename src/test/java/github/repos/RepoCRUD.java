package github.repos;

import github.pojos.Repo;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static github.util.ApiUtil.*;
import static github.payloads.ApiPayloads.*;

public class RepoCRUD extends Base{

    @DisplayName("Testing GET /B20-Project/Bitrix24")
    @Test
    public void getBitrix24Repo(){
        JsonPath jp = get("/repos/{owner}/{repo}").extract().jsonPath();

        assertThat(jp.getString("name"),equalTo("Bitrix24"));
        assertThat(jp.getString("owner.login"),equalTo("B20-Project"));
        assertThat(jp.getString("language"),equalTo("Java"));
        assertThat(jp.getString("default_branch"),equalTo("master"));
    }

    @DisplayName("Testing GET /B20-Project/Bitrix 404")
    @Test
    public void getRepoNotFound(){
        JsonPath jp = get("/repos/{owner}/{repo}",404).extract().jsonPath();

        assertThat(jp.getString("message"),equalTo("Not Found"));
    }

    @DisplayName(("Testing POST /user/repos"))
    @Test
    public void createSimpleUserRepo(){
        Map<String,Object> simpleRepo = new HashMap<>();
        simpleRepo.put("name","Postman-Test5");
        simpleRepo.put("description","This is a test repo2");
        simpleRepo.put("private",true);

        setBaseReqSpec();
        JsonPath jp = post("/user/repos",simpleRepo,201).extract().jsonPath();

        assertThat(jp.getString("name"),equalTo("Postman-Test5"));
        assertThat(jp.getString("description"),equalTo("This is a test repo2"));
        assertThat(jp.getBoolean("private"),is(true));
    }

    @DisplayName("Testing POST /user/repos 422")
    @Test
    public void createDuplicateRepo(){
        Map<String,Object> simpleRepo = new HashMap<>();
        simpleRepo.put("name","Postman-Test2");
        simpleRepo.put("description","This is a test repo2");
        simpleRepo.put("private",true);

        setBaseReqSpec();
        JsonPath jp = post("/user/repos",simpleRepo,422).extract().jsonPath();

        assertThat(jp.getString("message"),equalTo("Repository creation failed."));
        assertThat(jp.getString("errors[0].message"),equalTo("name already exists on this account"));
    }

    @DisplayName("Testing POST /user/repos 400")
    @Test
    public void createRepoWithoutBody(){
        setBaseReqSpec();
        post("/user/repos",400)
                .body("message",equalTo("Body should be a JSON object"));
    }

    @DisplayName("Testing POST /user/repos")
    @Test
    public void createRepoWithPojo(){
        Repo expectedRepo = createFullRepo();

        setBaseReqSpec();
        post("/user/repos",expectedRepo,201);
        List<String> repoNames = get("/user/repos").extract().jsonPath().getList("full_name");

        assertThat(repoNames.stream().filter(each->each.contains("PojoTest")).collect(Collectors.joining())
                ,equalTo("B20-Project/PojoTest") );
    }

    @DisplayName("Testing PATCH /repos/B20-Project")
    @Test
    public void updateRepo(){
        Map<String,Object> simpleRepo = new HashMap<>();
        simpleRepo.put("name","Postman-Test2");
        simpleRepo.put("description","This is update v1.1");
        simpleRepo.put("private",false);

        patch("/repos/{owner}/{repo}")
                .body("description",equalTo("This is update v1.1"))
                .body("private",is(false));
    }

    @DisplayName("Testing DELETE /repos/B20-Project/Postman-Test2")
    @Test
    public void deleteUserRepo(){
        delete("/repos/{owner}/{repo}",204);
    }

    @DisplayName("Testing GET /Bitrix24/contributors")
    @Test
    public void getRepoContributors(){
        Map<String,String> params = new HashMap<>();
        params.put("anon","true");
        setQueryParam(params);

        List<String> expected = Arrays.asList("Arafat9329","B20-Project","Deenen","elv-krgb");
        List<String> contributors = get("/repos/{owner}/{repo}/contributors")
                .extract().jsonPath().getList("login");

        contributors.forEach(each-> assertThat(expected,hasItem(each)));
    }

    @DisplayName("Testing GET /Bitrix24/contributors")
    @Test
    public void getHighestRepoContributor(){
        Map<String,String> params = new HashMap<>();
        params.put("anon","true");
        setQueryParam(params);
        String contributor = get("/repos/{owner}/{repo}/contributors")
                .extract().jsonPath().getString("max{it.contributions}.login");

        assertThat(contributor,equalTo("Arafat9329"));
    }

    @DisplayName("GET /Bitrix24/contributors")
    @Test
    public void getRepoContributorByPage(){
        Map<String,String> params = new HashMap<>();
        params.put("anon","true");
        params.put("per_page","2");
        params.put("page","1");

        setQueryParam(params);
        List<String> contributors = get("/repos/{owner}/{repo}/contributors")
                .extract().jsonPath().getList("login");

        assertThat(contributors.size(),is(2));
        assertThat(contributors,hasItem("Arafat9329"));
    }

    @DisplayName("GET /{repo}/contributors 204")
    @Test
    public void getNoContributor(){
        Map<String,String> params = new HashMap<>();
        params.put("anon","true");

        setQueryParam(params);
        get("/repos/{owner}/{repo}/contributors",204);
    }

    @DisplayName("GET /{repo}/languages")
    @Test
    public void getRepoLanguages(){
        Map<String,Object> languages = get("/repos/{owner}/{repo}/languages")
                .extract().jsonPath().getMap("");

        assertThat(languages.containsKey("Java"),is(true));
        assertThat(languages.containsKey("Gherkin"),is(true));
    }

    @DisplayName("Testing GET /{repo}/tags")
    @Test
    public void getRepoTags(){
        get("repos/{owner}/{repo}/tags").body("[0].name",equalTo("v3.1.4"));
    }
}
