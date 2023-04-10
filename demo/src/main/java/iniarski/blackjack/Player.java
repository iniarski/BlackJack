package iniarski.blackjack;

import java.util.ArrayList;
import java.util.Arrays;

// defining abstract class Player form which
// Dealer, ComputerPlayer and (possibly) HumanPlayer will inherit
// Player class will define several basic methods (like adding card to hand and calculating score)
// methods such as play() defining player strategies will be defined in inheriting classes

public abstract class Player {
    // final variables describing actions taken in game
    public final static byte STAND = 0;
    public final static byte HIT = 1;
    public final static byte DOUBLE_DOWN = 2;
    public final static byte SPLIT = 3;
    public final static byte SURRENDER = 4;

    // information of the player's cards will be stored in an ArrayList
    protected ArrayList<Card> hand = new ArrayList<>();

    protected byte score;

    public abstract void setHand(ArrayList <Card> hand);

    public void addCard(Card addedCard) {
        hand.add(addedCard);
        calculateScore();
    }

    protected void calculateScore() {
        //
        boolean hasAce = false;
        byte tempScore = 0;

        for (int i = 0; i < hand.size(); i++) {
            byte rank = hand.get(i).getRank();

            if (rank == 0) {
                hasAce = true;
            }
            if (rank < 9) {
                tempScore += rank + 1;
            }
            else {
                tempScore += 10;
            }
        }
        if (hasAce && tempScore <= 11) {
            tempScore += 10;
        }

        score = tempScore;
    }

    public byte getScore() {
        return score;
    }

    public void printHand() {
        for (Card card : hand) {
            System.out.print(card + ", ");
        }
        System.out.println(score + " points");
    }

    // play() method will be defined in inheriting classes
    // will return an int representing action taken by the player
    // (as specified in static final fields)
    public abstract byte play();
}
