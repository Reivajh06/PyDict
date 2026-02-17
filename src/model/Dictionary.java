package model;

import java.util.Arrays;
import java.util.Objects;

public class Dictionary<K, V> {

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

	public Dictionary() {
		indices = new int[BASELENGTH];
		Arrays.fill(indices, -1);
		entries = new Entry[BASELENGTH];
		mask = BASELENGTH - 1;

		nItems = 0;
	}

	private void grow() {
		Entry<K, V>[] entr = Arrays.copyOf(entries, entries.length);

		entries = new Entry[entries.length * 2];
		indices = new int[indices.length * 2];
		Arrays.fill(indices, -1);
		mask = indices.length - 1;
		nItems = 0;
		nextEntryIndex = 0;

		for(Entry<K, V> entry : entr) {
			if(entry == null) continue;
			put(entry.key, entry.value);
		}
	}

	public void put(K key, V value) {
		if((nItems * 100) / indices.length >= EXPANSIONFACTOR) {
			grow();
		}

		//Remember to include a reference to the first dummy found,
		//Finding a dummy may not lead to insertion as said key might already exists

		int perturb = key.hashCode();
		int j = perturb;
		int index = j & mask;

		while(indices[index] != DKIXDUMMY && indices[index] != DKIXEMPTY) {
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

	public K[] keys() {
		return (K[]) Arrays.stream(entries)
				.filter(Objects::nonNull)
				.map(Entry::key)
				.toArray(Object[]::new);
	}

	public V[] values() {
		return (V[]) Arrays.stream(entries)
				.filter(Objects::nonNull)
				.map(Entry::value)
				.toArray(Object[]::new);
	}

	record Entry<K, V>(K key, V value, int hash) {
	}
}
