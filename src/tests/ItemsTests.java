package tests;
import model.PyDict;
import java.util.Arrays;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class ItemsTests contains some tests to check the PyDict method
 * public Pair<K, V>[] items()
 */
public class ItemsTests{
    
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
     * items of an empty dictionary
     */
    @Test
    public void EmptyDictKeys(){
        Object[] items = dict.items();
        assertEquals("[]", Arrays.toString(items));
    }
    
    /**
     * Test to check if it returns an array with the items (key/value) of
     * a not empty dictionary
     */
    @Test
    public void NotEmptyDictKeys(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        
        Object[] items = dict.items();
        assertEquals("[[A, 1], [B, 2], [C, 3]]", Arrays.toString(items));
    }
}