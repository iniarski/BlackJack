package iniarski.blackjack;

import java.util.ArrayList;
import java.util.Arrays;

public class ComputerPlayer extends Player{

    public final static double NOT_POSSIBLE = -128.0;
    private int money;
    private int optimalMove;
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

        // TODO implement recursive lookup or something

        double bustProbability = 0.0;
        double firstHitWinProbability = 0.0;

        for (int i = 0; i < 10; i++) {
            int[] newHand = Arrays.copyOf(cardsInHand, cardsInHand.length + 1);
            newHand[newHand.length - 1] = i;

            int tempScore = BlackjackUtil.getInstance().calculateScore(newHand);
            if (tempScore > 21) {
                bustProbability +=  cardProbabilities[i];
            } else {
                double oneHitWinProbability = 0.0;
                // adding probability that dealer has lower score
                for (int j = 0; j < tempScore - 17; j++) {
                    oneHitWinProbability += dealerProbabilities[j];
                }
                // adding probability that dealer is bust
                oneHitWinProbability += dealerProbabilities[5];

                firstHitWinProbability += oneHitWinProbability * cardProbabilities[i];
            }
        }

        // Temporary solution, make proper probability calculation later
        expectedValues[HIT] = firstHitWinProbability - bustProbability;

        // 2 - DOUBLE-DOWN

        // probability with winning with one card was calculate previously
        // possible only if first move

        if (hand.size() == 2) {
            expectedValues[DOUBLE_DOWN] = 4.0 * firstHitWinProbability - 2.0;
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

    public void winMoney(int winnings) {
        money += winnings;
    }

    public int getMoney() {
        return money;
    }
}
