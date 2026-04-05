package tests;
import model.PyDict;
import java.util.Arrays;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class SetDefaultTests contains some tests to check the PyDict method
 * public V setDefault(K key, V defaultValue)
 */
public class SetDefaultTests{
    
    PyDict<String, Integer> dict;

    /**
     * Initializing dictionary dict before the tests
     */
    @Before
    public void init(){
        dict = new PyDict<>();
    }
    
    /**
     * Test to check if an empty dictionary has a value for a null key.
     * In this case, the dictionary does not have a value for a null key,
     * and it creates a new item with the key with the default value, 
     * also null. We expected a NullPointerException because it is not 
     * posible to add a null key to the dictionary
     */
    @Test(expected = NullPointerException.class)
    public void setDefaultNullKeyEmptyDict(){
        Object value = dict.setDefault(null, null);
    }
    
    /**
     * Test to check if a not empty dictionary has a value for a null key.
     * In this case, the dictionary does not have a value for a null key,
     * It must raise a NullPointerException, because a key cannot be null
     */
    @Test(expected = NullPointerException.class)
    public void setDefaultNullKeyNotEmptyDict(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        
        Object value = dict.setDefault(null, null);
    }
    
    /**
     * Test to check if an empty dictionary has a value for a not null key.
     * In this case, the dictionary does not have a value for this key,
     * and it creates a new item with the key, and the default value.
     * We check if the dictionary has the new item
     */
    @Test
    public void setDefaultNotNullKeyEmptyDict(){
        Object value = dict.setDefault("A", 1);
        assertEquals("[[A, 1]]", Arrays.toString(dict.items()));
    }
    
    /**
     * Test to check if a not empty dictionary has a value for a not null
     * key. In this case, the dictionary does not have a value for this key,
     * and it creates a new item with the key, and the default value.
     * We check if the dictionary has the new item
     */
    @Test
    public void setDefaultNotNullKeyNotEmptyDict(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        
        Object value = dict.setDefault("D", 4);
        assertEquals("[[A, 1], [B, 2], [C, 3], [D, 4]]",
                     Arrays.toString(dict.items()));
    }
    
    /**
     * Test to check if a not empty dictionary has a value for a not null 
     * key. In this case, the dictionary has a value for this key, which is
     * returned by the PyDict method setDefault()
     */
    @Test
    public void setDefaultNotNullKeyNotEmptyDict2(){
        dict = dict.of(
            "A", 1,
            "B", 2,
            "C", 3
        );
        
        Object value = dict.setDefault("A", 4);
        assertEquals(1, value);
    }
}