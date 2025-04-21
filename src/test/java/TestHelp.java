import errors.MyError;
import errors.Resource;
import org.junit.Test;

import java.io.IOException;

public class TestHelp {
    @Test
    public void test1() throws IOException {
        Resource res=new Resource();
        res.getResource();
    }
}
