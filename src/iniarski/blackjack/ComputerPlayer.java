package iniarski.blackjack;

public class ComputerPlayer extends Player{

    @Override
    public int play() {
        // TODO : Implement logic
        return Player.STAND;
    }

    // using Player() constructor
    public ComputerPlayer(Card firstCard, Card secondCard) {
        super(firstCard, secondCard);
    }
}
