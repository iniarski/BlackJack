package iniarski.blackjack;

import java.util.ArrayList;
import java.util.Arrays;

public class ComputerPlayer extends Player{


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
    public void calculateBestMove(int[] cardsLeft, int dealerCardRank) {

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

        // Temporary solution
        // TODO :  improve logic

        double bustProbability = 0;

        for (int i = 0; i < 10; i++) {
            // making an array for possible hand
            int[] possibleHand = Arrays.copyOf(cardsInHand, cardsInHand.length + 1);
            possibleHand[possibleHand.length - 1] = i;

            // checking if is a bust
            if (BlackjackUtil.getInstance().calculateScore(possibleHand) > 21) {
                // if is, add to probability of busting
                bustProbability += cardProbabilities[i];
            }
        }

        if (bustProbability > 0.5) {
            optimalMove = STAND;
        } else {
            optimalMove = HIT;
        }
    }


    public void winMoney(int winnings) {
        money += winnings;
    }

    public int getMoney() {
        return money;
    }
}
