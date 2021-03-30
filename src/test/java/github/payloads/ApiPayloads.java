package github.payloads;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import github.pojos.BranchProtection;
import github.pojos.PullRequest;
import github.pojos.Repo;
import github.util.GlobalDataUtil;

public class ApiPayloads {

    private static GlobalDataUtil globalDataUtil = GlobalDataUtil.get();
    public static Repo createFullRepo(){
        return new Repo("PojoTest","Full Pojo Created","http://localhost:8787",
                true,true,false,true, false,
                true,false, false,false);
    }

    public static BranchProtection createBranchProtection(){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode status_check = mapper.createObjectNode();
        status_check.put("strict",true);
        ArrayNode arrayNode = status_check.putArray("contexts");
        arrayNode.add("success");
        arrayNode.add("pending");
        ObjectNode pr_review = mapper.createObjectNode();
        pr_review.put("dismiss_stale_reviews",true);
        pr_review.put("require_code_owner_reviews",true);
        pr_review.put("required_approving_review_count",3);

        return new BranchProtection
                (status_check,true,
                        null,
                        null,
                        false,
                        true,
                        true);
    }

    public static PullRequest PullRequestPayLoad(String head, String base, boolean draft){
        PullRequest payload = new PullRequest();
        payload.setTitle(head+" to "+base);
        payload.setHead(head);
        payload.setBase(base);
        payload.setBody(head+" is merging into "+base);
        payload.setDraft(draft);
        return payload;
    }

    public static ObjectNode getMergePullRequestPayload(){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("commit_title","merge demo");
        payload.put("commit_message","simple merge");
        payload.put("merge_method","merge");
        return payload;
    }

    public static ObjectNode getPullReqReviewPayload(){

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("body","this is my first PR review from java");
        payload.put("event","COMMENT");

        ArrayNode comments = payload.putArray("comments");
        ObjectNode comment = mapper.createObjectNode();

        comment.put("path", globalDataUtil.getPR_Review_Path());
        comment.put("position",globalDataUtil.getPR_Review_Position());
        comment.put("body","this is PR review from java with PENDING status");
        comments.add(comment);
        GlobalDataUtil.get().setPR_Review_body(payload.get("body").asText());
        globalDataUtil.setPR_Review_Comment_Body(comment.get("body").asText());
        return payload;
    }
}
