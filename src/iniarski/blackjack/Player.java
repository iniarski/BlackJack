package iniarski.blackjack;

import java.util.ArrayList;
import java.util.Arrays;

// defining abstract class Player form which
// Dealer, ComputerPlayer and (possibly) HumanPlayer will inherit
// Player class will define several basic methods (like adding card to hand and calculating score)
// methods such as play() defining player strategies will be defined in inheriting classes

public abstract class Player {
    // final variables describing actions taken in game
    public final static int STAND = 0;
    public final static int HIT = 1;
    public final static int DOUBLE_DOWN = 2;
    public final static int SPLIT = 3;
    public final static int SURRENDER = 4;

    // information of the player's cards will be stored in an ArrayList
    private ArrayList<Card> hand;

    private int score;

    public Player(Card firstCard, Card secondCard) {
        hand = new ArrayList<Card>(Arrays.asList(firstCard, secondCard));
        calculateScore();
    }

    public void addCard(Card addedCard) {
        hand.add(addedCard);
        calculateScore();
    }

    private void calculateScore() {
        //
        boolean hasAce = false;
        int tempScore = 0;

        for (int i = 0; i < hand.size(); i++) {
            int rank = hand.get(i).getRank();

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

    public int getScore() {
        return score;
    }

    // play() method will be defined in inheriting classes
    // will return an int representing action taken by the player
    // (as specified in static final fields)
    public abstract int play();
}
