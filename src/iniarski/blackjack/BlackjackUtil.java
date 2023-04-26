package iniarski.blackjack;

// class for handling miscellaneous tasks in the project

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class BlackjackUtil {

    // NEGLIGIBLE_THRESHOLD determines when probability is so small that case can be not considered
    public static final float NEGLIGIBLE_THRESHOLD = 0.0005f;
    public static final byte MAX_RECURSIONS = 3;
    private static final BlackjackUtil instance = new BlackjackUtil();
    private final byte[][] scoreMatrix = new byte[10][10];


    private BlackjackUtil() {
        // populating scoreMatrix with score representing hand composed of 2 cards represented by the indexes

        for (int i = 0; i < 10; i++) {
            // soft totals (with ace)

            scoreMatrix[0][i] = (byte) (i + 12);
            scoreMatrix[i][0] = scoreMatrix[0][i];
        }

        // hard totals
        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < 10; j++) {
                scoreMatrix[i][j] = (byte) (i + j + 2);
            }
        }

    }

    public static BlackjackUtil getInstance() {
        return instance;
    }

    public byte calculateScore(byte[] cardRanks) {

        // works for cards represented as
        // 0 : Ace
        // 1 - 8 : cards from 2 to 9
        // 9 : 10, J, Q and K

        byte score = 0;
        byte hasAce = 1;

        for (byte n : cardRanks) {

            score += n + 1;

            // Since ace is stored as a 0 multiplying all elements of array will produce 0 only when there's an ace
            hasAce = (byte) (hasAce * n);
        }

        // this way there's only one comparison in the function call

        if (hasAce == 11 && score <= 11) {
            score += 10;
        }

        return score;
    }

    public float[] getDealerScoreProbabilities(byte dealerCard, short[] cardsInDeck)
    {
        final float[] dealerScoreProbabilities = new float[6];
        // this array stores information of how likely is the dealer to end the game on a score
        // it is to be interpreted as :
        // [0] - 17 points
        // [1] - 18 points
        // [2] - 19 points
        // [3] - 20 points
        // [4] - 21 points
        // [5] - over 21 points (BUST)

        Arrays.fill(dealerScoreProbabilities, 0.0f);

        calculateDealerProbabilities(dealerCard, cardsInDeck, dealerScoreProbabilities);

        return dealerScoreProbabilities;
    }

    public void calculateDealerProbabilities(byte dealerCard, short[] cardsInDeck, float[] dealerScoreProbabilities) {


        // calculating probabilities of cards to come up
        short numberOfCardsLeft = 0;

        for (short n : cardsInDeck) {
            numberOfCardsLeft += n;
        }

        float[] cardProbabilities = new float[10];

        for (int i = 0; i < 10; i++) {
            cardProbabilities[i] = (float) cardsInDeck[i] / (float) numberOfCardsLeft;
        }

        // computing in parallel for any possible pair of cards

        CountDownLatch latch = new CountDownLatch(10);
        for (byte i = 0; i < 10; i++) {
            {
                byte finalI = i;
                Thread thread = new Thread(() -> {

                    // Checking for edge case where there are no cards of this rank left
                    if (cardsInDeck[finalI] == 0) { // if so - skip the procedure
                        latch.countDown();
                        return;
                    }

                    // making new dealer's hand
                    byte[] newHand = {dealerCard, finalI};
                    byte tempScore = calculateScore(newHand);

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
                    short[] newDeck = cardsInDeck.clone();
                    newDeck[finalI]--;
                    calculatePossibleDealerHands(newHand, newDeck, cardProbabilities[finalI], dealerScoreProbabilities);

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

    private void calculatePossibleDealerHands(byte[] dealerCards, short[] cardsInDeck,
                                              float caseProbability, float[] dealerScoreProbabilities) {

        int numberOfCardsLeft = 0;

        for (short n : cardsInDeck) {
            numberOfCardsLeft += n;
        }

        float[] cardProbabilities = new float[10];

        for (byte i = 0; i < 10; i++) {
            cardProbabilities[i] = (float) cardsInDeck[i] / (float) numberOfCardsLeft;
        }

        // analyzing all possible cases recursively in parallel
        CountDownLatch latch = new CountDownLatch(10);

        for (byte i = 0; i < 10; i++) {

                // checking for edge case - if there are no cards of such rank left in deck
                if (cardsInDeck[i] == 0) {
                    latch.countDown();
                    continue;
                }

                // making new hand (by adding new card of specified index and checking if dealer still has play)
                byte[] newHand = Arrays.copyOf(dealerCards, dealerCards.length + 1);
                newHand[newHand.length - 1] = i;
                int tempScore = calculateScore(newHand);

                if (tempScore >= 17) { // - condition for dealer to stand
                    if (tempScore > 21) { // dealer goes bust
                        dealerScoreProbabilities[5] += caseProbability * cardProbabilities[i];
                    } else { // dealer stands
                        dealerScoreProbabilities[tempScore - 17] += caseProbability * cardProbabilities[i];
                    }
                    latch.countDown();
                    continue;
                }

                float futureCaseProbability = cardProbabilities[i] * caseProbability;

                // checking if recursive call is reasonable
                if (futureCaseProbability < NEGLIGIBLE_THRESHOLD) {
                    latch.countDown();
                    continue;
                }

                // executes if dealer hits - recursive call
                short[] newDeck = cardsInDeck.clone();
                newDeck[i]--;

                Thread recursiveCallThread = new Thread(() -> {
                    calculatePossibleDealerHands(newHand, newDeck, futureCaseProbability, dealerScoreProbabilities);

                    latch.countDown();
                });

                recursiveCallThread.start();
        }

        // waiting for threads to finish
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public float calculatePlayerWinningChances(short[] cardsLeft) {

        // TODO: fix betting
        // idk why but the calculated probability is usually to high and not consistent
        // something to do wit threading, i guess

        short tempNOfCardsLeft = 0;

        for (short n : cardsLeft) {
            tempNOfCardsLeft += n;
        }

        final short nOfCardsLeft = tempNOfCardsLeft;

        float[][] winProbMatrix = new float[10][10];

        float[] dealerScoreProbabilities = new float[6];
        Arrays.fill(dealerScoreProbabilities, 0.0f);

        calculatePossibleDealerHands(new byte[0], cardsLeft, 1f, dealerScoreProbabilities);

        Thread softTotalsThread = new Thread(() -> {
            for (byte i = 0; i < 10; i++) {
                byte[] newHand = {0, i};

                short[] newDeck = cardsLeft.clone();
                newDeck[0]--;
                newDeck[i]--;

                winProbMatrix[0][i] = calculateHitWinProbability(newHand, cardsLeft, (short) (nOfCardsLeft - 2),
                        (byte) 0, dealerScoreProbabilities);
                winProbMatrix[i][0] = winProbMatrix[0][i];
            }
        });

        softTotalsThread.start();

        Thread hardTotalsThread = new Thread(() -> {
            // iterating over possible hard totals
            for (byte i = 4; i < 21; i++) {
                float scoreWinningProbability = 0f;
                boolean scoreIndexNotFound = true;

                for (byte j = 1; j < 10; j++) {
                    for (byte k = 1; k < 10; k++) {
                        if (scoreMatrix[j][k] == i) {
                            if (scoreIndexNotFound) {
                                byte[] newHand = {j, k};
                                short[] newDeck = cardsLeft.clone();

                                newDeck[j]--;
                                newDeck[k]--;

                                scoreWinningProbability = calculateHitWinProbability(newHand, newDeck,
                                        (short) (nOfCardsLeft - 2), (byte) 0, dealerScoreProbabilities);

                                winProbMatrix[j][k] = scoreWinningProbability;
                                scoreIndexNotFound = false;
                            } else {
                                winProbMatrix[j][k] = scoreWinningProbability;
                            }
                        }
                    }
                }

            }
        });

        hardTotalsThread.start();



        float[][] CardProbMatrix = new float[10][10];
        for (byte i = 0; i < 10; i++) {
            for (byte j = (byte) (i + 1); j < 10; j++) {
                // calculating the upper half of the matrix
                CardProbMatrix[i][j] = (float) (cardsLeft[i] * cardsLeft[j])
                        / (float) (tempNOfCardsLeft * (tempNOfCardsLeft - 1));
                // that is identical to the bottom half of the matrix;
                CardProbMatrix[j][i] = CardProbMatrix[i][j];
            }
        }

        // now the diagonal
        for (byte i = 0; i < 10; i++) {
            CardProbMatrix[i][i] = (float) (cardsLeft[i] * (cardsLeft[i] - 1)) /
                    (float) (tempNOfCardsLeft * (tempNOfCardsLeft - 1));
        }

        try {
            softTotalsThread.join();
            hardTotalsThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        float playerWinningProb = 0.0f;

        for (byte i = 0; i < 10; i++) {
            for (byte j = 0; j < 10; j++) {
                playerWinningProb += CardProbMatrix[i][j] * winProbMatrix[i][j];
            }
        }

        System.out.println(playerWinningProb);

        return playerWinningProb;
    }

    public float omega2WinningChances(short[] cardsLeft){
        int nOfCardsLeft = 0;


        int runningCount = 0;
        int[] omegaValues = {0, 1, 1, 2, 2, 2, 1, 0, -1, -2};

        for (int i = 0; i < 10; i++) {
            nOfCardsLeft += cardsLeft[i];

            // calculating running count
            // number of cards of a rank left * value in omega 2 system
            // negative because we are counting cards left in the deck, not cards dealt
            runningCount -= omegaValues[i] * cardsLeft[i];
        }

        float decksLeft = nOfCardsLeft / 52f;


        float trueCount = runningCount/decksLeft;

        float playersEdge = (trueCount - 1f) / 2f;

        float winningChances = (50f + playersEdge) / 100f;

        return winningChances;
    }
    public float calculateHitWinProbability(byte[] cardsInHand, short[] cardsInDeck, short cardsLeft,
                                            byte recursionNumber, float[] dealerScoreProbabilities) {

        //calculating probabilities of getting cards
        float[] cardProbabilities = new float[10];

        for (int i = 0; i < 10; i++) {
            cardProbabilities[i] = (float) cardsInDeck[i] / (float) cardsLeft;
        }

        AtomicReference<Float> winProb = new AtomicReference<>(0.0f);

        CountDownLatch latch = new CountDownLatch(10);

        for (byte i = 0; i < 10; i++) {

                // end if no cards of rank left
                if (cardsInDeck[i] <= 0){
                    latch.countDown();
                    continue;
                }

                byte[] newHand = Arrays.copyOf(cardsInHand, cardsInHand.length + 1);
                newHand[newHand.length - 1] = i;

                int tempScore = calculateScore(newHand);

                // bust
                if (tempScore > 21) {
                    //winProb.set(0.0f);
                    latch.countDown();
                    continue;
                }

                float standNowWinProbability = 0.0f;

                for (int j = 0; j < tempScore - 17 ; j++) {
                    standNowWinProbability += dealerScoreProbabilities[j];
                }

                standNowWinProbability += dealerScoreProbabilities[5];
                //standNowWinProbability += tempScore >= 17 ? dealerScoreProbabilities[tempScore - 17] / 2.0f : 0.0f;

                short[] newDeck = Arrays.copyOf(cardsInDeck, cardsInDeck.length);
                newDeck[i]--;
                final float finalStandWinProbability = standNowWinProbability;
                final byte finalI = i;

                Thread recursiveCallThread = new Thread(() -> {
                    float hitMoreWinProbability;

                    if (recursionNumber == MAX_RECURSIONS - 1) {
                        hitMoreWinProbability = finalStandWinProbability;
                    } else {
                        hitMoreWinProbability =
                                calculateHitWinProbability(newHand, newDeck, (short) (cardsLeft - 1),
                                        (byte) (recursionNumber + 1), dealerScoreProbabilities);
                    }


                    winProb.set(winProb.get() +
                            cardProbabilities[finalI] * finalStandWinProbability > hitMoreWinProbability ?
                            finalStandWinProbability : hitMoreWinProbability);


                    latch.countDown();
                });

                recursiveCallThread.start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return  winProb.get();
    }

}

