package iniarski.blackjack;

// class for handling miscellaneous tasks in the project

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class BlackjackUtil {

    // NEGLIGIBLE_THRESHOLD determines when probability is so small that case can be not considered
    public static final double NEGLIGIBLE_THRESHOLD = 0.0005;
    private static final BlackjackUtil instance = new BlackjackUtil();

    private final double[] dealerScoreProbabilities = new double[6];
    // this field stores information of how likely is the dealer to end the game on a score
    // it is to be interpreted as :
    // [0] - 17 points
    // [1] - 18 points
    // [2] - 19 points
    // [3] - 20 points
    // [4] - 21 points
    // [5] - over 21 points (BUST)

    private BlackjackUtil() {
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

        for (int n : cardRanks) {
            if (n == 0) {
                hasAce = true;
            }

            score += n + 1;
        }

        if (hasAce && score <= 11) {
            score += 10;
        }

        return score;
    }

    public double[] getDealerScoreProbabilities() {
        return dealerScoreProbabilities;
    }

    public void calculateDealerProbabilities(int dealerCard, int[] cardsInDeck) {

        // clearing array
        Arrays.fill(dealerScoreProbabilities, 0.0);

        // calculating probabilities of cards to come up
        int numberOfCardsLeft = Arrays.stream(cardsInDeck).sum();

        double[] cardProbabilities = new double[10];

        for (int i = 0; i < 10; i++) {
            cardProbabilities[i] = (double) cardsInDeck[i] / (double) numberOfCardsLeft;
        }

        // computing in parallel for any possible pair of cards

        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            {
                int finalI = i;
                Thread thread = new Thread(() -> {

                    // Checking for edge case where there are no cards of this rank left
                    if (cardsInDeck[finalI] == 0) { // if so - skip the procedure
                        latch.countDown();
                        return;
                    }

                    // making new dealer's hand
                    int[] newHand = {dealerCard, finalI};
                    int tempScore = calculateScore(newHand);

                    // checking if the dealer is at least 17 points
                    // NOTE : it is impossible to bust with only 2 cards (max score with 2 cards is 21)
                    if (tempScore >= 17) { // if score is at least 17 dealer will stand

                        if (tempScore > 21) { // dealer goes bust
                            dealerScoreProbabilities[5] += cardProbabilities[finalI];
                        } else {
                            dealerScoreProbabilities[tempScore - 17] += cardProbabilities[finalI];
                        }
                        latch.countDown();
                        return;
                    }

                    // executes if dealer hits
                    int[] newDeck = cardsInDeck.clone();
                    newDeck[finalI]--;
                    calculatePossibleDealerHands(newHand, newDeck, cardProbabilities[finalI]);

                    latch.countDown();

                });

                thread.start();
            }

        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void calculatePossibleDealerHands(int[] dealerCards, int[] cardsInDeck, double caseProbability) {

        int numberOfCardsLeft = Arrays.stream(cardsInDeck).sum();

        double[] cardProbabilities = new double[10];

        for (int i = 0; i < 10; i++) {
            cardProbabilities[i] = (double) cardsInDeck[i] / (double) numberOfCardsLeft;
        }

        // analyzing all possible cases recursively in parallel
        CountDownLatch latch = new CountDownLatch(10);

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
                    if (tempScore > 21) { // dealer goes bust
                        dealerScoreProbabilities[5] += caseProbability * cardProbabilities[finalI];
                    } else { // dealer stands
                        dealerScoreProbabilities[tempScore - 17] += caseProbability * cardProbabilities[finalI];
                    }
                    latch.countDown();
                    return;
                }

                double futureCaseProbability = cardProbabilities[finalI] * caseProbability;

                // checking if recursive call is reasonable
                if (futureCaseProbability < NEGLIGIBLE_THRESHOLD) {
                    latch.countDown();
                    return;
                }

                // executes if dealer hits - recursive call
                int[] newDeck = cardsInDeck.clone();
                newDeck[finalI]--;
                calculatePossibleDealerHands(newHand, newDeck, futureCaseProbability);

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
    }

    public double calculatePlayerWinningChances(int[] cardsLeft) {

        // !NOTE : this method resets dealerScoreProbabilities
        // run calculateDealersScoreProbabilities after using this method

        int nOfCardsLeft = 0;

        for (int n : cardsLeft) {
            nOfCardsLeft += n;
        }

        double[][] winProbMatrix = new double[10][10];

        double[] cardProbabilities = new double[10];

        for (int i = 0; i < 10; i++) {
            cardProbabilities[i] = (double) cardsLeft[i] / (double) nOfCardsLeft;
        }

        Arrays.fill(dealerScoreProbabilities, 0.0);

        calculatePossibleDealerHands(new int[]{}, cardsLeft, 1.0);

        CountDownLatch latch = new CountDownLatch(55);

        for (int i = 0; i < 10; i++) {
            for (int j = i; j < 10; j++) {
                int finalI = i; int finalJ = j;
                Thread thread = new Thread(() -> {
                    double standWinProb = 0.0;

                    int tempScore = calculateScore(new int[]{finalI, finalJ});

                    for (int k = 0; k < tempScore - 17; k++) {
                        standWinProb += dealerScoreProbabilities[k];
                    }
                    standWinProb += dealerScoreProbabilities[5];

                    double oneHitWinProb = 0.0;

                    for (int k = 0; k < 10; k++) {
                        int inLoopScore = calculateScore(new int[]{finalI, finalJ, k});
                        // breaking the loop whe player goes bust
                        if (inLoopScore > 21) {
                            k = 16;
                            continue;
                        }
                        double inLoopWinProb = 0.0;
                        for (int l = 0; l < inLoopScore - 17; l++) {
                            inLoopWinProb += dealerScoreProbabilities[l];
                        }
                        inLoopWinProb += dealerScoreProbabilities[5];
                        oneHitWinProb += inLoopWinProb * cardProbabilities[k];
                    }

                    winProbMatrix[finalI][finalJ] = Math.max(standWinProb, oneHitWinProb);

                    latch.countDown();
                });

                thread.start();
            }
        }

        double[][] CardProbMatrix = new double[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = i + 1; j < 10; j++) {
                // calculating the upper half of the matrix
                CardProbMatrix[i][j] = (double) (cardsLeft[i] * cardsLeft[j])
                        / (double) (nOfCardsLeft * (nOfCardsLeft - 1));
                // that is identical to the bottom half of the matrix;
                CardProbMatrix[j][i] = CardProbMatrix[i][j];
            }
        }

        // now the diagonal
        for (int i = 0; i < 10; i++) {
            CardProbMatrix[i][i] = (double) (cardsLeft[i] * (cardsLeft[i] - 1)) /
                    (double) (nOfCardsLeft * (nOfCardsLeft - 1));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double playerWinningProb = 0.0;

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                playerWinningProb += CardProbMatrix[i][j] * winProbMatrix[i][j];
            }
        }

        return playerWinningProb;
    }
}

