package iniarski.blackjack;

import java.util.Arrays;
import java.util.Collections;

public class Deck {
    final static int STANDARD_DECK_SIZE = 52;

    private final Card[] cards;
    private final int deckSize;
    // onCard field hold information which card is on top of the deck
    // and will be drawn next
    private int onCard = 0;
    // nOfCardsLeft field stores information of number of cards left for each rank
    private final short[] nOfCardsLeft = new short[13];


    public Deck(int nOfDecks) {
        // constructor creates a deck made of nOfDecks standard,
        // 52 playing card decks


        deckSize = STANDARD_DECK_SIZE * nOfDecks;
        cards = new Card[deckSize];

        int cardIndex = 0;

        // populating the deck

        for (int i = 0; i < nOfDecks; i++) {
            for (byte j = 0; j < 13; j++) {
                for (byte k = 0; k < 4; k++) {
                    cards[cardIndex] = new Card(j, k);
                    cardIndex++;
                }
            }
        }
        shuffle();

        Arrays.fill(nOfCardsLeft, (short) (4 *  nOfDecks));
    }

    public Card deal() {
        try {
            // decrementing nOfCardLeft at index of the rank of cards
            nOfCardsLeft[cards[onCard].getRank()]--;
            // changing to next card
            onCard++;
            // returning the present card
            return cards[onCard - 1];
        } catch (IndexOutOfBoundsException e) {
            // exception happens when the deck run out of cards
            reshuffle();
            return deal();
        }
    }

    public Card dealFaceDownCard() {
        try {
            onCard++;
            return cards[onCard - 1];
        } catch (IndexOutOfBoundsException e) {
            // similarly as in deal()
            reshuffle();
            return dealFaceDownCard();
        }
    }

    public void revealFaceDownCard(int rank){
        nOfCardsLeft[rank]--;
    }

    public void print() {
        for (int i = 0; i < deckSize; i++) {
            System.out.println(cards[i].toString());
        }
    }

    void shuffle() {
        Collections.shuffle(Arrays.asList(cards));
    }

    public void reshuffle(){
        onCard = 0;
        Arrays.fill(nOfCardsLeft, (short) (deckSize / 13));
        shuffle();
    }

    public short[] getCardsLeftSimplified() {

        // this method returns int array containing information about the number of cards left in the deck
        short[] cardsSimplified = new short[10];
        System.arraycopy(nOfCardsLeft, 0, cardsSimplified, 0, 9);

        cardsSimplified[9] = 0;

        // 10, J, Q and K are added together because they are each worth 10 points
        // and are identical in terms of rules of the game
        for (int i = 9; i < 13; i++) {
            cardsSimplified[9] += nOfCardsLeft[i];
        }

        return cardsSimplified;
    }
}
