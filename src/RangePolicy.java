import java.util.Random;

/**
 *
 *
 *
 * */
public class RangePolicy {

    private int max;
    private Random rand;

    public RangePolicy(int max){
        this.max = max;
        rand = new Random();
    }


    public int getRange(){
        // want it to return 0 to max
        return rand.nextInt(max+1);
    }
}
