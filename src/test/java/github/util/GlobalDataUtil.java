package github.util;

import lombok.*;

@Getter
@Setter

public class GlobalDataUtil {

    private static GlobalDataUtil instance;
    private String head_sha;
    private String PR_Review_Path;
    private int PR_Review_Position;
    private String PR_Review_body;
    private int PR_Review_ID;
    private String PR_Review_Comment_Body;
    private String PR_Review_State;


    private GlobalDataUtil(){};

    public static GlobalDataUtil get(){
        if (instance==null){
            instance = new GlobalDataUtil();
        }
        return instance;
    }

}
