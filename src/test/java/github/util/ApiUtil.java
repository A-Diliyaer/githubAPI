package github.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


public class ApiUtil {
    protected static RequestSpecification requestSpec;
    protected static String access_token;
    protected static ValidatableResponse response;
    protected static String active_repo;
    protected static String active_branch;

    public static RequestSpecification setBaseReqSpec(){
        access_token = ConfigurationReader.getProperty("A-token");
        requestSpec = given()
                .accept(ContentType.JSON)
                .log().ifValidationFails()
                .auth().oauth2(access_token);

        return requestSpec;
    }

    public static RequestSpecification setRepoReqSpec(){
        setBaseReqSpec();
        requestSpec
            .pathParam("owner","A-Diliyaer")
            .pathParam("repo",active_repo);
        return requestSpec;
    }
    public static RequestSpecification setBranchReqSpec(){
        setRepoReqSpec();
        requestSpec
                    .pathParam("branch",active_branch);
        return requestSpec;
    }

    public static RequestSpecification setCustomReqSpec( Map<String,Object> params){
        requestSpec
                .pathParams(params);
        return requestSpec;
    }

    public static void resetRequestSpec(){
        reset();
        baseURI = ConfigurationReader.getProperty("github.base_url");
    }

    public static RequestSpecification setQueryParam(Map<String,String> params){
        requestSpec
                .queryParams(params);
        return requestSpec;
    }

    public static void setActiveRepo(){
        active_repo = ConfigurationReader.getProperty("active_repo");
    }

    public static String getActiveRepo(){
        return active_repo;
    }

    public static void setActiveBranch(){
        active_branch = ConfigurationReader.getProperty("active_branch");
    }

    public static String getActiveBranch(){
        return active_branch;
    }

    public static ValidatableResponse get(String url){
        return get(url,200);
    }

    public static ValidatableResponse get(String url, int statusCode){
        response = given().spec(requestSpec)
                .when().get(url)
                .then().statusCode(is(statusCode));
        return response;
    }

    public static ValidatableResponse post(String url){
        return post(url,"",200);
    }

    public static ValidatableResponse post(String url, int statusCode){
        return post(url,"",statusCode);
    }

    public static ValidatableResponse post(String url, Object body){
        return post(url,body,200);
    }

    public static ValidatableResponse post(String url, Object body, int statusCode){
        response = given().spec(requestSpec)
                .body(body)
                .when().post(url)
                .then().log().ifError()
                .statusCode(is(statusCode));
        return response;
    }

    public static ValidatableResponse postFile(String url, File file){
        response = given().spec(requestSpec)
                .body(file)
                .when().post(url)
                .then();
        return response;
    }

    public static ValidatableResponse put(String url){
        return put(url,"",200);
    }

    public static ValidatableResponse put(String url, int statusCode){
        return put(url,"",statusCode);
    }

    public static ValidatableResponse put(String url, Object body){
        return put(url,body,200);
    }

    public static ValidatableResponse put(String url, Object body, int statusCode){
        response = given().spec(requestSpec)
                .body(body)
                .when().put(url)
                .then().log().ifError()
                .statusCode(is(statusCode));
        return response;
    }

    public static ValidatableResponse patch(String url){
        return patch(url,"",200);
    }

    public static ValidatableResponse patch(String url, int statusCode){
        return patch(url,"",statusCode);
    }

    public static ValidatableResponse patch(String url, Object body){
        return patch(url,body,200);
    }

    public static ValidatableResponse patch(String url, Object body, int statusCode){
        response = given().spec(requestSpec)
                .body(body)
                .when().patch(url)
                .then().log().ifError()
                .statusCode(is(statusCode));
        return response;
    }

    public static ValidatableResponse delete(String url){
        return delete(url,"",200);
    }

    public static ValidatableResponse delete(String url, int statusCode){
        return delete(url,"",statusCode);
    }

    public static ValidatableResponse delete(String url, Object body){
        return delete(url,body,200);
    }

    public static ValidatableResponse delete(String url, Object body, int statusCode){
        response = given().spec(requestSpec)
                .body(body)
                .when().delete(url)
                .then().log().ifError()
                .statusCode(is(statusCode));
        return response;
    }

    public static ArrayNode extractAsArrayNode(String path){
        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(response.extract().response().path(path));
    }

    public static JsonNode extractAsJsonNode(String path){
        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(response.extract().response().path(path));
    }

    public static JsonNode StringToJson(String content){
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jn = null;
        try {
            jn = mapper.readValue(content,JsonNode.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return jn;
    }

    public static JsonNode extractAsJsonNode(){
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jn = null;
        try {
            jn = mapper.readValue(response.extract().asString(),JsonNode.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return jn;
    }

    public static List<Object> extractList(String jsonPath){
        try {
            return response.extract().jsonPath().getList(jsonPath);
        }catch (NullPointerException e){
            return null;
        }
    }

    public static ValidatableResponse getResponse(){
        return response;
    }

    public static List<String> shuffleStreamNode(){
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode node = mapper.createArrayNode();
        return StreamSupport.stream(node.spliterator(),false).map(each->each.get("field").asText())
                            .collect(Collectors.collectingAndThen(Collectors.toList(),collected-> {
                                Collections.shuffle(collected);
                                return collected.stream();
                            })).collect(Collectors.toList());
    }
}
