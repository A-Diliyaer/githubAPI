package github.steps;

import com.fasterxml.jackson.databind.node.ObjectNode;
import github.util.GlobalDataUtil;

import java.util.HashMap;
import java.util.Map;

import static github.util.ApiUtil.*;
import static github.payloads.ApiPayloads.*;

public class PullRequestSteps {

    public static GlobalDataUtil globalDataUtil = GlobalDataUtil.get();
    public static void getListOfPullRequests(){
        get("/repos/{owner}/{repo}/pulls");
    }

    public static void createPullRequest(Object payload){
        post("/repos/{owner}/{repo}/pulls",payload,201).log().body();
    }

    public static void getPullRequestByNumber(int pull_number){
        Map<String,Object> param = new HashMap<>();
        param.put("pull_number",pull_number);
        setCustomReqSpec(param);
        get("/repos/{owner}/{repo}/pulls/{pull_number}");
    }

    public static void updatePullRequest(int pull_number,Object payload){
        Map<String,Object> param = new HashMap<>();
        param.put("pull_number",pull_number);
        setCustomReqSpec(param);
        patch("/repos/{owner}/{repo}/pulls/{pull_number}",payload);
    }

    public static void retrievePullRequestFiles(int pull_number){
        Map<String,Object> param = new HashMap<>();
        param.put("pull_number",pull_number);
        setCustomReqSpec(param);
        get("/repos/{owner}/{repo}/pulls/{pull_number}/files");
    }

    public static boolean checkIfPullRequestIsMerged(int pull_number){
       getPullRequestByNumber(pull_number);
       if (getResponse().extract().response().path("merged_at") == null){
           String head_sha = getResponse().extract().response().path("head.sha");
           globalDataUtil.setHead_sha(head_sha);
           return true;
       }else{
           System.out.println("pull request already merged");
           return false;
       }
    }

    public static void mergePullRequest(){
        ObjectNode payload = getMergePullRequestPayload();
        payload.put("sha",globalDataUtil.getHead_sha());
        put("/repos/{owner}/{repo}/pulls/{pull_number}/merge",payload);
    }

    public static void createPullRequestReviewWithComment(int pull_number, String path, int position){
        Map<String,Object> param = new HashMap<>();
        param.put("pull_number",pull_number);
        setCustomReqSpec(param);
        globalDataUtil.setPR_Review_Path(path);
        globalDataUtil.setPR_Review_Position(position);
        ObjectNode payload = getPullReqReviewPayload();
        post("/repos/{owner}/{repo}/pulls/{pull_number}/reviews",payload);
        globalDataUtil.setPR_Review_ID(getResponse().extract().response().path("id"));
    }

    public static void createPullRequestReviewNoComment(int pull_number){
        Map<String,Object> param = new HashMap<>();
        param.put("pull_number",pull_number);
        setCustomReqSpec(param);
        Map<String,Object> payload = new HashMap<>();
        payload.put("body","this is my first PR review from java aji");
        payload.put("event","COMMENT");
        post("/repos/{owner}/{repo}/pulls/{pull_number}/reviews",payload);
        globalDataUtil.setPR_Review_ID(getResponse().extract().response().path("id"));
    }

    public static void retrievePullRequestReviewByID(int statusCode){
        get("/repos/{owner}/{repo}/pulls/{pull_number}/reviews/"+globalDataUtil.getPR_Review_ID(),statusCode);
    }

    public static void deletePullRequestReviewByID(){
        delete("/repos/{owner}/{repo}/pulls/{pull_number}/reviews/"+globalDataUtil.getPR_Review_ID());
    }

    public static void retrievePullRequestReviewComment(){
        get("/repos/{owner}/{repo}/pulls/{pull_number}/reviews/"+globalDataUtil.getPR_Review_ID()+"/comments");
    }


    public static void submitPullRequestReview(){
        Map<String,String> payload = new HashMap<>();
        payload.put("event","COMMENT");
        globalDataUtil.setPR_Review_State(payload.get("event"));
        post("/repos/{owner}/{repo}/pulls/{pull_number)/reviews/"+globalDataUtil.getPR_Review_ID()+"/events",payload);
    }

    public static void getRepoReviewComments(){
        get("/repos/{owner}/{repo}/pulls/comments");
    }
}
