package bstmap;

import java.util.Iterator;
import java.util.Set;



public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private int Mapsize;
    private K key;
    private V value;
    private BSTMap<K, V> prev;
    private BSTMap<K, V> next;

    public BSTMap() {
        Mapsize = 0;
        key = null;
        value = null;
        prev = null;
        next = null;
    }

    private BSTMap(K k, V v) {
        Mapsize = 1;
        key = k;
        value = v;
        prev = null;
        next = null;
    }

    public int size() {
        return Mapsize;
    }

    public V get(K k) {
        if (Mapsize == 0) {
            return null;
        }
        if (k.equals(key)) {
            return value;
        }
        if ((int) k.compareTo(key) < 0) {
            return prev.get(k);
        }
        return next.get(k);
    }

    public boolean containsKey(K k) {
        if (Mapsize == 0) {
            return false;
        }
        if (k.equals(key)) {
            return true;
        }
        if (k.compareTo(this.key) < 0) {
            return prev.containsKey(k);
        }
        return next.containsKey(k);
    }

    public void put(K k, V v) {
        if (Mapsize == 0) {
            key = k;
            value = v;
            Mapsize++;
        }
        if (k.equals(key)) {
            value = v;
        } else if (k.compareTo(key) < 0) {
            if (prev == null) {
                prev = new BSTMap(k, v);
                Mapsize++;
            } else {
                int s = prev.size();
                prev.put(k, v);
                if (prev.size() > s) {
                    Mapsize++;
                }
            }
        } else {
            if (next == null) {
                next = new BSTMap(k, v);
                Mapsize++;
            } else {
                int s2 = next.size();
                next.put(k, v);
                if (next.size() > s2) {
                    Mapsize++;
                }
            }
        }
    }

    public void printInOrder() {
        if (Mapsize == 0) {
            return;
        }

        if (prev != null) {
            prev.printInOrder();
        }
        System.out.print(key);
        if (next != null) {
            next.printInOrder();
        }
    }

    public void clear() {
        Mapsize = 0;
        key = null;
        value = null;
        prev = null;
        next = null;
    }

    public V remove(K k) {
        throw new UnsupportedOperationException();
    }

    public V remove(K k, V v) {
        throw new UnsupportedOperationException();
    }

    public Set<K> keySet() { throw new UnsupportedOperationException(); }

    @Override
    public Iterator<K> iterator() { throw new UnsupportedOperationException(); }
}