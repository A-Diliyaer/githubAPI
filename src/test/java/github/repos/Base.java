package github.repos;

import github.util.ConfigurationReader;
import github.util.GlobalDataUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.*;
import static github.util.ApiUtil.*;
public class Base {

    protected GlobalDataUtil globalDataUtil = GlobalDataUtil.get();

    @BeforeEach
    public void init(){
        baseURI = ConfigurationReader.getProperty("github.base_url");
        setActiveRepo();
        setActiveBranch();
        setRepoReqSpec();
    }

    @AfterEach
    public void destroy(){
        reset();
    }
}
