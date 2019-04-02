import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EliminationArray<T> {

    private static final int DURATION = 100; // how avoid magic number?
    LockFreeExchanger<T>[] exchanger;
    Random random;

    public EliminationArray(int capacity){
        exchanger = (LockFreeExchanger<T>[]) new LockFreeExchanger[capacity];
        // init the exchanger
        for(int i = 0; i< capacity; i++){
            exchanger[i] = new LockFreeExchanger<T>();
        }
        random = new Random();
    }

    public T visit(T value, int range) throws TimeoutException{
        int slot = random.nextInt(range+1);
        return (exchanger[slot].exchange(value,DURATION, TimeUnit.MILLISECONDS));
    }

}
