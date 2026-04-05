package tests;
import model.PyDict;
import java.util.Arrays;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class ToStringTests contains some tests to check the PyDict method
 * public String toString()
 */
public class ToStringTests{
    
    PyDict<String, Integer> dict;

    /**
     * Initializing dictionary dict before the tests
     */
    @Before
    public void init(){
        dict = new PyDict<>();
    }
    
    /**
     * Test to check the PyDict method toString() with an empty dictionary
     */
    @Test
    public void EmptyDictToString(){
        String dict_string = dict.toString();
        assertEquals("{}", dict_string);
    }
    
    /**
     * Test to check the PyDict method toString() with a not empty dictionary
     */
    @Test
    public void NotEmptyDictToString(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        
        String dict_string = dict.toString();
        assertEquals("{A: 1, B: 2, C: 3}", dict_string);
    }
}