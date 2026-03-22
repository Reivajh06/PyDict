package tests;
import model.PyDict;
import java.util.Arrays;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class PutTests contains some tests to check the PyDict method
 * public void put(K key, V value)
 */
public class PutTests {
    PyDict<String, Integer> dict;

    /**
     * Initializing the dictionary dict before the tests
     */
    @Before
    public void init(){
        dict = new PyDict<>();
    }

    /**
     * Test to add to the dictionary a null key and a null value.
     * It's no posible to add to the dictionary a null key, so this test
     * generate a NullPointerException
     */
    @Test(expected = NullPointerException.class)
    public void testPutNoKeyNoValue(){
        dict.put(null, null);
    }

    /**
     * Test to add to the dictionary a null key and a valid value.
     * It's no posible to add to the dictionary a null key, so this test
     * generate a NullPointerException
     */
    @Test (expected = NullPointerException.class)
    public void testPutNoKey(){
        dict.put(null, 1);
    }

    /**
     * Test to add to the dictionary a valid key and a null value.
     * If the key is not null, it's posible a null value, so this test
     * correctly adds the item (key, value) to the dictionary.
     */
    @Test
    public void testPutNoValue(){
        dict.put("A", null);
        assertEquals("[[%s, %s]]".formatted("A", null), Arrays.toString(dict.items()));
    }

    /**
     * Test to add to the dictionary a valid key and a not null value.
     * This test correctly adds the item (key, value) to the dictionary
     */
    @Test
    public void testPutKeyValue(){
        dict.put("A", 1);
        assertEquals("[[%s, %d]]".formatted("A", 1), Arrays.toString(dict.items()));
    }
    
    /**
     * Test to add to the dictionary an item with a repeated key and different
     * value. The dictionary replaces the previous key value with the new one
     */
    @Test
    public void testPutKeyValue2(){
        dict.put("A", 1);
        dict.put("A", 3);
        assertEquals("[[%s, %d]]".formatted("A", 3), Arrays.toString(dict.items()));
    }
    
    /**
     * Test to add to the dictionary an item with repeated key and value.
     * The dictionary replaces the previous key value with the new one, but
     * in this case the modification is not perceptible because it's changing
     * with an item with the same value
     */
    @Test
    public void testPutKeyValue3(){
        dict.put("A", 1);
        dict.put("A", 1);
        assertEquals("[[%s, %d]]".formatted("A", 1), Arrays.toString(dict.items()));
    }
}
