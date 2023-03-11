package iniarski.blackjack;

import java.util.ArrayList;
import java.util.Arrays;

public class ComputerPlayer extends Player{


    protected int money;
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
        // TODO : Implement logic
        // as of now the computer plays the same as the dealer;
        if (score < 17) {
            return Player.HIT;
        }
        return Player.STAND;
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

    public void winMoney(int winnings) {
        money += winnings;
    }

    public int getMoney() {
        return money;
    }
}
