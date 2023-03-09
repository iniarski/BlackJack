package iniarski.blackjack;

public class Dealer extends Player{
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
