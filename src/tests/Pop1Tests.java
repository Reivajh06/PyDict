package tests;
import model.PyDict;
import java.util.Arrays;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class Pop1Tests contains some tests to check the PyDict method
 * public V pop(K key)
 */
public class Pop1Tests{
    PyDict<String, Integer> dict;

    /**
     * Initializing dictionary dict before the tests
     */
    @Before
    public void init(){
        dict = new PyDict<>();
    }


    /**
     * Test to pop an item with a null key from an empty dictionary.
     * It generates an IllegalArgumentException because the dictionary
     * is empty
     */
    @Test(expected = IllegalArgumentException.class)
    public void popNullKeyEmptyDict(){
        dict.pop(null);
    }

    /**
     * Test to pop an item with a not null key from an empty dictionary.
     * It generates an IllegalArgumentException because the dictionary is empty
     */
    @Test(expected = IllegalArgumentException.class)
    public void popNotNullKeyEmptyDict(){
        dict.pop("A");
    }

    /**
     * Test to pop an item with a null key from a not empy dictionary.
     * It generates a NullPointerExcetpion because a null key is not alowed
     * in the dictionary
     */
    @Test(expected = NullPointerException.class)
    public void popNullKeyNotEmpyDict(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        
        dict.pop(null);
    }

    /**
     * Test to pop an item that not belongs to the dictionary.
     * It generates an IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void popNotNullNotValidKeyNotEmpyDict(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        
        dict.pop("D");
    }

    /**
     * Test to pop an item that belongs to the dictionary.
     * It pops the item (key, value) from the dictionary and returns the value
     */
    @Test
    public void popNotNullValidKeyNotEmptyDict(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        
        int pop_value = dict.pop("A");
        assertEquals(1, pop_value);
    }
}
