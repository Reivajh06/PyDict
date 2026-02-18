import model.PyDict;

import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		PyDict<String, Integer> dict = new PyDict<>(
				new String[] {"Blue", "Serio", "Espabilado", "Albino", "Saltarina", "Pardo"},
				new Integer[] {1, 2, 3, 4, 5, 6}
		);
		System.out.println(Arrays.toString(dict.items()));
		System.out.println(dict.get("Saltarina"));
	}
}
