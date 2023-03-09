package iniarski.blackjack;

import java.util.ArrayList;

public class Game {
    // players stored in an ArrayList
    protected ArrayList<Player> players;

    // fields for holding game rules
    // TODO : write class for reading and storing game rules in .properties file from which the values will be taken

    protected int nOfDecks = 8;
    protected int minBet = 10;
    protected int maxBet = 100;
    protected int startingMoney = 250;
    protected int handsPlayed = 20;

    // TODO : write game logic
    Game() {
    }
}
