package iniarski.blackjack;

import java.util.ArrayList;

public class Dealer extends Player{

    @Override
    public void setHand(ArrayList<Card> hand) {
        this.hand = hand;
        calculateScore();
    }

    public void setHand(Card dealersCard) {
        hand.clear();
        hand.add(dealersCard);
        calculateScore();
    }
    @Override
    public int play() {
        // According to the rules of Blackjack if the dealer has score of less than 17 he has to hit
        if (score < 17) {
            return Player.HIT;
        }
        // else he has to stand
        return Player.STAND;
    }

}
