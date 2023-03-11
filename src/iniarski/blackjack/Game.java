package iniarski.blackjack;

public class Game {
    // fields for holding game rules
    // TODO : write class for reading and storing game rules in .properties file from which the values will be taken
    // values should be read in constructor

    Game() {

    }

    protected int nOfDecks = 8;
    protected int minBet = 10;
    protected int maxBet = 100;
    protected int startingMoney = 250;
    protected int handsPlayed = 20;

    // TODO : write game logic
    void start() {
        Dealer dealer = new Dealer();
        ComputerPlayer player = new ComputerPlayer(startingMoney);

        Deck deck = new Deck(nOfDecks);

        for (int i = 0; i < handsPlayed; i++) {

            System.out.println("Hand : " + (i + 1) + "\n");

            System.out.println("Player : ");
            player.setHand(deck.deal(), deck.deal());
            player.printHand();

            System.out.println("Dealer : ");
            dealer.setHand(deck.deal());
            dealer.printHand();

            System.out.println("Player move");
            int playerBet = player.bet(minBet, maxBet);
            System.out.println("Player's bet : " + playerBet);
            while (player.play() != 0 && player.getScore() <= 21) {
                player.addCard(deck.deal());
                player.printHand();
            }

            // If the player is BUST (over 21) the dealer doesn't move

            if (player.getScore() > 21) {
                System.out.println("BUST - house wins");
                System.out.println("Player's money : " + player.getMoney() + "\n");
            }

            System.out.println("Dealer move");
            dealer.addCard(deck.deal());
            dealer.printHand();
            while (dealer.play() != 0 && dealer.getScore() <= 21) {
                dealer.addCard(deck.deal());
                dealer.printHand();
            }

            // player win condition
            if (dealer.getScore() > 21 || player.getScore() > dealer.getScore()) {
                System.out.println("Player wins");
                player.winMoney(2 * playerBet);
            } else {
                System.out.println("The house wins");
            }

            System.out.println("Player's money : " + player.getMoney() + "\n");
        }

        System.out.println("Game over");
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}
