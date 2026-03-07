package tests;
import model.PyDict;
import java.util.Arrays;
import org.junit.*;
import static org.junit.Assert.*;

public class GetValueTests{
    PyDict<String, Integer> dict;

    @Before
    public void init(){
        dict = new PyDict<>();
    }

    @Test
    public void testGetNoKeyEmptyDict(){
        Object value = dict.get(null);
        assertEquals(null, value);
    }

    @Test
    public void testGetKeyEmptyDict(){
        Object value = dict.get("A");
        assertEquals(null, value);
    }
    
    @Test(expected = NullPointerException.class)
    public void testGetNoKey(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        Object value = dict.get(null);
    }

    @Test
    public void testGetWrongKey(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        Object value = dict.get("D");
        assertEquals(null, value);
    }

    @Test
    public void testGetCorrectKey(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        Object value = dict.get("A");
        assertEquals(1, value);
    }
}