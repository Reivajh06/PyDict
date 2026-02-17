import model.Dictionary;

import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		Dictionary<String, Integer> dict = new Dictionary<>();
		dict.put("Aa", 2);
		dict.put("Aa", 4);
		dict.put("BB", 3);
		dict.put("Blue", 5);
		dict.put("El Serio", -8);
		dict.put("El Gato", 7);
		dict.put("El Pardo", 60);
		dict.put("El dqadwada", 80);
		dict.put("adawdwadad", 1);
		dict.put("nhfhfhfhtho", 0);

		System.out.println(Arrays.toString(dict.keys()));
	}
}
