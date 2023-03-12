package iniarski.blackjack;

import java.util.ArrayList;

public class Dealer extends Player{
    private boolean hasRevealedCard;

    @Override
    public void setHand(ArrayList<Card> hand) {
        this.hand = hand;
        calculateScore();
    }

    public void setHand(Card dealersCard, Card faceDownCard) {
        hand.clear();
        hand.add(dealersCard);
        hand.add(faceDownCard);
        hasRevealedCard = false;
        calculateScore();
    }

    public int revealCard() {
        hasRevealedCard = true;
        return hand.get(1).getRank();
    }

    public int getRevealedCard() {
        return hand.get(0).getRank();
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

    @Override
    public void printHand() {
        if (hasRevealedCard) {
            super.printHand();
        } else {
            System.out.println(hand.get(0) + ", face down card");
        }
    }
}
