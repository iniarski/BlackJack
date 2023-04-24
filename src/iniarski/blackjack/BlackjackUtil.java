package iniarski.blackjack;

// class for handling miscellaneous tasks in the project

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class BlackjackUtil {

    // NEGLIGIBLE_THRESHOLD determines when probability is so small that case can be not considered
    public static final float NEGLIGIBLE_THRESHOLD = 0.0005f;
    public static final byte MAX_RECURSIONS = 3;
    public static final long BYTES_IN_MEGABYTE = 1_048_576;
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
            byte finalI = i;
            Thread thread = new Thread(null ,() -> {

                // checking for edge case - if there are no cards of such rank left in deck
                if (cardsInDeck[finalI] == 0) {
                    latch.countDown();
                    return;
                }

                // making new hand (by adding new card of specified index and checking if dealer still has play)
                byte[] newHand = Arrays.copyOf(dealerCards, dealerCards.length + 1);
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

                float futureCaseProbability = cardProbabilities[finalI] * caseProbability;

                // checking if recursive call is reasonable
                if (futureCaseProbability < NEGLIGIBLE_THRESHOLD) {
                    latch.countDown();
                    return;
                }

                // executes if dealer hits - recursive call
                short[] newDeck = cardsInDeck.clone();
                newDeck[finalI]--;
                calculatePossibleDealerHands(newHand, newDeck, futureCaseProbability, dealerScoreProbabilities);

                latch.countDown();
            },
                    this.toString(), 4 * BYTES_IN_MEGABYTE);

            thread.start();
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

        calculatePossibleDealerHands(new byte[0], cardsLeft, 1.0f, dealerScoreProbabilities);



        CountDownLatch winProbCalculationLatch = new CountDownLatch(55);

        for (byte i = 0; i < 10; i++) {
            for (byte j = i; j < 10; j++) {
                byte finalI = i; byte finalJ = j;
                Thread thread = new Thread(() -> {
                    float standWinProb = 0.0f;

                    byte tempScore = calculateScore(new byte[]{finalI, finalJ});

                    for (byte k = 0; k < tempScore - 17; k++) {
                        standWinProb += dealerScoreProbabilities[k];
                    }
                    standWinProb += dealerScoreProbabilities[5];

                    float hitWinProb = calculateHitWinProbability(new byte[]{finalI, finalJ}, cardsLeft, nOfCardsLeft,
                            (byte) 1, dealerScoreProbabilities);


                    winProbMatrix[finalI][finalJ] = Math.max(standWinProb, hitWinProb);
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

    public float calculateHitWinProbability(byte[] cardsInHand, short[] cardsInDeck, short cardsLeft,
                                            byte recursionNumber, float[] dealerScoreProbabilities) {

        //calculating probabilities of getting cards
        float[] cardProbabilities = new float[10];

        for (int i = 0; i < 10; i++) {
            cardProbabilities[i] = (float) cardsInDeck[i] / (float) cardsLeft;
        }

        AtomicReference<Float> winProb = new AtomicReference<>(0.0f);

        // multithreading here
        CountDownLatch latch = new CountDownLatch(10);
        // the stack size will be smaller in consecutive recursive calls
        long threadStackSize = 4 * (MAX_RECURSIONS - recursionNumber) * BYTES_IN_MEGABYTE;

        for (byte i = 0; i < 10; i++) {
            byte finalI = i;

            Thread thread = new Thread(null, () -> {
                // end if no cards of rank left
                if (cardsInDeck[finalI] == 0){
                    latch.countDown();
                    return;
                }

                byte[] newHand = Arrays.copyOf(cardsInHand, cardsInHand.length + 1);
                newHand[newHand.length - 1] = finalI;

                int tempScore = BlackjackUtil.getInstance().calculateScore(newHand);

                // bust
                if (tempScore > 21) {
                    winProb.set(0.0f);
                    latch.countDown();
                    return;
                }

                float standNowWinProbability = 0.0f;

                for (int j = 0; j < tempScore - 17 ; j++) {
                    standNowWinProbability += dealerScoreProbabilities[j];
                }

                standNowWinProbability += dealerScoreProbabilities[5];
                //standNowWinProbability += tempScore >= 17 ? dealerScoreProbabilities[tempScore - 17] / 2.0f : 0.0f;

                short[] newDeck = Arrays.copyOf(cardsInDeck, cardsInDeck.length);
                newDeck[finalI]--;

                float hitMoreWinProbability;

                if (recursionNumber == MAX_RECURSIONS - 1) {
                    hitMoreWinProbability = standNowWinProbability;
                } else {
                    hitMoreWinProbability =
                            calculateHitWinProbability(newHand, newDeck, (short) (cardsLeft - 1),
                                    (byte) (recursionNumber + 1), dealerScoreProbabilities);
                }


                winProb.set(winProb.get() +
                        cardProbabilities[finalI] * standNowWinProbability > hitMoreWinProbability ?
                        standNowWinProbability : hitMoreWinProbability);


                latch.countDown();

            }, "Thread-" + Thread.currentThread().hashCode() + "-" + i, threadStackSize);

            thread.start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return  winProb.get();
    }

}

