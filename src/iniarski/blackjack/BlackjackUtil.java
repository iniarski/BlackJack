package iniarski.blackjack;

// class for handling miscellaneous tasks in the project

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class BlackjackUtil {

    // NEGLIGIBLE_THRESHOLD determines when probability is so small that case can be not considered
    public static final float NEGLIGIBLE_THRESHOLD = 0.0001f;
    public static final byte MAX_RECURSIONS = 3;
    private static final BlackjackUtil instance = new BlackjackUtil();


    private BlackjackUtil() {
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


        CountDownLatch winProbCalculationLatch = new CountDownLatch(55);

        for (byte i = 0; i < 10; i++) {
            for (byte j = i; j < 10; j++) {
                byte finalI = i; byte finalJ = j;
                    float standWinProb = 0.0f;

                    byte tempScore = calculateScore(new byte[]{finalI, finalJ});

                    for (byte k = 0; k < tempScore - 17; k++) {
                        standWinProb += dealerScoreProbabilities[k];
                    }
                    standWinProb += dealerScoreProbabilities[5];

                    short[] newDeck = cardsLeft.clone();
                    newDeck[finalI]--;
                    newDeck[finalJ]--;

                    final float finalStandWinProb = standWinProb;

                    Thread thread = new Thread(() -> {
                        float hitWinProb = calculateHitWinProbability(new byte[]{finalI, finalJ}, newDeck,
                                (short) (nOfCardsLeft - 2), (byte) 1, dealerScoreProbabilities);


                        winProbMatrix[finalI][finalJ] = Math.max(finalStandWinProb, hitWinProb);
                        winProbMatrix[finalJ][finalI] = winProbMatrix[finalI][finalJ];

                        winProbCalculationLatch.countDown();
                    });

                       thread.start();
            }
        }

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
            winProbCalculationLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        float playerWinningProb = 0.0f;

        for (byte i = 0; i < 10; i++) {
            for (byte j = 0; j < 10; j++) {
                playerWinningProb += CardProbMatrix[i][j] * winProbMatrix[i][j];
            }
        }

        return playerWinningProb;
    }

    public float wongHalvesWinningChances(short[] cardsLeft){
        int nOfCardsLeft = 0;


        int runningCount = 0;
        int[] wongHalvesValues = {-2, 1, 2, 2, 3, 2, 1, 0, -1, -2};

        for (int i = 0; i < 10; i++) {
            nOfCardsLeft += cardsLeft[i];

            // calculating running count
            // number of cards of a rank left * value in omega 2 system
            // negative because we are counting cards left in the deck, not cards dealt
            runningCount -= wongHalvesValues[i] * cardsLeft[i];
        }

        float decksLeft = nOfCardsLeft / 52f;


        float trueCount = runningCount/decksLeft/2f;

        float playersEdge = (trueCount - 2f) / 2f;


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
                if (cardsInDeck[i] == 0){
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

