package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] array;
    private int arraysize;
    private int defaultsize = 8;
    private int firstspot;
    private int lastspot;

    public ArrayDeque() {
        arraysize = 0;
        firstspot = 0;
        lastspot = 1;
        array = (T[]) new Object[defaultsize];
    }

    public int size() {
        return arraysize;
    }

    @Override
    public void printDeque() {
        int place = (firstspot + 1) % array.length;
        for (int x = 0; x < arraysize; x += 1) {
            System.out.print(array[place] + " ");
            place = (place + 1) % array.length;
        }
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= arraysize) {
            return null;
        }
        int oldfirst = firstspot + 1;
        while (index > 0) {
            index -= 1;
            oldfirst += 1;
        }
        return array[oldfirst % array.length];
    }

    private void resize(int newlength) {
        T[] newarray = (T[]) new Object[newlength];
        int firstindex = newarray.length / 2 - arraysize / 2;
        int newfirstindex = firstindex;
        int oldfirstindex = firstspot + 1;
        int x = 0;
        while (x < arraysize) {
            newarray[newfirstindex] = array[oldfirstindex % array.length];
            x += 1;
            oldfirstindex += 1;
            newfirstindex += 1;
        }
        array = newarray;
        firstspot = firstindex - 1;
        lastspot = newfirstindex;
    }

    private void smallersize() {
        resize(Math.max(arraysize * 2, defaultsize));
    }

    @Override
    public void addFirst(T item) {
        if (arraysize == array.length) {
            resize(array.length * 2);
        }
        arraysize += 1;
        array[firstspot] = item;
        firstspot = (firstspot + array.length - 1) % array.length;
    }

    @Override
    public void addLast(T item) {
        if (arraysize == array.length) {
            resize(array.length * 2);
        }
        array[lastspot] = item;
        lastspot = (lastspot + 1) % array.length;
        arraysize += 1;
    }

    @Override
    public T removeFirst() {
        if (arraysize == 0) {
            return null;
        }
        if ((double) arraysize / array.length < .25 && array.length > defaultsize) {
            smallersize();
        }
        firstspot = (firstspot + 1) % array.length;
        T removedfirst = array[firstspot];
        array[firstspot] = null;
        arraysize -= 1;
        return removedfirst;
    }

    @Override
    public T removeLast() {
        if (arraysize == 0) {
            return null;
        }
        if ((double) arraysize / array.length < .25 && array.length > defaultsize) {
            smallersize();
        }
        lastspot = (lastspot - 1 + array.length) % array.length;
        T removedlast = array[lastspot];
        array[lastspot] = null;
        arraysize -= 1;
        return removedlast;
    }

    public boolean equals(Object o) {
        if (o instanceof Deque && ((Deque) o).size() == this.size()) {
            int count = 0;
            for (int x = 0; x < ((Deque) o).size(); x++) {
                if (((Deque) o).get(x).equals(this.get(x))) {
                    count += 1;
                }
            }
            return count == ((Deque) o).size();
        }
        return false;
    }


    public Iterator<T> iterator() {
        return new ArrayIterator();
    }

    private class ArrayIterator implements Iterator<T> {
        private int currpos;
        ArrayIterator() {
            currpos = 0;
        }

        public boolean hasNext() {
            return currpos < arraysize;
        }

        public T next() {
            T output = get(currpos);
            currpos += 1;
            return output;
        }
    }
}
