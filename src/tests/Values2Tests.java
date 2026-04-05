package tests;
import model.PyDict;
import java.util.Arrays;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class Values2Tests contains some tests to check the PyDict method
 * public V[] values(IntFunction<V[]> arrayProvider)
 */
public class Values2Tests{
    
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
     * values of an empty dictionary
     */
    @Test
    public void EmptyDictKeys(){
        Integer[] values = dict.values(Integer[]::new);
        assertEquals("[]", Arrays.toString(values));
    }
    
    /**
     * Test to check if it returns an array with the values of
     * a not empty dictionary
     */
    @Test
    public void NotEmptyDictKeys(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        
        Integer[] values = dict.values(Integer[]::new);
        assertEquals("[1, 2, 3]", Arrays.toString(values));
    }
}