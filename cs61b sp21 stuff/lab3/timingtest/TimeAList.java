package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
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
        timeAListConstruction();
    }

    public static void timeAListConstruction() {

        AList<Integer> Nslist = new AList<>(0);
        AList<Integer> oplist = new AList<>(0);
        AList<Double> Tlist = new AList<>(0.00);
        for (int N=1000; N <= 128000; N *= 2) {
            AList<Integer> Newlist = new AList<>(0);
            Stopwatch sp = new Stopwatch();
            for (int x=0; x <= N; x++) {
               Newlist.addLast(x);
            }
            double timetaken = sp.elapsedTime();
            Tlist.addLast(timetaken);
            Nslist.addLast(N);
            oplist.addLast(N);
        }
        printTimingTable(Nslist, Tlist, oplist);
    }
}
