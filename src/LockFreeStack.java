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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class LockFreeStack<T> {


    private AtomicReference<Node> head;
    private AtomicInteger numOps;
    private static final int MIN_SLEEP = 2; // min sleep time in ms
    private static final int MAX_SLEEP = 20000; // max sleep time in ms



    class Node<T>{
        public T val;
        public Node next;

        public Node(T val){
            this.val = val;
            next = null;

        }
    }

    public LockFreeStack(){
       this.numOps = new AtomicInteger(0);
       this.head = new AtomicReference<Node>(null);
    }



    public static void main(String args[]){
        LockFreeStack<Integer> LFS = new LockFreeStack<Integer>();

        LFS.push(10);

        System.out.println(LFS.pop());


    }




    /**
     * Exponential backoff. take peramiter n, how many times thread has had to back off
     * sleeps for (MIN_SLEEP^n) + small rand milliseconds
    * */
    private void backoff(int n){
        int sleepMillSec = (int)(Math.pow(MIN_SLEEP,n) + Math.random()*10);
        if (sleepMillSec > MAX_SLEEP){
            sleepMillSec = MAX_SLEEP;
        }

        try{
            Thread.sleep(sleepMillSec);
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();  // set interrupt flag
            System.out.println("Wait interrupted.");
        }


    }

    protected boolean tryPush(Node n){
        Node oldHead = head.get();
        n.next = oldHead;
        return head.compareAndSet(oldHead,n);
    }

    public void push(T p){

        Node n = new Node(p);
        int tryCount = 0;
        while(true){
            if(tryPush(n)){
                // push successful
                numOps.getAndIncrement();
                return;
            }else{
                backoff(tryCount++);
            }
        }

    }

    protected Node tryPop()throws EmptyStackException{

        Node oldHead = head.get();

        if (oldHead == null){
            //lock.unlock();
            throw new EmptyStackException();
        }

        Node newHead = oldHead.next;

        if(head.compareAndSet(oldHead,newHead)){
            return oldHead;
        }else{
            return null;
        }
    }

    public T pop() throws EmptyStackException {

        int tryCount = 0;
        while(true){
            Node returnNode = tryPop();
            if (returnNode != null){
                numOps.getAndIncrement();
                return (T)returnNode.val;
            }else{
                backoff(tryCount++);
            }
        }


    }

    public int getNumOps(){
        return numOps.get();
    }



}
