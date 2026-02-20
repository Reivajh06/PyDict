import model.PyDict;

import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		PyDict<String, Integer> dict = PyDict.of(
				"D", 1,
				"G", 2,
				"V", 3,
				"C", 6
		);

		System.out.println();
		System.out.println(Arrays.toString(dict.copy().values()));
	}
}
