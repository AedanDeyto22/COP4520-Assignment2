// Aedan Gilbert D. Deyto
// Project 2 Problem 1 for COP4520
// 3/4/2022

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.*;
import java.io.*;

public class CupcakeLabyrinth
{
    public ArrayList<Guest> guestList;
    public int guestAmount;
    public boolean canRun;
    public AtomicBoolean allDone;
    public AtomicBoolean canChoose;

    public static void main(String [] args)
    {
        CupcakeLabyrinth host = new CupcakeLabyrinth();
        Labyrinth labyrinth = new Labyrinth();
        Random rand = new Random();
        host.guestList = new ArrayList<Guest>();
        int counter = 0;
        int enterAmount = 0;
        host.guestAmount = 100;
        host.canRun = true;
        host.allDone = new AtomicBoolean(false);
        host.canChoose = new AtomicBoolean(true);
        long start = System.nanoTime();

        // Create all guest and choose guest 1 as the one that notifies the Minotour
        for (int i = 0; i < host.guestAmount; i++)
        {
            if (counter == i)
            {
                Guest temp = new Guest(i, true, false, labyrinth, host);
                host.guestList.add(temp);
            }
            else
            {
                Guest temp = new Guest(i, false, false, labyrinth, host);
                host.guestList.add(temp);
            }

            Thread th = new Thread(host.guestList.get(i));
            th.start();
        }

        // Will keep choosing a guest to enter the labyrinth when it can.
        while(host.canRun == true)
        {
            if (host.allDone.get() == true)
            {
                break;
            }

            // When it can choose a guets it will randomly choose and set itself to false
            // so it can't choose another guest until the thread that it choose says it can
            if (host.canChoose.get() == true)
            {
                enterAmount++;
                host.canChoose.set(false);
                int chosenGuest = rand.nextInt(host.guestAmount);
                host.guestList.get(chosenGuest).enter = true;
            }
        }

        System.out.println("Everyone Has Gotten a Cupcake!");

        long end = System.nanoTime();
        long exectution = end - start;
        double convert = exectution / 1000000000;

        System.out.println("Execution Time: " + convert + " Seconds");
        System.out.println("Guest Amount: " + host.guestAmount);
        System.out.println("The amount of times a guest entered the labyrinth is: " + enterAmount);
    }
}
