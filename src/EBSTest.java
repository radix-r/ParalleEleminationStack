import org.jfree.ui.RefineryUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EBSTest {

    static EliminationBackoffStack<String> stringEBS;
    static Test t;

    public static void main(String[] args) {
        // stringEBS = new EliminationBackoffStack<>();

        Test t = new Test();

        t.runTests();
    }

    public static class Test {



        public void runTests() {
            try {
                List<long[]> data = new ArrayList<>();



                // test from 0 to 32 threads
                for (int i = 1; i < 32; i++) {

                    // init new Backoff stack
                    stringEBS = new EliminationBackoffStack<>();


                    ExecutorService pool = Executors.newFixedThreadPool(i);

                    // start timer
                    long startTime = System.nanoTime();


                    // run 15 pushes, 15 pops, and 2 numOps
                    for (int j = 0; j < 15; j++) {
                        pool.execute(new Push(String.format("push%d", i), Integer.toString(i), 100));
                        pool.execute(new Pop(String.format("Pop%d", i), 100));

                    }

                    for (int k = 0; k < 2; k++) {
                        pool.execute(new NumOps(String.format("numOps%d", i), 100));
                    }

                    pool.awaitTermination(1, TimeUnit.MINUTES);


                    long endTime = System.nanoTime();

                    long duration = (endTime - startTime)/1000000;  //divided by 1000000 to get milliseconds.

                    data.add(new long[]{duration,(long)i});
                }








                String title = String.format("Time for %d Operations 15/32 push, 15/32 pop, 2/32 numOps",32*1000);
                LineChart_AWT lineChart= new LineChart_AWT("Line Graph",title, data);
                lineChart.pack();
                RefineryUtilities.centerFrameOnScreen(lineChart);
                lineChart.setVisible(true);

            }catch(InterruptedException ex){
                ex.printStackTrace();
            }
        }


    }

    static class NumOps implements Runnable {

        private String name;
        private int n;

        public NumOps(String name, int n) {

            this.n = n;
            this.name = name;
        }

        private void NumOpsNTimes(int n) {
            for (int i = 0; i < n; i++) {
                System.out.println(stringEBS.getNumOps());
            }
        }

        public void run() {
            NumOpsNTimes(n);
        }

    }

    static class Push implements Runnable {

        private String data;
        private String name;
        private int n;

        public Push(String name, String data, int n) {
            this.data = data;
            this.n = n;
            this.name = name;
        }

        private void pushNTimes(String data, int n) {
            for (int i = 0; i < n; i++) {
                stringEBS.push(data);
            }
        }

        public void run() {
            pushNTimes(data, n);
        }

    }

    static class Pop implements Runnable {

        private String name;
        private int n;

        public Pop(String name, int n) {

            this.n = n;
            this.name = name;
        }

        private void popNTimes(int n) {
            for (int i = 0; i < n; i++) {
                System.out.println(stringEBS.pop());
            }
        }

        public void run() {
            popNTimes(n);
        }

    }
}