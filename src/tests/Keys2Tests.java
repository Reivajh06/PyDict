package tests;
import model.PyDict;
import java.util.Arrays;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class Keys2Tests contains some tests to check the PyDict method
 * public K[] keys(IntFunction<K[]> arrayProvider)
 */
public class Keys2Tests{
    
    PyDict<String, Integer> dict;

    /**
     * Initializing dictionary dict before the tests
     */
    @Before
    public void init(){
        dict = new PyDict<>();
    }
    
    /**
     * Test to check if it returns an empty array while checking the
     * keys of an empty dictionary
     */
    @Test
    public void EmptyDictKeys(){
        String[] keys = dict.keys(String[]::new);
        assertEquals("[]", Arrays.toString(keys));
    }
    
    /**
     * Test to check if it returns an array with the keys of
     * a not empty dictionary
     */
    @Test
    public void NotEmptyDictKeys(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        
        String[] keys = dict.keys(String[]::new);
        assertEquals("[A, B, C]", Arrays.toString(keys));
    }
}