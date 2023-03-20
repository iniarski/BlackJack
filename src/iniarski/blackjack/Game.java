package iniarski.blackjack;

public class Game {
    // fields for holding game rules
    // TODO : write class for reading and storing game rules in .properties file from which the values will be taken
    // values should be read in constructor

    Game() {

    }

    protected short nOfDecks = 8;
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
            int playerBet = player.bet(minBet, maxBet, deck.getCardsLeftSimplified());
            System.out.println("Player's bet : " + playerBet);

            player.setHand(deck.deal(), deck.deal());
            player.printHand();

            System.out.println("Dealer : ");
            dealer.setHand(deck.deal(), deck.dealFaceDownCard());
            dealer.printHand();

            BlackjackUtil.getInstance().calculateDealerProbabilities(
                    dealer.getRevealedCard(), deck.getCardsLeftSimplified());

            System.out.println("Player move");

            boolean playerMakesNextMove = true;
            boolean dealerMoves = true;

            do {
                player.calculateBestMove(deck.getCardsLeftSimplified());

                switch (player.play()) {
                    case Player.STAND:
                        playerMakesNextMove = false;
                        System.out.println("Player stands");
                        break;

                    case Player.HIT:
                        player.addCard(deck.deal());
                        System.out.println("Player hits");
                        if (player.getScore() > 21) {
                            playerMakesNextMove = false;
                        }
                        break;

                    case Player.DOUBLE_DOWN:
                        player.addCard(deck.deal());
                        playerMakesNextMove = false;
                        player.winMoney(-playerBet);
                        playerBet = 2 * playerBet;
                        System.out.println("Player doubles bet! New bet : " + playerBet);
                        break;

                    case Player.SPLIT:
                        // not yet implemented
                        break;

                    case Player.SURRENDER:
                            playerMakesNextMove = false;
                            player.winMoney(playerBet / 2);
                            dealerMoves = false;
                            System.out.println("Player surrenders loosing half bet. New balance : " + player.getMoney());
                            break;

                    default:
                        System.out.println("You shouldn't be seeing this message - something went horribly wrong.");
                        break;
                }

                player.printHand();

            } while (playerMakesNextMove);


            // If the player is BUST (over 21) the dealer doesn't move
            if (player.getScore() > 21) {
                System.out.println("BUST - house wins");
                System.out.println("Player's money : " + player.getMoney() + "\n");
                continue;
            }

            if (!dealerMoves) {
                continue;
            }

            System.out.println("Dealer move");
            deck.revealFaceDownCard(dealer.revealCard());
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
