import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicStampedReference;

public class LockFreeExchanger<T> {
    /**
     * Written by Ross Wagner following "The Art of Multiprocessor Programing" ch 11
     * */
    static final int EMPTY=0,WAITING=1,BUSY=2;
    AtomicStampedReference<T> slot = new AtomicStampedReference<T>(null,0);

    public T exchange(T myItem, long timeout, TimeUnit unit) throws TimeoutException {
        long nanos = unit.toNanos(timeout);
        long timeBound = System.nanoTime() + nanos;
        int[] stampHolder = {EMPTY};

        while(true){
            // check for timeout
            if(System.nanoTime() > timeBound){
                throw new TimeoutException();
            }

            T yrItem = slot.get(stampHolder);
            int stamp = stampHolder[0];
            switch (stamp){
                case EMPTY:
                    // try to occupy slot until timeout
                    if(slot.compareAndSet(yrItem,myItem,EMPTY,WAITING)){
                        // spin until timeout or matching exchange
                        while(System.nanoTime()< timeBound){
                            yrItem = slot.get(stampHolder);
                            if (stampHolder[0]==BUSY){
                                // match found
                                // reset slot
                                slot.set(null,EMPTY);
                                // return other item
                                return yrItem;
                            }
                        }
                        if(slot.compareAndSet(myItem,null,WAITING,EMPTY)){
                            // timeout
                            throw new TimeoutException();
                        }else{
                            // a match is found
                            yrItem = slot.get(stampHolder);
                            slot.set(null,EMPTY);
                            return yrItem;
                        }
                    }
                    break;

                case WAITING: // 1 item in slot waiting for pair
                    if(slot.compareAndSet(yrItem,myItem,WAITING,BUSY)){
                        // swap value of slot, change state
                        return yrItem;
                    }
                    break;

                case BUSY: // pending exchange try again later
                    break;

                default: // impossible

            }
        }
    }
}
