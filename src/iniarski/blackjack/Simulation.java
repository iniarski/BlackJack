package iniarski.blackjack;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Simulation
{
    protected int nOfSimulations;
    protected int handsPlayed;

    public Simulation()
    {
        nOfSimulations = 2;
        handsPlayed = 40;
    }

    protected int[][] moneyMatrix;

    public void simulate()
    {
        Game[] games = new Game[nOfSimulations];
        Thread[] gameThreads = new Thread[nOfSimulations];

        for (int i = 0; i < nOfSimulations; i++)
        {
            games[i] = new Game();
            gameThreads[i] = new Thread(games[i]);
            gameThreads[i].start();
        }

        moneyMatrix = new int[nOfSimulations][handsPlayed];

        for (int i = 0; i < nOfSimulations; i++) {
            try {
                gameThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            moneyMatrix[i] = games[i].getMoneyHistogram();
        }



        for (int i = 0; i < nOfSimulations; i++)
        {
            System.out.println(Arrays.toString(games[i].getMoneyHistogram()));
        }

        System.out.println(Arrays.toString(moneyMatrix));
    }

    public static void main(String[] args)
    {
        Simulation sim = new Simulation();
        sim.simulate();
    }
}
