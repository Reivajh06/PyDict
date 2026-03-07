import model.PyDict;

import java.util.Arrays;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({tests.PutTests.class, tests.GetValueTests.class})

public class Main {

    public static void main(String[] args) {
        PyDict<String, Integer> dict = PyDict.of(
                "D", 1,
                "G", 2,
                "V", 3,
                "C", 6
        );
        org.junit.runner.JUnitCore.main("Main");

        System.out.println(Arrays.toString(dict.items()));
        System.out.println(dict.setDefault("D", 2));
        System.out.println(dict.setDefault("A", 30));
        System.out.println(Arrays.toString(dict.items()));
    }
}

