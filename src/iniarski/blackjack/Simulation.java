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
        ExecutorService executorService = Executors.newFixedThreadPool(nOfSimulations);

        for (int i = 0; i < nOfSimulations; i++)
        {
            games[i] = new Game();
            executorService.execute(games[i]);
        }

        executorService.shutdown();

        for (int i = 0; i < nOfSimulations; i++)
        {
            System.out.println(Arrays.toString(games[i].getMoneyHistogram()));
        }
    }

    public static void main(String[] args)
    {
        Simulation sim = new Simulation();
        sim.simulate();
    }
}
