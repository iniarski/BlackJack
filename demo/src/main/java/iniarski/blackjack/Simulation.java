package iniarski.blackjack;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;



public class Simulation
{
    protected int nOfSimulations;
    protected int handsPlayed;
    protected int maxConcurrentGames;

    public Simulation()
    {
        
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

        Stage stage = new Stage();
        final NumberAxis xAxis = new NumberAxis(0,handsPlayed,1);
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Hands PLayed");
        yAxis.setLabel("Money");

        LineChart lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setPrefHeight(1080);
        lineChart.setPrefWidth(1920);
        
        
        for(int i = 0; i<nOfSimulations; i++)
        {
            XYChart.Series series = new XYChart.Series<>();
            int iter = 0;
            for(int j = 0; j<handsPlayed; j++)
            {
                series.getData().add(new XYChart.Data<>(iter,games[i].getMoneyHistogram()[j]));
                iter++;
            }
            lineChart.getData().add(series);
            
        }

        

        Group root = new Group(lineChart);
        Scene scene = new Scene(root,1920,1080);
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args)
    {
        Simulation sim = new Simulation();
        sim.simulate();

    }

    
}