/**
 *
 * @Author: Ross Wagner
 * 2/3/2019
 *
 * Implementation of a lock free stack using exponential back off
 * Influenced by The example in the book in chapter 11
 *
 * The linearization point of the push operation  is when compareAndSet is called in try push.
 * The push takes effect when compareAndSet is called. The push operation can be placed in a
 * serial history according to when compareAndSet returns true.
 *
 * The linearization point of the pop operation is when the compareAndSet instruction is called.
 * The pop takes effect when compareAndSet is called. The pop operation can be placed in a
 * serial history according to when compareAndSet returns true meaning that the old head has been replaced
 * with new head.
 *
 * The lack of locks in this implementation guarantees that at least one method call finishes in a finite number
 * of steps. I use exponential backoff instead a queuing structure so fairness is not guaranted.
 *
 * */


import java.util.EmptyStackException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class EliminationBackoffStack<T> extends LockFreeStack<T>{

    static final int capacity = 10; // Magic number
    EliminationArray<T> eliminationArray = new EliminationArray<T>(capacity);
    static ThreadLocal<RangePolicy> policy = new ThreadLocal<>(){
        protected synchronized RangePolicy initialValue(){
            return new RangePolicy(capacity);
        }
    };


    public static void main(String[] args){



        // threads

    }

    @Override
    public void push(T value){
        RangePolicy rangePolicy = policy.get();
        Node<T> node = new Node<T>(value);
        while(true){
            if (tryPush(node)){
                // successful push
                numOps.getAndIncrement();
                return;
            } else try{
                T otherValue = eliminationArray.visit(value, rangePolicy.getRange());

                if (otherValue == null){
                    // matching pop found
                    // record elem success
                    rangePolicy.recordEleminationSuccess();
                    numOps.getAndIncrement();
                    return;

                }
            } catch (TimeoutException ex){
                // record timeout
                rangePolicy.recordEliminationTimeout();
            }

        }
    }

    @Override
    public T pop() throws EmptyStackException{
        RangePolicy rangePolicy = policy.get();
        while (true){
            Node<T> returnNode = tryPop();
            if (returnNode != null){
                numOps.getAndIncrement();
                return returnNode.val;
            }else try {
                T otherValue =eliminationArray.visit(null,rangePolicy.getRange());
                if (otherValue != null){
                    // successful elimination
                    numOps.getAndIncrement();
                    rangePolicy.recordEleminationSuccess();
                    return otherValue;
                }
            }catch(TimeoutException ex){
                // timeout
                rangePolicy.recordEliminationTimeout();
            }

        }
    }



}
