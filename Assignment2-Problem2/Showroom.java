import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import java.util.*;
import java.io.*;

class QNode
{
    volatile boolean canEnter = false;
    volatile QNode next = null;
}

class Guest implements Runnable
{
    public AtomicReference<QNode> tail;
    public ThreadLocal<QNode> myNode;
    Showroom vase;
    public int name;
    boolean canRun;
    boolean inQueue;
    public int counter;
    public int maxQueue = 3;

    // The constructer sets the all the global variable for the thread
    public Guest(AtomicReference<QNode> tail, Showroom vase, int name)
    {
        this.canRun = true;
        this.inQueue = false;
        this.counter = 0;
        this.name = name;
        this.vase = vase;
        this.tail = tail;
        this.myNode = new ThreadLocal<QNode>()
        {
            protected QNode initialValue()
            {
                return new QNode();
            }
        };
    }

    public void enterQueue()
    {
        // Gets the node for the thread.
        // Gets the address of the tail and sets the current node as the new tail of the queue.
        QNode qnode = myNode.get();
        QNode pred = tail.getAndSet(qnode);

        // If there was a predicesor to the current node then tell that qnode its can't enter yet
        // and set the predicesor next to this node, and wait until given access.
        // If there is no predicesor then you can enter the crystal vase room
        if (pred != null)
        {
            qnode.canEnter = true;
            pred.next = qnode;

            while (qnode.canEnter)
            {
            }
        }
    }

    public void informNext()
    {
        // Gets the node address of the thread
        QNode qnode = myNode.get();

        // See's if its at the end of the queue
        if (qnode.next == null)
        {
            // If there is no more nodes coming in then we are done with the Queue
            // and just return.
            if (tail.compareAndSet(qnode, null) == true)
            {
                return;
            }

            // If there is another node entering the Queue, then wait for it
            while (qnode.next == null)
            {}
        }

        // Tells the next thread that it can enter the Crysal vase room
        // Sets the threads queue next to null to show that its exited the Queue.
        qnode.next.canEnter = false;
        qnode.next = null;
    }

    @Override
    public void run()
    {
        while (canRun == true)
        {
            // If the thread is not in the Queue and it hasn't reach its limit
            // on the amount of times it can reQueue, then try to Queue in.
            if (inQueue == false && counter < maxQueue)
            {
                // The threds flips a coin to see if it wants to enter the queue
                Random gen = new Random();
                int decision = gen.nextInt(2);

                // If it does then it set itself that its queue and increments
                // the amount of time is queued in. And enters the Queue.
                if (decision == 0)
                {
                    this.inQueue = true;
                    this.counter++;
                    enterQueue();
                }
            }

            else
            {
                // When the thread is queued in and has enter the room it looks
                // and them leaves.

                //  Removed for the fastest runtime for large threads, but
                // if want to simulate with smaller threads then you can uncomment Thread.sleep().
                /*try
                {
                    Thread.sleep(10);
                }
                catch (Exception error)
                {
                    error.printStackTrace();
                }*/

                // When it leaves it informs the next in Queue and tells itself
                // thats its not queued in anymore.
                informNext();
                inQueue = false;
            }

            // If the thread is no longer queued and has queued in the max amount of time then it can stop itself
            if (inQueue == false && counter >= maxQueue)
            {
                canRun = false;
            }
        }
    }
}

public class Showroom
{
    public static void main(String [] args)
    {
        boolean canRun = true;
        AtomicReference<QNode> tail = new AtomicReference<QNode>(null);
        ExecutorService executor = Executors.newFixedThreadPool(100);
        Showroom vase = new Showroom();
        int threadAmount = 100;
        long start = System.nanoTime();

        // Creates all the threads to represent the amount of guest in the program
        // Sends the guest the tail of the Queue that is Atomic and shared between them
        for (int i = 0; i < threadAmount; i++)
        {
            executor.submit(new Guest(tail, vase, i));
        }

        executor.shutdown();

        try
        {
            if (executor.awaitTermination(1000000, TimeUnit.SECONDS) == true)
            {
                long end = System.nanoTime();
                long exectution = end - start;
                double convert = exectution / 1000000;

                System.out.println("Execution Time: " + convert + " Milliseconds");
            }
        }
        catch (InterruptedException error)
        {
            error.printStackTrace();
        }
    }
}
