package iniarski.blackjack;

// class for handling miscellaneous tasks in the project

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class BlackjackUtil {

    private static final BlackjackUtil instance = new BlackjackUtil();
    private BlackjackUtil(){
    }

    public static BlackjackUtil getInstance() {
        return instance;
    }

    public int calculateScore(int[] cardRanks) {

        // method for calculating score
        // works for both cards description in the form of int[10] and int[13]

        int score = 0;
        boolean hasAce = false;

        for(int n : cardRanks) {
            if(n == 0) {
                hasAce = true;
            }

            if (n < 10){
                score += n + 1;
            } else {
                score += 10;
            }
        }

        if(hasAce && score <= 11) {
            score += 10;
        }

        return score;
    }

    public double calculateDealerChances(int[] dealerCards, int[] cardsInDeck, int scoreToBeat) {

        int currentScore = calculateScore(dealerCards);

        // if dealer has more than 1 card
        if (dealerCards.length > 1) {
            // check if score is equal or greater than 17 - condition for dealer to stand
            if (currentScore >= 17) {
                // in this case dealer stands
                if (currentScore >= scoreToBeat && currentScore <= 21) { // dealer win condition
                    return 1.0;
                }
                // else - dealer loses
                return 0.0;
            }
        }

        // This executes only if dealer has 1 card or is under 17 points

        int numberOfCardsLeft = Arrays.stream(cardsInDeck).sum();

        double[] cardProbabilities = new double[10];

        for (int i = 0; i < 10; i++) {
            cardProbabilities[i] = (double) cardsInDeck[i] / (double) numberOfCardsLeft;
        }

        // analyzing all possible cases recursively in parallel
        CountDownLatch latch = new CountDownLatch(10);

        AtomicReference<Double> atomicWinProb = new AtomicReference<>((double) 0);

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {

                // checking for edge case - if there are no cards of such rank left in deck

                if (cardsInDeck[finalI] == 0) {
                    latch.countDown();
                    return;
                }

                int[] newDeck = cardsInDeck.clone();
                newDeck[finalI]--;

                int[] newHand = Arrays.copyOf(dealerCards, dealerCards.length + 1);
                newHand[newHand.length - 1] = finalI;

                // recursive call
                atomicWinProb.set(calculateDealerChances(newHand, newDeck, scoreToBeat) * cardProbabilities[finalI] +
                        atomicWinProb.get());

                latch.countDown();
            });

            thread.start();
        }

        // waiting for threads to finish
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return atomicWinProb.get();
    }
}
