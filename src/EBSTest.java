import org.jfree.ui.RefineryUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EBSTest {

    static EliminationBackoffStack<String> stringEBS;
    static LockFreeStack<String> stringLFS;
    static Test t;
    static final int TIMESTOAVG = 100;
    static final int TEST1AMT = 100;
    static final boolean DEBUG = false;

    public static void main(String[] args) {
        // stringEBS = new EliminationBackoffStack<>();

        Test t = new Test();

        t.runTests();
    }

    public static class Test {



        public void runTests() {
            //try {
                List<long[]> data = new ArrayList<>();



                // test from 0 to 32 threads
                for (int i = 1; i < 32; i++) {

                    long duration = 0;
                    long endTime;
                    long startTime;
                    ExecutorService pool;
                    // re init new Backoff stack
                    for (int rep = 0; rep < TIMESTOAVG; rep++) {
                        stringEBS = new EliminationBackoffStack<>();


                        // time EBS
                        pool = Executors.newFixedThreadPool(i);

                        // start timer
                        startTime = System.nanoTime();

                        // run 15 pushes, 15 pops, and 2 numOps
                        for (int j = 0; j < 15; j++) {
                            pool.execute(new Push(stringEBS, String.format("push%d", j), Integer.toString(j), TEST1AMT));
                            pool.execute(new Pop(stringEBS, String.format("pop%d", j), TEST1AMT));

                        }

                        for (int k = 0; k < 2; k++) {
                            pool.execute(new NumOps(stringEBS, String.format("numOps%d", k), TEST1AMT));
                        }

                        pool.shutdown();
                        //pool.awaitTermination(1, TimeUnit.MINUTES);

                        endTime = System.nanoTime();

                        duration += (endTime - startTime) ;  //nano seconds.

                    }

                    long avg = (duration / TIMESTOAVG) ;

                    data.add(new long[]{avg,0,(long)i});

                    // time lfs
                    duration = 0;
                    for (int rep = 0; rep < TIMESTOAVG; rep++) {
                        stringLFS = new LockFreeStack<>();
                        pool = Executors.newFixedThreadPool(i);

                        // start timer
                        startTime = System.nanoTime();

                        // run 15 pushes, 15 pops, and 2 numOps
                        for (int j = 0; j < 15; j++) {
                            pool.execute(new Push(stringLFS, String.format("push%d", j), Integer.toString(j), TEST1AMT));
                            pool.execute(new Pop(stringLFS, String.format("Pop%d", j), TEST1AMT));

                        }

                        for (int k = 0; k < 2; k++) {
                            pool.execute(new NumOps(stringLFS, String.format("numOps%d", k), TEST1AMT));
                        }

                        pool.shutdown();
                        //pool.awaitTermination(1, TimeUnit.MINUTES);

                        endTime = System.nanoTime();

                        duration += (endTime - startTime) ;  //nano seconds.

                    }

                    avg = (duration / TIMESTOAVG) ; // s
                    data.add(new long[]{avg,1,(long)i});

                }




                String title = String.format("Time for %d Operations 15/32 push, 15/32 pop, 2/32 numOps",32*100);
                MultipleLinesChart lineChart= new MultipleLinesChart("Line Graph",title, data);
                lineChart.pack();
                RefineryUtilities.centerFrameOnScreen(lineChart);
                lineChart.setVisible(true);

           /* }catch(InterruptedException ex){
                ex.printStackTrace();
            }*/
        }


    }

    static class NumOps implements Runnable {

        private String name;
        private int n;
        private LockFreeStack<String> stack;

        public NumOps(LockFreeStack stack,String name, int n) {

            this.stack = stack;
            this.n = n;
            this.name = name;
        }

        private void NumOpsNTimes(int n) {
            for (int i = 0; i < n; i++) {
                int out  = stack.getNumOps();
                if(DEBUG) {
                    System.out.println("NumOps: " + Integer.toString(out));
                }
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
        private LockFreeStack<String> stack;

        public Push(LockFreeStack<String > stack,String name, String data, int n) {
            this.data = data;
            this.n = n;
            this.name = name;
            this.stack = stack;
        }

        private void pushNTimes(String data, int n) {
            for (int i = 0; i < n; i++) {
                stack.push(data);
            }
        }

        public void run() {
            pushNTimes(data, n);
        }

    }

    static class Pop implements Runnable {

        private String name;
        private int n;
        private LockFreeStack<String> stack;

        public Pop(LockFreeStack<String> stack,String name, int n) {

            this.n = n;
            this.name = name;
            this.stack = stack;
        }

        private void popNTimes(int n) {
            for (int i = 0; i < n; i++) {
                String out = stack.pop();
                if (DEBUG) {
                    System.out.println("Pop: " + out);
                }
            }
        }

        public void run() {
            popNTimes(n);
        }

    }
}