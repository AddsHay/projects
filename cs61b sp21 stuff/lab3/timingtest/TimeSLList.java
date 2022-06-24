package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        AList<Integer> oplist = new AList<>(0);
        AList<Double> Tlist = new AList<>(0.00);
        AList<Integer> bNlist = new AList<>(0);
        for (int N=1000; N <= 128000; N *= 2) {
            SLList<Integer> Nlist = new SLList<>(0);
            for (int x=0; x <= N; x++) {
                Nlist.addLast(x);
            }
            Stopwatch sp = new Stopwatch();
            int M = 0;
            while (M < 10000) {
                Nlist.getLast();
                M += 1;
            }
            double timetaken = sp.elapsedTime();
            Tlist.addLast(timetaken);
            bNlist.addLast(N);
            oplist.addLast(10000);
        }
        printTimingTable(bNlist, Tlist, oplist);
    }

}
