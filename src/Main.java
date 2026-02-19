import model.PyDict;

import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		PyDict<String, Integer> dict = PyDict.of(
				"Blue", 1,
				"Saltarina", 2
		);

		System.out.println(Arrays.toString(dict.items()));
		System.out.println(dict.get("Saltarina"));
		System.out.println(dict.popItem());
		System.out.println(Arrays.toString(dict.keys()));
	}
}
