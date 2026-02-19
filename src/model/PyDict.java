package model;

import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Stream;

@SuppressWarnings("all")
public class PyDict<K, V> {

	/*
	Implemented so far (Python-dict-like API):
	- fromKeys(...)
	- of(...)
	- put
	- get
	- update
	- keys
	- values
	- items

	Missing / TODO:
	- copy
	- popitem
	- setdefault
	 */

	private static final int EXPANSIONFACTOR = 75;

	private static final int DKIXEMPTY = -1;
	private static final int DKIXDUMMY = -2;
	private static final int PERTURBSHIFT = 5;
	private static final int BASELENGTH = 8;

	private int[] indices;
	private int mask;

	private Entry<K, V>[] entries;
	private int nextEntryIndex = 0;

	private int nItems;

	public static <K, V> PyDict<K, V> fromKeys(K[] keys, V initialValue) {
		return new PyDict<>(keys, initialValue);
	}

	public static <K, V> PyDict<K, V> fromKeys(K[] keys) {
		return fromKeys(keys, null);
	}

	@SuppressWarnings("all")
	public static <K, V> PyDict<K, V> of(Object... args) {
		if(args.length % 2 != 0) throw new IllegalArgumentException("Args must be key, value pairs");

		PyDict raw = new PyDict();

		for(int i = 0; i < args.length; i += 2) {
			raw.put(args[i], args[i + 1]);
		}

		return raw;
	}

	public PyDict() {
		indices = new int[BASELENGTH];
		fill(indices, -1);
		entries = new Entry[BASELENGTH];
		mask = BASELENGTH - 1;

		nItems = 0;
	}

	public PyDict(K[] keys, V[] values) {
		if(keys.length != values.length) throw new IllegalArgumentException("keys and values arrays lengths do not match");

		indices = new int[keys.length * 2];
		fill(indices, -1);
		entries = new Entry[keys.length * 2];
		mask = indices.length - 1;
		nItems = 0;

		for(int i = 0; i < keys.length; i++) {
			put(keys[i], values[i]);
		}
	}

	/*
	* Constructor similar to fromkeys function in python to create dictionary with default value
	*
	* */
	public PyDict(K[] keys, V initialValue) {
		indices = new int[keys.length * 2];
		fill(indices, -1);
		entries = new Entry[keys.length * 2];

		mask = indices.length - 1;

		nItems = 0;

		for (K key : keys) {
			put(key, initialValue);
		}
	}

	public PyDict(K[] keys){
		this(keys, (V[]) new Object());
	}

	private void grow() {
		Entry<K, V>[] entr = new Entry[entries.length];
		System.arraycopy(entries, 0, entr, 0, entries.length);

		entries = new Entry[entries.length * 2];
		indices = new int[indices.length * 2];

		fill(indices, -1);

		mask = indices.length - 1;
		nItems = 0;
		nextEntryIndex = 0;

		for(Entry<K, V> entry : entr) {
			if(entry == null) continue;
			put(entry.key, entry.value);
		}
	}

	private void fill(int[] arr, int initialValue) {
		for(int i = 0; i < arr.length; i++) {
			arr[i] = initialValue;
		}
	}

	public void clear() {
		entries = new Entry[entries.length];
		indices = new int[indices.length];
		nItems = 0;
		nextEntryIndex = 0;
	}

	public void put(K key, V value) {
		if((nItems * 100) / indices.length >= EXPANSIONFACTOR) {
			grow();
		}

		int firstDummyPos = -1;

		int perturb = key.hashCode();
		int j = perturb;
		int index = j & mask;

		while(true) {
			if(firstDummyPos == -1) {
				if(indices[index] == DKIXDUMMY) firstDummyPos = index;

				else if(indices[index] == DKIXEMPTY) break;

			} else {
				if(indices[index] == DKIXEMPTY) {
					index = firstDummyPos;
					break;
				}
			}

			if(entries[indices[index]].key.equals(key)) {
				entries[indices[index]] = new Entry<>(key, value, key.hashCode());
				return;
			}

			//unsigned shift
			perturb >>>= PERTURBSHIFT;
			j = (5 * j) + 1 + perturb;
			index = j & mask;
		}

		entries[nextEntryIndex] = new Entry<>(key, value, key.hashCode());
		indices[index] = nextEntryIndex++;
		nItems++;
	}

