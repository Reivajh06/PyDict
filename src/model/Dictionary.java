package model;

public class Dictionary<K, V> {

	private static final int BASELENGTH = 8;

	private int slotsFilled;
	private final Entry<K, V> dummy = new Entry(0, null, null);

	private Entry<K, V>[] hashtable;

	public Dictionary() {
		hashtable = new Entry[BASELENGTH];
		slotsFilled = 0;
	}

	public void put(K key, V value) {

		int index = key.hashCode() % hashtable.length;
		Entry<K, V> positionValue = hashtable[index];

		while(positionValue != null) {
			if(positionValue != dummy && positionValue.key.equals(key)) {
				hashtable[index] = new Entry<>(key.hashCode(), key, value);
				break;
			}
		}

		hashtable[index] = new Entry<>(key.hashCode(), key, value);
		slotsFilled++;
	}

	record Entry<K, V> (int hashcode, K key, V value) {
	}
}
