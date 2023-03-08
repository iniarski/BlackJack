package iniarski.blackjack;

import java.util.Arrays;
import java.util.Collections;

public class Deck {
    final int STANDARD_DECK_SIZE = 52;

    private Card[] cards;
    private int deckSize;
    // onCard field hold information which card is on top of the deck
    // and will be drawn next
    private int onCard = 0;
    // nOfCardsLeft field stores information of number of cards left for each rank
    private int[] nOfCardsLeft = new int[13];


    public Deck(int nOfDecks) {
        // constructor creates a deck made of nOfDecks standard,
        // 52 playing card decks


        deckSize = STANDARD_DECK_SIZE * nOfDecks;
        cards = new Card[deckSize];

        int cardIndex = 0;

        // populating the deck

        for (int i = 0; i < nOfDecks; i++) {
            for (int j = 0; j < 13; j++) {
                for (int k = 0; k < 4; k++) {
                    cards[cardIndex] = new Card(j, k);
                    cardIndex++;
                }
            }
        }
        shuffle();

        Arrays.fill(nOfCardsLeft, 4 * nOfDecks);
    }

    Card deal() {
        // decrementing nOfCardLeft at index of the rank of cards
        nOfCardsLeft[cards[onCard].getRank()]--;
        // changing to next card
        onCard++;
        // returning the present card
        return cards[onCard - 1];
    }

    void print() {
        for (int i = 0; i < deckSize; i++) {
            System.out.println(cards[i].toString());
        }
    }

    void shuffle() {
        Collections.shuffle(Arrays.asList(cards));
    }
}
