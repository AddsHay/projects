/** Class that prints the Collatz sequence starting from a given number.
 *  @author Adrian Haynes
 */
public class Collatz {
    public static int nextNumber(int n) {
        if (n == 1)
            return 0;
        if (n % 2 == 0)
            return n / 2;
        if (n % 2 == 1)
            return (n * 3) + 1;
        return n;
    }
    public static void main(String[] args) {
        int n = 5;
        while (n != 0) {
            System.out.print(n + " ");
            n = nextNumber(n);
        }
    }
}

