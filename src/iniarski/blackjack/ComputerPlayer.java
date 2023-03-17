package iniarski.blackjack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class ComputerPlayer extends Player{

    public final static double NOT_POSSIBLE = -128.0;
    private int money;
    // this field is used to store the move computed by
    private int optimalMove;
    private final int MAX_RECURSIONS = 4;
    public ComputerPlayer(int money){
        this.money = money;
    }


    @Override
    public void setHand(ArrayList<Card> hand) {
        this.hand = hand;
        calculateScore();
    }

    // overloading for ease of use
    public void setHand(Card firstCard, Card secondCard) {
        hand.clear();
        hand.add(firstCard);
        hand.add(secondCard);
        calculateScore();
    }

    @Override
    public int play() {
        return optimalMove;
    }

    //
    public int bet(int minBet, int maxBet) {
        // TODO : Implement betting logic

        int preferredBet = money / 20;

        if (preferredBet < minBet) {
            preferredBet = minBet;
        } else if (preferredBet > maxBet) {
            preferredBet = maxBet;
        }

        money -= preferredBet;

        return preferredBet;
    }

    // this function will save the code of best move to optimalMove field
    // takes the state of the game ( Deck.getCardsLeftSimplified()) and dealer's first card rank as inputs
    public void calculateBestMove(int[] cardsLeft) {

        // if the score is 21 the only valid move is to stand
        if (score==21) {
            optimalMove = STAND;
            return;
        }

        // Calculating probability of getting each card

        double[] cardProbabilities = new double[10];
        int nOfCardsLeft = 0;

        for (int n : cardsLeft) {
            nOfCardsLeft += n;
        }
        for (int i = 0; i < 10; i++) {
            cardProbabilities[i] = (double) cardsLeft[i] / (double) nOfCardsLeft;
        }

        int[] cardsInHand = new int[hand.size()];

        for (int i = 0; i < hand.size(); i++) {
            cardsInHand[i] = hand.get(i).getRank();
        }


        // expectedValues array stores expected value of actions
        // were expectedValues[ACTION] - expected value of doing action
        double[] expectedValues = new double[5];
        double[] dealerProbabilities = BlackjackUtil.getInstance().getDealerScoreProbabilities();

        // 0 - STAND
        double standWinProbability = 0.0;
        // adding probability that dealer has lower score
        for (int i = 0; i < score - 17; i++) {
            standWinProbability += dealerProbabilities[i];
        }
        // adding probability that dealer is bust
        standWinProbability += dealerProbabilities[5];

        // expected value of standing : ev = 1 * p - 1 * (1 - p) = 2p - 1
        // where p - probability of winning if standing

        expectedValues[STAND] = 2.0 * standWinProbability - 1.0;

        // 1 - HIT

        AtomicReference<Double> firstHitWinProbability = new AtomicReference<>(0.0);
        AtomicReference<Double> hitWinProbability = new AtomicReference<>(0.0);

        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            int finalI = i;

            int finalNOfCardsLeft = nOfCardsLeft;
            Thread thread = new Thread(() -> {

                // checking for case where there are no cards left of a rank;
                if (cardsLeft[finalI] == 0) {
                    latch.countDown();
                    return;
                }

                int[] newHand = Arrays.copyOf(cardsInHand, cardsInHand.length + 1);
                newHand[newHand.length - 1] = finalI;

                int tempScore = BlackjackUtil.getInstance().calculateScore(newHand);
                if (tempScore <= 21) {
                    double oneHitWinProbability = 0.0;
                    // adding probability that dealer has lower score
                    for (int j = 0; j < tempScore - 17; j++) {
                        oneHitWinProbability += dealerProbabilities[j];
                    }
                    // adding probability that dealer is bust
                    oneHitWinProbability += dealerProbabilities[5];
                    firstHitWinProbability.set(firstHitWinProbability.get() +
                            oneHitWinProbability * cardProbabilities[finalI] );
                } else { // bust
                    latch.countDown();
                    return;
                }

                    int [] newDeck = Arrays.copyOf(cardsLeft, cardsLeft.length);
                    newDeck[finalI]--;

                    hitWinProbability.set(hitWinProbability.get() + cardProbabilities[finalI] *
                            calculateHitWinProbability(newHand, newDeck, finalNOfCardsLeft - 1, 0));
            });
            }

        expectedValues[HIT] =  2.0 * hitWinProbability.get() - 1.0;

        // 2 - DOUBLE-DOWN

        // probability with winning with one card was calculate previously
        // possible only if first move

        if (hand.size() == 2) {
            expectedValues[DOUBLE_DOWN] = 4.0 * firstHitWinProbability.get() - 2.0;
        } else {
            expectedValues[DOUBLE_DOWN] = NOT_POSSIBLE;
        }

        // 3 - SPLIT
        // TODO : splitting

        expectedValues[SPLIT] = NOT_POSSIBLE;

        // 4 - SURRENDER
        // surrendering means forfeiting half original bet
        // possible only on first move

        // hence :
        if (hand.size() == 2) {
            expectedValues[SURRENDER] = -0.5;
        } else {
            expectedValues[SURRENDER] = NOT_POSSIBLE;
        }

        // looking for the highest expected value
        int maxIndex = 0;

        for (int i = 1; i < 5; i++) {
            if (expectedValues[i] > expectedValues[maxIndex]) {
                maxIndex = i;
            }
        }

        optimalMove = maxIndex;
    }

    private double calculateHitWinProbability(int[] cardsInHand, int[] cardsInDeck, int cardsLeft, int recursionNumber) {
        // returning if reached maximum search depth;
        if (recursionNumber == MAX_RECURSIONS) {
            return 0.0;
        }

        //calculating probabilities of getting cards
        double[] cardProbabilities = new double[10];

        for (int i = 0; i < 10; i++) {
            cardProbabilities[i] = (double) cardsInDeck[i] / (double) cardsLeft;
        }

        double[] dealerScoreProbabilities = BlackjackUtil.getInstance().getDealerScoreProbabilities();

        double winProb = 0.0;

        // multithreading here
        CountDownLatch latch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            AtomicReference<Double> tempWinProb = new AtomicReference<>(0.0);

            Thread thread = new Thread(() -> {

                // end if no cards of rank left
                if (cardsInDeck[finalI] == 0){
                    tempWinProb.set(0.0);
                    latch.countDown();
                    return;
                }

                int[] newHand = Arrays.copyOf(cardsInHand, cardsInHand.length + 1);
                newHand[newHand.length - 1] = finalI;

                int tempScore = BlackjackUtil.getInstance().calculateScore(newHand);

                // bust
                if (tempScore > 21) {
                    tempWinProb.set(0.0);
                    latch.countDown();
                    return;
                }

                double standNowWinProbability = 0.0;

                for (int j = 0; j < tempScore - 17 ; j++) {
                    standNowWinProbability += dealerScoreProbabilities[j];
                }

                standNowWinProbability += dealerScoreProbabilities[5];

                int[] newDeck = Arrays.copyOf(cardsInDeck, cardsInDeck.length);
                newDeck[finalI]--;

                double hitMoreWinProbability =
                        calculateHitWinProbability(newHand, newDeck, cardsLeft - 1, recursionNumber + 1);

                tempWinProb.set(cardProbabilities[finalI] * standNowWinProbability > hitMoreWinProbability ?
                        standNowWinProbability : hitMoreWinProbability);

                latch.countDown();

            });

            thread.start();

            winProb += tempWinProb.get();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return  winProb;
    }

    public void winMoney(int winnings) {
        money += winnings;
    }

    public int getMoney() {
        return money;
    }
}