	public V get(K key) {
		if(nItems == 0) return null;

		int perturb = key.hashCode();
		int j = perturb;
		int index = j & mask;

		while(true) {
			if(indices[index] == DKIXEMPTY) return null;

			if(entries[indices[index]].key.equals(key)) {
				return entries[indices[index]].value;
			}

			perturb >>>= PERTURBSHIFT;
			j = (5 * j) + 1 + perturb;
			index = j & mask;
		}
	}

	public V pop(K key) {
		if(nItems == 0) throw new IllegalArgumentException("Key %s not found".formatted(key));

		int perturb = key.hashCode();
		int j = perturb;
		int index = j & mask;

		while(true) {
			if(indices[index] == DKIXEMPTY) throw new IllegalArgumentException("Key %s not found".formatted(key));

			if(entries[indices[index]].key.equals(key)) {
				break;
			}

			perturb >>>= PERTURBSHIFT;
			j = (5 * j) + 1 + perturb;
			index = j & mask;
		}

		//Should eliminate entry of key and readjust array
		//Do NOT reduce array length, just readjust insertion order of the entries array
		V val = entries[indices[index]].value;

		entries[indices[index]] = null;
		indices[index] = DKIXDUMMY;

		if(index != indices.length - 1) {
			System.arraycopy(indices, index + 1, indices, index, indices.length - (index + 1));
			System.arraycopy(entries, index + 1, entries, index, entries.length - (index  + 1));
		}

		nItems--;

		return val;
	}

	public Object pop(K key, Object defaultValue) {
		if(nItems == 0) return defaultValue;

		int perturb = key.hashCode();
		int j = perturb;
		int index = j & mask;

		while(true) {
			if(indices[index] == DKIXEMPTY) return defaultValue;

			if(entries[indices[index]].key.equals(key)) {
				break;
			}

			perturb >>>= PERTURBSHIFT;
			j = (5 * j) + 1 + perturb;
			index = j & mask;
		}

		//Should eliminate entry of key and readjust array
		//Do NOT reduce array length, just readjust insertion order of the entries array
		V val = entries[indices[index]].value;

		entries[indices[index]] = null;
		indices[index] = DKIXDUMMY;

		if(index != indices.length - 1) {
			System.arraycopy(indices, index + 1, indices, index, indices.length - (index + 1));
			System.arraycopy(entries, index + 1, entries, index, entries.length - (index  + 1));
		}

		nItems--;

		return val;
	}

	public Pair popItem() {
		if(nItems == 0) throw new IndexOutOfBoundsException("PyDict is empty!");

		int i = nextEntryIndex - 1;

		Entry e = entries[i];

		int perturb = entries[i].hash;
		int j = perturb;
		int index = j & mask;

		while(true) {
			if(indices[index] == DKIXEMPTY) return null;

			if(entries[indices[index]].key.equals(e.key)) {
				Pair p = new Pair(e.key, e.value);
				entries[indices[index]] = null;
				indices[index] = DKIXDUMMY;
				nextEntryIndex--;
				nItems--;
				return p;
			}

			perturb >>>= PERTURBSHIFT;
			j = (5 * j) + 1 + perturb;
			index = j & mask;
		}
	}

	public void update(PyDict<K, V> otherDict) {
		for(Pair<K, V> pair : otherDict.items()) {
			put(pair.key, pair.value);
		}
	}

	public Object[] keys() {
		return Stream.of(entries)
				.filter(Objects::nonNull)
				.map(Entry::key)
				.toArray();
	}

	public K[] keys(IntFunction<K[]> arrayProvider) {
		//Method to return the keys as their intended class specified when the object PyDict was created
		//Basically imitates the use of the toArray function used in the List interface
		//Provided by an IntFunction<generic> which identifies the desired class to return (Example, keys(String[]::new))
		return Stream.of(entries)
				.filter(Objects::nonNull)
				.map(Entry::key)
				.toArray(arrayProvider);
	}

	public V[] values() {
		return (V[]) Stream.of(entries)
				.filter(Objects::nonNull)
				.map(Entry::value)
				.toArray(Object[]::new);
	}

	public V[] values(IntFunction<V[]> arrayProvider) {
		return Stream.of(entries)
				.filter(Objects::nonNull)
				.map(Entry::value)
				.toArray(arrayProvider);
	}


	public Pair<K, V>[] items() {
		return (Pair<K, V>[]) Stream.of(entries)
				.filter(Objects::nonNull)
				.map(e -> new Pair<>(e.key, e.value))
				.toArray(Pair[]::new);
	}

	private record Entry<K, V>(K key, V value, int hash) {
	}

	public record Pair<K, V>(K key, V value) {

		public String toString() {
			return "[%s, %s]".formatted(key, value);
		}
	}
}
