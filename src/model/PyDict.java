package model;

import java.util.Collection;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Stream;

@SuppressWarnings("all")
public class PyDict<K, V> {

	/**
	 * Load factor threshold (as percent) at which the table is grown.
	 * <p>
	 * When {@code (nItems * 100) / indices.length >= EXPANSIONFACTOR}, a resize occurs.
	 * </p>
	 */
	private static final int EXPANSIONFACTOR = 66;

	/** Marker stored in {@link #indices} meaning "empty bucket". */
	private static final int DKIXEMPTY = -1;

	/** Marker stored in {@link #indices} meaning "dummy bucket" (tombstone). */
	private static final int DKIXDUMMY = -2;

	/**
	 * Perturb right-shift constant used in the probe sequence:
	 * {@code perturb >>>= PERTURBSHIFT}.
	 */
	private static final int PERTURBSHIFT = 5;

	/**
	 * Base capacity for a newly created table.
	 * <p>
	 * Must be a power of two because indexing uses {@code index = hash & mask}.
	 * </p>
	 */
	private static final int BASELENGTH = 8;

	/**
	 * Hash table indices array.
	 * <p>
	 * Each position stores either:
	 * <ul>
	 *   <li>{@link #DKIXEMPTY} for an empty bucket</li>
	 *   <li>{@link #DKIXDUMMY} for a deleted bucket (tombstone)</li>
	 *   <li>a non-negative integer that is an index into {@link #entries}</li>
	 * </ul>
	 * </p>
	 */
	private int[] indices;

	/**
	 * Bitmask for bucket selection.
	 * <p>
	 * Invariant: {@code mask == indices.length - 1} and {@code indices.length} is a power of two.
	 * </p>
	 */
	private int mask;

	/**
	 * Array storing the actual key/value pairs.
	 * <p>
	 * Entries are written sequentially using {@link #nextEntryIndex}.
	 * Buckets in {@link #indices} refer into this array.
	 * </p>
	 */
	private Entry<K, V>[] entries;

	/**
	 * Next free index in {@link #entries} where a newly inserted entry is written.
	 */
	private int nextEntryIndex = 0;

	/**
	 * Number of live items (non-deleted mappings) in the dictionary.
	 */
	private int nItems;

	/**
	 * Number of dummy/tombstone buckets in {@link #indices}.
	 */
	private int nDummies;

	/**
	 * Factory method similar to Python's {@code dict.fromkeys(keys, initialValue)}.
	 *
	 * @param keys the keys to insert
	 * @param initialValue the value to associate to every key
	 * @return a new {@code PyDict} with the provided keys mapped to {@code initialValue}
	 */
	public static <K, V> PyDict<K, V> fromKeys(K[] keys, V initialValue) {
		return new PyDict<>(keys, initialValue);
	}

	/**
	 * Factory method similar to Python's {@code dict.fromkeys(keys)} using {@code null} as default value.
	 *
	 * @param keys the keys to insert
	 * @return a new {@code PyDict} with the provided keys mapped to {@code null}
	 */
	public static <K, V> PyDict<K, V> fromKeys(K[] keys) {
		return fromKeys(keys, null);
	}

	/**
	 * Factory method similar to Python's {@code dict(k1=v1, k2=v2, ...)} but using varargs key/value pairs.
	 *
	 * @param args alternating key, value, key, value...
	 * @return a new {@code PyDict} containing all provided pairs
	 * @throws IllegalArgumentException if {@code args.length} is not even
	 */
	@SuppressWarnings("all")
	public static <K, V> PyDict<K, V> of(Object... args) {
		if(args.length % 2 != 0) throw new IllegalArgumentException("Args must be key, value pairs");

		PyDict raw = new PyDict();

		for(int i = 0; i < args.length; i += 2) {
			raw.put(args[i], args[i + 1]);
		}

		return raw;
	}

	/**
	 * Constructs an empty dictionary with {@link #BASELENGTH} capacity.
	 * <p>
	 * Capacity is a power of two to allow bucket selection via {@code index = hash & mask}.
	 * </p>
	 */
	public PyDict() {
		indices = new int[BASELENGTH];
		fill(indices, DKIXEMPTY);
		entries = new Entry[BASELENGTH];
		mask = BASELENGTH - 1;

		nItems = 0;
	}

