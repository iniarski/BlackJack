package iniarski.blackjack;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Simulation
{
    protected int nOfSimulations;
    protected int handsPlayed;
    protected int maxConcurrentGames;

    public Simulation()
    {
        nOfSimulations = 5;
        handsPlayed = 40;
        maxConcurrentGames = 8;
    }

    protected int[][] moneyMatrix;

    public void simulate()
    {
        Game[] games = new Game[nOfSimulations];
        ExecutorService gamePool = Executors.newFixedThreadPool(maxConcurrentGames);

        for (int i = 0; i < nOfSimulations; i++)
        {
            games[i] = new Game(i);
            gamePool.submit(games[i]);
        }

        gamePool.shutdown();

        try {
            gamePool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        float average = 0;
        for (int i = 0; i < nOfSimulations; i++)
        {
            System.out.println(Arrays.toString(games[i].getMoneyHistogram()));
            average += games[i].getMoneyHistogram()[handsPlayed-1];
        }

        average /= nOfSimulations;

        System.out.println("Average result " + average);

    }

    public static void main(String[] args)
    {
        Simulation sim = new Simulation();
        sim.simulate();
    }
}
