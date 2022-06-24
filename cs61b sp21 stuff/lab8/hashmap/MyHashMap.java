package hashmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author Adrian Haynes
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int initialSize;
    private double loadFactor;
    private int size;
    private HashSet<K> keys;

    /** Constructors */
    public MyHashMap() {
        initialSize = 16;
        loadFactor = .75;
        size = 0;
        buckets = createTable(initialSize);
        keys = new HashSet<>();
    }

    public MyHashMap(int initialSize) {
        initialSize = initialSize;
        loadFactor = .75;
        size = 0;
        buckets = createTable(initialSize);
        keys = new HashSet<>();
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        initialSize = initialSize;
        loadFactor = maxLoad;
        size = 0;
        buckets = createTable(initialSize);
        keys = new HashSet<>();
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) { return new Node(key, value); }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() { return new java.util.LinkedList<>(); }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    private int hash(K key, int length) {
        int x = key.hashCode();
        x = java.lang.Math.floorMod(x, length);
        return x;
    }
    public void clear() {
       buckets = createTable(initialSize);
       size = 0;
       keys = new HashSet<>();
    }

    @Override
    public boolean containsKey(K key) { return keys.contains(key); }

    private Node getnode(K key) {
        int index = hash(key, buckets.length);
        for (Node n: buckets[index]) {
            if (n.key.equals(key)) {
                return n;
            }
        }
        return null;
    }

    @Override
    public V get(K key) {
        if (containsKey(key)) {
            return getnode(key).value;
        }
        return null;
    }

    private void resize(int newsize) {
        Collection<Node>[] newbuckets = createTable(newsize);
        for (K key: keys) {
            int index = hash(key, newsize);
            if (newbuckets[index] == null) {
                newbuckets[index] = createBucket();
            }
            newbuckets[index].add(getnode(key));
        }
        buckets = newbuckets;
    }

    @Override
    public void put(K key, V value) {
        if (containsKey(key)) {
            getnode(key).value = value;
        } else {
            if ((double) size / buckets.length > loadFactor) {
                resize(buckets.length * 2);
            }
            int index = hash(key, buckets.length);
            if (buckets[index] == null) {
                buckets[index] = createBucket();
            }
            keys.add(key);
            buckets[index].add(createNode(key, value));
            size += 1;
        }

    }

    @Override
    public Set<K> keySet(){ return keys; }

    @Override
    public int size() { return size; }

    @Override
    public Iterator<K> iterator() { return keys.iterator(); }

    @Override
    public V remove(K key) { throw new UnsupportedOperationException(); }

    @Override
    public V remove(K key, V value) { throw new UnsupportedOperationException(); }

}