	/**
	 * Constructs a dictionary from parallel key/value arrays.
	 * <p>
	 * This constructor allocates a table capacity based on {@code keys.length * 2}
	 * and then rounds it up to the next power of two to maintain the {@code hash & mask}
	 * invariant.
	 * </p>
	 *
	 * @param keys keys to insert
	 * @param values values to insert
	 * @throws IllegalArgumentException if lengths do not match or arrays are empty
	 */
	public PyDict(K[] keys, V[] values) {
		if(keys.length != values.length) throw new IllegalArgumentException("keys and values arrays lengths do not match");

		if(keys.length == 0) throw new IllegalArgumentException("Keys and Values must be size greater than zero");

		if(keys[0] instanceof Collection<?>) throw new IllegalArgumentException("Keys type must be inmutable");

		// Ensure power-of-two capacity (required because we use j & mask)
		int capacity = tableSizeFor(keys.length * 2);

		indices = new int[capacity];
		fill(indices, DKIXEMPTY);
		entries = new Entry[capacity];
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
	/**
	 * Constructs a dictionary containing all {@code keys} mapped to {@code initialValue}.
	 * <p>
	 * Allocates capacity based on {@code keys.length * 2} and rounds up to a power of two.
	 * </p>
	 *
	 * @param keys keys to insert
	 * @param initialValue default value for all keys
	 * @throws IllegalArgumentException if keys is empty or keys are considered mutable by the original check
	 */
	public PyDict(K[] keys, V initialValue) {
		if(keys.length == 0) throw new IllegalArgumentException("Keys array must contain at least one element");

		if(keys[0] instanceof Collection) throw new IllegalArgumentException("Keys must be inmutable");

		// Ensure power-of-two capacity (required because we use j & mask)
		int capacity = tableSizeFor(keys.length * 2);

		indices = new int[capacity];
		fill(indices, -1);
		entries = new Entry[capacity];

		mask = indices.length - 1;

		nItems = 0;

		for (K key : keys) {
			put(key, initialValue);
		}
	}

	/**
	 * Convenience constructor delegating to {@link #PyDict(Object[], Object)} with {@code null} default.
	 *
	 * @param keys keys to insert
	 */
	public PyDict(K[] keys){
		this(keys, (V[]) null);
	}

	/**
	 * Grows the internal arrays and reinserts existing entries.
	 * <p>
	 * The new capacity is computed from {@code entries.length * 2} and then normalized
	 * to a power of two (even though doubling a power-of-two remains a power-of-two,
	 * this keeps the invariant explicit and robust if the initial capacity was changed).
	 * </p>
	 */
	private void grow() {
		Entry<K, V>[] entr = new Entry[entries.length];
		System.arraycopy(entries, 0, entr, 0, entries.length);

		// Ensure power-of-two capacity after growth
		int newCapacity = tableSizeFor(entries.length * 2);

		entries = new Entry[newCapacity];
		indices = new int[newCapacity];

		fill(indices, DKIXEMPTY);

		mask = indices.length - 1;
		nItems = 0;
		nextEntryIndex = 0;
		nDummies = 0;

		for(Entry<K, V> entry : entr) {
			if(entry == null) continue;
			put(entry.key, entry.value);
		}
	}

	/**
	 * Fills an {@code int[]} array with a given initial value.
	 *
	 * @param arr array to fill
	 * @param initialValue value to assign to every index
	 */
	private void fill(int[] arr, int initialValue) {
		for(int i = 0; i < arr.length; i++) {
			arr[i] = initialValue;
		}
	}

	/**
	 * Returns a power-of-two table size that is {@code >= requested}.
	 * <p>
	 * This is required because bucket selection uses {@code index = j & mask}, where
	 * {@code mask = capacity - 1}. That technique only implements modulo arithmetic
	 * correctly when {@code capacity} is a power of two.
	 * </p>
	 *
	 * @param requested requested minimum capacity
	 * @return the smallest power of two that is {@code >= requested}, with a minimum of {@link #BASELENGTH}
	 */
	private int tableSizeFor(int requested) {
		int n = Math.max(BASELENGTH, requested);

		// Round up to next power of two.
		// If n is already a power of two, highestOneBit(n) == n.
		int highest = Integer.highestOneBit(n);
		return (highest == n) ? n : highest << 1;
	}

	/**
	 * Removes all entries from this dictionary while keeping current capacity.
	 */
	public void clear() {
		entries = new Entry[entries.length];
		indices = new int[indices.length];
		fill(indices, DKIXEMPTY);
		nItems = 0;
		nextEntryIndex = 0;
		nDummies = 0;
	}

	/**
	 * Creates a shallow copy of this dictionary by reinserting all live entries into a new {@code PyDict}.
	 *
	 * @return a new {@code PyDict} containing the same key/value mappings
	 */
	public PyDict<K, V> copy() {
		PyDict<K, V> copyDict = new PyDict<>();

		for(Entry<K, V> e : entries) {
			if(e == null) continue;
			copyDict.put(e.key, e.value);
		}

		return copyDict;
	}

	/**
	 * Inserts or updates a key/value mapping.
	 * <p>
	 * Uses open addressing with perturbation-based probing.
	 * </p>
	 *
	 * @param key key to insert
	 * @param value value to associate to {@code key}
	 */
	public void put(K key, V value) {
		if((nItems * 100) / indices.length >= EXPANSIONFACTOR || nextEntryIndex >= entries.length) {
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
					nDummies--;
					break;
				}
			}

			if(indices[index] != DKIXDUMMY && entries[indices[index]] != null && entries[indices[index]].key.equals(key)) {
				entries[indices[index]] = new Entry<>(key, value, key.hashCode());
				return;
			}

			//unsigned shift
			perturb >>>= PERTURBSHIFT;
			j = (5 * j) + 1 + perturb;
			index = j & mask;
		}

