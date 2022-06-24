package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private int listsize;

    /** Node containing the current item, and pointing to the former and next entries in the list
     *
     */
    private class LNode {
        LNode former;
        LNode next;
        T curr;
        LNode(T curr, LNode former, LNode next) {
            this.curr = curr;
            this.former = former;
            this.next = next;
        }
    }
    private final LNode lsentinel;

    public LinkedListDeque() {
        listsize = 0;
        lsentinel = new LNode(null, null, null);
        lsentinel.former = lsentinel;
        lsentinel.next = lsentinel;
    }

    @Override
    public void addFirst(T item) {
        LNode first = new LNode(item, lsentinel, lsentinel.next);
        lsentinel.next.former = first;
        lsentinel.next = first;
        listsize += 1;
    }

    @Override
    public void addLast(T item) {
        LNode last = new LNode(item, lsentinel.former, lsentinel);
        lsentinel.former.next = last;
        lsentinel.former = last;
        listsize += 1;
    }

    @Override
    public int size() {
        return listsize;
    }

    @Override
    public void printDeque() {
        LNode currentprint = lsentinel.next;
        while (currentprint != lsentinel) {
            System.out.println(currentprint.curr + " ");
            currentprint = currentprint.next;
        }
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T first  = lsentinel.next.curr;
        LNode newfirst = lsentinel.next.next;
        newfirst.former = lsentinel;
        lsentinel.next = newfirst;
        listsize -= 1;
        return first;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        T last = lsentinel.former.curr;
        LNode newlast = lsentinel.former.former;
        newlast.next = lsentinel;
        lsentinel.former = newlast;
        listsize -= 1;
        return last;
    }

    @Override
    public T get(int index) {
        if (listsize == 0 || index >= listsize && index < 0) {
            return null;
        }
        int currentindex = 0;
        LNode currentnode = lsentinel.next;
        while (currentindex < index) {
            currentnode = currentnode.next;
            currentindex += 1;
        }
        return currentnode.curr;
    }

    public T getRecursive(int index) {
        if (listsize == 0 || index >= listsize && index < 0) {
            return null;
        }
        return getfunc(lsentinel.next, index);
    }

    private T getfunc(LNode l, int index) {
        if (index == 0) {
            return l.curr;
        }
        return getfunc(l.next, index - 1);
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
        return new LinkedListIterator();
    }

    private class LinkedListIterator implements Iterator<T> {
        private int currpos;
        LinkedListIterator() {
            currpos = 0;
        }

        public boolean hasNext() {
            return currpos < listsize;
        }

        public T next() {
            T output = get(currpos);
            currpos += 1;
            return output;
        }
    }
}
