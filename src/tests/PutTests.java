package tests;
import model.PyDict;
import java.util.Arrays;
import org.junit.*;
import static org.junit.Assert.*;

public class PutTests {
    PyDict<String, Integer> dict;

    @Before
    public void init(){
        dict = new PyDict<>();
    }

    @Test(expected = NullPointerException.class)
    public void testPutNoKeyNoValue(){
        dict.put(null, null);
    }
    
    @Test (expected = NullPointerException.class)
    public void testPutNoKey(){
        dict.put(null, 1);
    }
    
    @Test
    public void testPutNoValue(){
        dict.put("A", null);
        assertEquals("[[%s, %s]]".formatted("A", null), Arrays.toString(dict.items()));
    }

    @Test
    public void testPutKeyValue(){
        dict.put("A", 1);
        assertEquals("[[%s, %d]]".formatted("A", 1), Arrays.toString(dict.items()));
    }
}
