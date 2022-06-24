package IntList;

import static org.junit.Assert.*;
import org.junit.Test;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSimple() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testSquarePrimesone() {
        IntList lst = IntList.of(1, 4, 5, 7, 11, 12);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("1 -> 4 -> 25 -> 49 -> 121 -> 12", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testSquarePrimestwo() {
        IntList lst = IntList.of(1, 4, 6, 20, 14, 12, 51);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("1 -> 4 -> 6 -> 20 -> 14 -> 12 -> 51", lst.toString());
        assertFalse(changed);
    }
}
