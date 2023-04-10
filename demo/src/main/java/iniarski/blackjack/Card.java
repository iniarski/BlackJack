package iniarski.blackjack;

public class Card {
    public final static char[] RANKS =
            {'A', '2', '3', '4', '5', '6', '7', '8', '9', '1', 'J', 'Q', 'K'}; // !! 1 means 10,
    // can't have two digits in single char
    public final static String[] SUITS =
            {"CLUBS", "DIAMONDS", "HEARTS", "SPADES"};
    public final static char[] SUIT_SYMBOLS =
            {'♣', '♦', '♥', '♠'};
    private final byte rank;
    private final byte suit;

    public Card(byte rankIndex, byte suitIndex) {
        rank = rankIndex;
        suit = suitIndex;
    }

    public byte getRank() {
        return rank;
    }

    public byte getRankSimplified() {
        return rank > 9 ? 9 : rank;
    }

    public char getRankAsChar() {
        return RANKS[rank];
    }

    public byte getSuit() {
        return suit;
    }

    public String getSuitAsString() {
        return SUITS[suit];
    }

    public char getSuitSymbol() {
        return SUIT_SYMBOLS[suit];
    }

    @Override
    public String toString() {
        // special case for 10
        if (rank == 9) {
            return "10" + SUIT_SYMBOLS[suit];
        }
        // else
        return Character.toString(RANKS[rank]) + SUIT_SYMBOLS[suit];
    }
}
