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

        // works for cards represented as
        // 0 : Ace
        // 1 - 8 : cards from 2 to 9
        // 9 : 10, J, Q and K

        int score = 0;
        boolean hasAce = false;

        for(int n : cardRanks) {
            if(n == 0) {
                hasAce = true;
            }

            score += n + 1;
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

                // making new hand (by adding new card of specified index and checking if dealer still has play

                int[] newHand = Arrays.copyOf(dealerCards, dealerCards.length + 1);
                newHand[newHand.length - 1] = finalI;

                int tempScore = calculateScore(newHand);

                if (tempScore >= 17) { // - condition for dealer to stand

                    if (tempScore <= 21 && tempScore >= scoreToBeat) { // dealer win condition
                        atomicWinProb.set(atomicWinProb.get() + cardProbabilities[finalI]);
                    } // else - dealer loses
                    // increase dealer win probability by 0 (do nothing)

                    latch.countDown();
                    return;
                }

                int[] newDeck = cardsInDeck.clone();
                newDeck[finalI]--;


                // recursive call
                // only when dealer has play to make
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
