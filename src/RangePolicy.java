import java.util.Random;

/**
 *
 *
 *
 * */
public class RangePolicy {

    private int max;
    private int range; // size range to search. Grows with more contension
    private Random rand;

    public RangePolicy(int max){
        this.max = max;
        range = 0;
        rand = new Random();
    }


    public int getRange(){
        // want it to return 0 to range
        return rand.nextInt(range+1);
    }

    public void recordEleminationSuccess(){
        // high contention. grow range
        if (range < max){
            range++;
        }
    }

    public void recordEliminationTimeout(){
        // low contention shrink range
        if (range > 0){
            range--;
        }
    }
}
