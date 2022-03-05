import java.util.concurrent.atomic.AtomicBoolean;
import java.util.*;
import java.io.*;

public class Guest implements Runnable
{
    public int num;
    public int count;
    public boolean isCounter;
    public boolean gotCupcake;
    public boolean canRun;
    public AtomicBoolean enter;
    public Labyrinth labyrinth;
    public CupcakeLabyrinth host;

    Guest(int num, Labyrinth labyrinth, CupcakeLabyrinth host)
    {
        this.num = num;
        this.gotCupcake = false;
        this.labyrinth = labyrinth;
        this.host = host;
        enter = new AtomicBoolean(false);
        canRun = true;
        this.count = 0;
    }

    @Override
    public void run()
    {
        while(canRun == true)
        {
            // If its chosen to enter the Labyrinth it will
            if (enter.get() == true)
            {
                // If its chosen to be the counter then it has a specific task to do.
                if (host.counter == num)
                {
                    // When it enters the labyrinth if they see the Cupcake gone it means sone has entered
                    // and will increments it count
                    if (labyrinth.hasCupcake == false)
                    {
                        count++;
                        labyrinth.hasCupcake = true;
                    }

                    // If its count + 1 is equal to the guest then notify the host (Minotour) that everyone has entered the Labyrinth
                    if (host.guestAmount == (count + 1))
                    {
                        host.allDone.set(true);
                    }
                }

                // The task of the guest that aren't the counter
                else
                {
                    // If they haven't gotten the cupcake then get one
                    // If the cupcake is not taken yet then take it, to indicate to the counter
                    // that a new unique person has entered the labyrinth.
                    // If the cupcake has been already taken then just leave the plate empty and move on
                    if (gotCupcake == false && labyrinth.hasCupcake == true)
                    {
                        gotCupcake = true;
                        labyrinth.hasCupcake = false;
                    }
                }

                this.enter.set(false);
                host.canChoose.set(true);
            } // End of Active Thread

            else
            {
                try
                {
                    Thread.sleep(1);
                }
                catch (Exception error)
                {
                    error.printStackTrace();
                }
            }

            if (host.allDone.get() == true)
            {
                canRun = false;
            }
        } // End of While Loop
    } // End Of Run
}
