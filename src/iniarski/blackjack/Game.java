package iniarski.blackjack;

import java.util.ArrayList;

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
                        player.printHand();
                        break;

                    case Player.HIT:
                        player.addCard(deck.deal());
                        System.out.println("Player hits");
                        player.printHand();
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
                        player.printHand();
                        break;

                    case Player.SPLIT:
                        // making two new ComputerPlayer objects to play each of the hands

                        ComputerPlayer firstHand = new ComputerPlayer(0);
                        ComputerPlayer secondHand = new ComputerPlayer(0);

                        ComputerPlayer.CAN_SPLIT = false;
                        ArrayList<Card> playersHand = player.getHand();
                        firstHand.setHand(playersHand.get(0), deck.deal());
                        secondHand.setHand(playersHand.get(1), deck.deal());

                        System.out.println("Player splits! Player plays with two hand, each with half original bet");

                        BlackjackUtil.getInstance().
                                calculateDealerProbabilities(dealer.getRevealedCard(), deck.getCardsLeftSimplified());

                        firstHand.calculateBestMove(deck.getCardsLeftSimplified());
                        secondHand.calculateBestMove(deck.getCardsLeftSimplified());

                        System.out.println("First hand :");
                        firstHand.printHand();
                        System.out.println("Second hand :");
                        secondHand.printHand();

                        boolean firstHandStillPlays = true;
                        boolean secondHandStillPlays = true;

                        boolean firstHandWaitsForDealer = true;
                        boolean secondHandWaitsForDealer = true;

                        int firstHandBet = playerBet/2;
                        int secondHandBet = playerBet - firstHandBet;

                        while (firstHandStillPlays || secondHandStillPlays) {

                            if(firstHandStillPlays) {
                                switch (firstHand.play()) {
                                    case Player.STAND:
                                        System.out.println("Player stands with first hand");
                                        firstHand.printHand();
                                        firstHandStillPlays = false;
                                        break;

                                    case Player.HIT:
                                        firstHand.addCard(deck.deal());
                                        System.out.println("Player hits with first hand");
                                        firstHand.printHand();
                                        break;

                                    case Player.DOUBLE_DOWN:
                                        firstHand.addCard(deck.deal());
                                        System.out.println();
                                        firstHandStillPlays = false;
                                        player.winMoney(-firstHandBet);
                                        firstHandBet = 2 * firstHandBet;
                                        System.out.println
                                               ("Player doubles bet with first hand. New bet : " + firstHandBet);
                                        break;

                                    case Player.SURRENDER:
                                        firstHandStillPlays = false;
                                        firstHandWaitsForDealer = false;
                                        System.out.println("Player surrenders with first hand.");
                                        player.winMoney(firstHandBet / 2);
                                        break;
                                }

                                if(firstHand.getScore() > 21) {
                                    System.out.println("First hand is bust");
                                    firstHandStillPlays = false;
                                    firstHandWaitsForDealer = false;
                                }

                            }

                            if (secondHandStillPlays) {
                                switch (secondHand.play()) {
                                    case Player.STAND:
                                        System.out.println("Player stands with second hand");
                                        secondHand.printHand();
                                        secondHandStillPlays = false;
                                        break;

                                    case Player.HIT:
                                        secondHand.addCard(deck.deal());
                                        System.out.println("Player hits with second hand");
                                        secondHand.printHand();
                                        break;

                                    case Player.DOUBLE_DOWN:
                                        secondHand.addCard(deck.deal());
                                        System.out.println();
                                        secondHandStillPlays = false;
                                        player.winMoney(-secondHandBet);
                                        secondHandBet = 2 * secondHandBet;
                                        System.out.println
                                                ("Player doubles bet with second hand. New bet : " + secondHandBet);
                                        break;

                                    case Player.SURRENDER:
                                        secondHandStillPlays = false;
                                        firstHandWaitsForDealer = false;
                                        System.out.println("Player surrenders with second hand.");
                                        player.winMoney(secondHandBet / 2);
                                        break;
                                }
                            }


                            if(secondHand.getScore() > 21) {
                                System.out.println("Second hand is bust");
                                secondHandWaitsForDealer = false;
                                secondHandStillPlays = false;
                            }

                            if (firstHandStillPlays || secondHandStillPlays) {
                                BlackjackUtil.getInstance().
                                        calculateDealerProbabilities(dealer.getRevealedCard(), deck.getCardsLeftSimplified());
                                if(firstHandStillPlays) {
                                    firstHand.calculateBestMove(deck.getCardsLeftSimplified());
                                }
                                if (secondHandStillPlays) {
                                    secondHand.calculateBestMove(deck.getCardsLeftSimplified());
                                }
                                continue;
                            }

                            if (firstHandWaitsForDealer || secondHandWaitsForDealer) {
                                System.out.println("Dealer move");
                                deck.revealFaceDownCard(dealer.revealCard());
                                dealer.printHand();

                                while (dealer.play() != Player.STAND && dealer.score <= 21) {
                                    dealer.addCard(deck.deal());
                                    dealer.printHand();
                                }

                                if (firstHandWaitsForDealer){
                                    if (firstHand.getScore() > dealer.getScore() || dealer.getScore() > 21) {
                                        player.winMoney(2 * firstHandBet);
                                        System.out.println("First hand wins");
                                    } else {
                                        System.out.println("Dealer wins with first hand");
                                    }
                                }
                                if (secondHandWaitsForDealer){
                                    if (secondHand.getScore() > dealer.getScore() || dealer.getScore() > 21) {
                                        player.winMoney(2 * secondHandBet);
                                        System.out.println("Second hand wins");
                                    } else {
                                        System.out.println("Dealer wins with second hand");
                                    }
                                }
                            }

                            playerMakesNextMove = false;
                            dealerMoves = false;
                            ComputerPlayer.CAN_SPLIT = true;

                            System.out.println("Player's money : " + player.getMoney() + "\n");
                        }

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