		int entryIndex = nextEntryIndex++;
		indices[index] = entryIndex;
		entries[entryIndex] = new Entry<>(key, value, key.hashCode());
		nItems++;
	}

	/**
	 * Retrieves a value for the given key, or {@code null} if not present.
	 *
	 * @param key key to look up
	 * @return the associated value, or {@code null} if missing
	 */
	public V get(K key) {
		if(nItems == 0) return null;

		int perturb = key.hashCode();
		int j = perturb;
		int index = j & mask;

		while(true) {
			if(indices[index] == DKIXEMPTY) return null;

			if(indices[index] != DKIXDUMMY && entries[indices[index]] != null && entries[indices[index]].key.equals(key)) {
				return entries[indices[index]].value;
			}

			perturb >>>= PERTURBSHIFT;
			j = (5 * j) + 1 + perturb;
			index = j & mask;
		}
	}

	/**
	 * Returns the number of live key/value mappings in the dictionary.
	 *
	 * @return number of items
	 */
	public int size() {
		return nItems;
	}

	/**
	 * Removes the specified key and returns its value.
	 *
	 * @param key key to remove
	 * @return the removed value
	 * @throws IllegalArgumentException if the key is not present
	 */
	public V pop(K key) {
		if(nItems == 0) throw new IllegalArgumentException("Key %s not found".formatted(key));

		int perturb = key.hashCode();
		int j = perturb;
		int index = j & mask;

		while(true) {
			if(indices[index] == DKIXEMPTY) throw new IllegalArgumentException("Key %s not found".formatted(key));

			if(indices[index] >= 0 && entries[indices[index]] != null && entries[indices[index]].key.equals(key)) {
				break;
			}

			perturb >>>= PERTURBSHIFT;
			j = (5 * j) + 1 + perturb;
			index = j & mask;
		}

		V val = entries[indices[index]].value;

		entries[indices[index]] = null;
		indices[index] = DKIXDUMMY;

		nItems--;
		nDummies++;

		rebuild();

		return val;
	}

	/**
	 * Removes the specified key and returns its value, or {@code defaultValue} if missing.
	 *
	 * @param key key to remove
	 * @param defaultValue value returned if the key is missing
	 * @return removed value or {@code defaultValue} if missing
	 */
	public Object pop(K key, Object defaultValue) {
		if(nItems == 0) return defaultValue;

		int perturb = key.hashCode();
		int j = perturb;
		int index = j & mask;

		while(true) {
			if(indices[index] == DKIXEMPTY) return defaultValue;

			if(indices[index] >= 0 && entries[indices[index]] != null && entries[indices[index]].key.equals(key)) {
				break;
			}

			perturb >>>= PERTURBSHIFT;
			j = (5 * j) + 1 + perturb;
			index = j & mask;
		}

		V val = entries[indices[index]].value;

		entries[indices[index]] = null;
		indices[index] = DKIXDUMMY;

		nItems--;
		nDummies++;

		rebuild();

		return val;
	}

	/**
	 * Rebuilds the table if the number of dummy slots exceeds the number of live items.
	 * <p>
	 * This compacts out tombstones by reinserting live entries into a fresh table.
	 * </p>
	 */
	private void rebuild() {
		if(nDummies > nItems) {
			Entry<K, V>[] entriesBuffer = entries;

			clear();

			for(Entry<K, V> e : entriesBuffer) {
				if(e == null) continue;

				put(e.key, e.value);
			}
		}
	}

	/**
	 * Removes and returns an arbitrary (currently: last inserted non-null in {@link #entries} by your logic)
	 * key/value pair as a {@link Pair}.
	 *
	 * @return a removed key/value pair
	 * @throws IndexOutOfBoundsException if the dictionary is empty
	 */
	public Pair popItem() {
		if(nItems == 0) throw new IndexOutOfBoundsException("PyDict is empty!");

		int i = nextEntryIndex - 1;

		Entry e = entries[i];

		while(i >= 0 && entries[i] == null) {
			i--;
		}

		if(i < 0) throw new IndexOutOfBoundsException("PyDict is empty");

		Pair p = new Pair(entries[i].key, entries[i].value);
		pop(entries[i].key);

		return p;
	}

	/**
	 * Updates this dictionary with all items from {@code otherDict}.
	 *
	 * @param otherDict dictionary providing updates
	 */
	public void update(PyDict<K, V> otherDict) {
		for(Pair<K, V> pair : otherDict.items()) {
			put(pair.key, pair.value);
		}
	}

	/**
	 * Determines if the dictionary contains specified key or not
	 *
	 * @param key object to see if it is contained or not
	 * @return if said key is contained within the dictionary or not
	 */
	public boolean containsKey(K key) {
		if(nItems == 0) return false;

		int perturb = key.hashCode();
		int j = perturb;
		int index = j & mask;

		while(true) {
			if(indices[index] == DKIXEMPTY) return false;

			if(indices[index] != DKIXDUMMY && entries[indices[index]] != null && entries[indices[index]].key.equals(key)) {
				return true;
			}

			perturb >>>= PERTURBSHIFT;
			j = (5 * j) + 1 + perturb;
			index = j & mask;
		}
	}

	/**
	 * If {@code key} is missing, insert {@code defaultValue}. Returns the current value for {@code key}.
	 *
	 * @param key key to look up
	 * @param defaultValue value to insert if missing
	 * @return the existing or inserted value
	 */
	public V setDefault(K key, V defaultValue) {
		V val = get(key);

		if(val == null && !containsKey(key)) {
			put(key, defaultValue);
			return defaultValue;
		}

		return val;
	}

	/**
	 * Returns all keys currently present (in the order of {@link #entries} traversal).
	 *
	 * @return an {@code Object[]} of keys
	 */
	public Object[] keys() {
		return Stream.of(entries)
				.filter(Objects::nonNull)
				.map(Entry::key)
				.toArray();
	}

	/**
	 * Returns all keys using the provided array constructor (List-like {@code toArray} style).
	 *
	 * @param arrayProvider function producing an array of the desired runtime type
	 * @return an array of keys
	 */
	public K[] keys(IntFunction<K[]> arrayProvider) {
		return Stream.of(entries)
				.filter(Objects::nonNull)
				.map(Entry::key)
				.toArray(arrayProvider);
	}

	/**
	 * Returns all values currently present (in the order of {@link #entries} traversal).
	 *
	 * @return an {@code Object[]} of values
	 */
	public Object[] values() {
		return Stream.of(entries)
				.filter(Objects::nonNull)
				.map(Entry::value)
				.toArray(Object[]::new);
	}

	/**
	 * Returns all values using the provided array constructor (List-like {@code toArray} style).
	 *
	 * @param arrayProvider function producing an array of the desired runtime type
	 * @return an array of values
	 */
	public V[] values(IntFunction<V[]> arrayProvider) {
		return Stream.of(entries)
				.filter(Objects::nonNull)
				.map(Entry::value)
				.toArray(arrayProvider);
	}

	/**
	 * Returns all key/value mappings as an array of {@link Pair}.
	 *
	 * @return array of key/value pairs
	 */
	public Pair<K, V>[] items() {
		return (Pair<K, V>[]) Stream.of(entries)
				.filter(Objects::nonNull)
				.map(e -> new Pair<>(e.key, e.value))
				.toArray(Pair[]::new);
	}

	/**
	 * Returns a string representation in a Python-like mapping format.
	 *
	 * @return string representation
	 */
	@Override
	public String toString() {
		if(size() == 0) return "{}";

		StringBuilder repr = new StringBuilder();

		repr.append("{");

		for(int i = 0; i < entries.length; i++) {
			if(entries[i] == null) continue;

			repr.append(entries[i].toString());

			repr.append(", ");
		}

		repr.delete(repr.lastIndexOf(", "), repr.length());
		repr.append("}");

		return repr.toString();
	}

	/**
	 * Internal record storing a key/value mapping and cached hash.
	 *
	 * @param key key
	 * @param value value
	 * @param hash cached hash of key
	 */
	private record Entry<K, V>(K key, V value, int hash) {

		public String toString() {
			return "%s: %s".formatted(key, value);
		}
	}

	/**
	 * Public-facing key/value pair record used by {@link #items()} and {@link #popItem()}.
	 *
	 * @param key key
	 * @param value value
	 */
	public record Pair<K, V>(K key, V value) {

		public String toString() {
			return "[%s, %s]".formatted(key, value);
		}
	}
}