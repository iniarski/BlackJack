package iniarski.blackjack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class ComputerPlayer extends Player{

    public final static float NOT_POSSIBLE = -128.0f;
    private boolean canSplit = true;
    private boolean canDoubleDown = true;
    private boolean canSurrender = true;
    private int money;
    // this field is used to store the move computed by
    private byte optimalMove;

    public ComputerPlayer(int money){
        this.money = money;
        // TODO: read rules in constructor (canSplit etc.)
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
    public byte play() {
        return optimalMove;
    }

    //
    public int bet(int minBet, int maxBet, short[] cardsLeft) {

        //float winProb = BlackjackUtil.getInstance().calculatePlayerWinningChances(cardsLeft);

        // Temporary fix, remove when calculatePlayerWinningChances will be working properly

        float winProb = BlackjackUtil.getInstance().wongHalvesWinningChances(cardsLeft);


        // optimal bet fraction derived from Kelly's criterion
        float betFraction = 2.0f * winProb - 1.0f;

        int preferredBet = (int) (money * betFraction);

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
    public void calculateBestMove(short[] cardsLeft, byte dealersFaceUpCard) {

        // if the score is 21 the only valid move is to stand
        if (score >= 19) {
            optimalMove = STAND;
            return;
        }

        // Calculating probability of getting each card

        float[] cardProbabilities = new float[10];
        short nOfCardsLeft = 0;

        for (short n : cardsLeft) {
            nOfCardsLeft += n;
        }
        for (int i = 0; i < 10; i++) {
            cardProbabilities[i] = (float) cardsLeft[i] / (float) nOfCardsLeft;
        }

        byte[] cardsInHand = new byte[hand.size()];

        for (int i = 0; i < hand.size(); i++) {
            cardsInHand[i] = hand.get(i).getRankSimplified();
        }


        // expectedValues array stores expected value of actions
        // were expectedValues[ACTION] - expected value of doing action
        float[] expectedValues = new float[5];
        float[] dealerProbabilities = BlackjackUtil.getInstance()
                .getDealerScoreProbabilities(dealersFaceUpCard, cardsLeft);


        // 0 - STAND
        float standWinProbability = 0.0f;
        // adding probability that dealer has lower score
        for (int i = 0; i < score - 17; i++) {
            standWinProbability += dealerProbabilities[i];
        }
        // adding probability that dealer is bust
        standWinProbability += dealerProbabilities[5];

        float standPushProbability = score >= 17 ? dealerProbabilities[score - 17] : 0.0f;

        // expected value of standing : ev = 1 * p - 1 * (1 - p) = 2p - 1
        // where p - probability of winning if standing

        expectedValues[STAND] = 2.0f * standWinProbability - 1.0f + standPushProbability;

        // 1 - HIT

        AtomicReference<Float> firstHitWinProbability = new AtomicReference<>(0.0f);
        AtomicReference<Float> hitWinProbability = new AtomicReference<>(0.0f);

        CountDownLatch latch = new CountDownLatch(10);

        for (byte i = 0; i < 10; i++) {
            byte finalI = i;

            int finalNOfCardsLeft = nOfCardsLeft;
            Thread thread = new Thread(() -> {

                // checking for case where there are no cards left of a rank;
                if (cardsLeft[finalI] == 0) {
                    latch.countDown();
                    return;
                }

                byte[] newHand = Arrays.copyOf(cardsInHand, cardsInHand.length + 1);
                newHand[newHand.length - 1] = finalI;

                int tempScore = BlackjackUtil.getInstance().calculateScore(newHand);
                if (tempScore <= 21) {
                    float oneHitWinProbability = 0.0f;
                    // adding probability that dealer has lower score
                    for (int j = 0; j < tempScore - 17; j++) {
                        oneHitWinProbability += dealerProbabilities[j];
                    }
                    // adding probability that dealer is bust
                    oneHitWinProbability += dealerProbabilities[5];
                    firstHitWinProbability.set(firstHitWinProbability.get() +
                            oneHitWinProbability * cardProbabilities[finalI] );

                    //System.out.println("firstHitWinProbability : " + firstHitWinProbability.get());
                } else { // bust
                    latch.countDown();
                    return;
                }

                    short [] newDeck = Arrays.copyOf(cardsLeft, cardsLeft.length);
                    newDeck[finalI]--;

                    final float thisHitWinProb = cardProbabilities[finalI] *
                            BlackjackUtil.getInstance()
                                    .calculateHitWinProbability(newHand, newDeck, (short) (finalNOfCardsLeft - 1),
                                            (byte) 0, dealerProbabilities);

                    hitWinProbability.set(hitWinProbability.get() + thisHitWinProb);

                    //System.out.println("hitWinProbability : " + hitWinProbability.get());

                    latch.countDown();
            });

            thread.start();
            }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        expectedValues[HIT] =  2.0f * hitWinProbability.get() - 1.0f;

        // 2 - float-DOWN

        // probability with winning with one card was calculate previously
        // possible only if first move

        if (hand.size() == 2 && canDoubleDown) {
            expectedValues[DOUBLE_DOWN] = 4.0f * firstHitWinProbability.get() - 2.0f;
        } else {
            expectedValues[DOUBLE_DOWN] = NOT_POSSIBLE;
        }

        // 3 - SPLIT

        // conditions for splitting :
        // 1. first move of the hand
        // 2. two identical cards in hand
        // 3. does not play from a split hand (the CAN_SPLIT field)

        if (hand.size() == 2 && hand.get(0).getRank() == hand.get(1).getRank()
        && canSplit) {
            expectedValues[SPLIT] = BlackjackUtil.getInstance().calculateHitWinProbability(
                    cardsInHand, cardsLeft, nOfCardsLeft,
                    (byte) 0, dealerProbabilities) * 2.0f - 1.0f;
        } else {
            expectedValues[SPLIT] = NOT_POSSIBLE;
        }

        // 4 - SURRENDER
        // surrendering means forfeiting half original bet
        // possible only on first move

        // hence :
        if (hand.size() == 2 && canSurrender) {
            expectedValues[SURRENDER] = -0.5f;
        } else {
            expectedValues[SURRENDER] = NOT_POSSIBLE;
        }

        // looking for the highest expected value
        byte maxIndex = 0;

        for (byte i = 1; i < 5; i++) {
            if (expectedValues[i] > expectedValues[maxIndex]) {
                maxIndex = i;
            }
        }

        optimalMove = maxIndex;
    }


    public void winMoney(int winnings) {
        money += winnings;
    }

    public int getMoney() {
        return money;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public void setCanSplit(boolean canSplit) {
        this.canSplit = canSplit;
    }
}
