package iniarski.blackjack;

import java.util.ArrayList;
import java.util.Arrays;

public class Game  {

    

    // fields for holding game rules
    protected  short nOfDecks;
    protected  int minBet;
    protected  int maxBet;
    protected  int startingMoney;
    protected  int handsPlayed;
    protected  int id;
    
    
   

    Game(int id) 
    {    
        this.id = id;
        
    }

    // field for storing player's money after each hand played
    protected int[] moneyHistogram;

    void start() {
        Dealer dealer = new Dealer();
        ComputerPlayer player = new ComputerPlayer(startingMoney);
        moneyHistogram = new int[handsPlayed];

        Deck deck = new Deck(nOfDecks);

        for (int i = 0; i < handsPlayed; i++) {

            if (player.getMoney() < minBet) {
                // if players runs out of money the game ends
                Arrays.fill(moneyHistogram, i, handsPlayed, player.getMoney());
                break;
            }

            int playerBet = player.bet(minBet, maxBet, deck.getCardsLeftSimplified());

            player.setHand(deck.deal(), deck.deal());

            dealer.setHand(deck.deal(), deck.dealFaceDownCard());

            if (dealer.has21()) {
                deck.revealFaceDownCard(dealer.revealCard());
                moneyHistogram[i] = player.getMoney();
                continue;
            }

            BlackjackUtil.getInstance().calculateDealerProbabilities(
                    dealer.getRevealedCard(), deck.getCardsLeftSimplified());

            boolean playerMakesNextMove = true;
            boolean dealerMoves = true;

            do {
                player.calculateBestMove(deck.getCardsLeftSimplified());

                switch (player.play()) {
                    case Player.STAND -> {
                        playerMakesNextMove = false;
                    }
                    case Player.HIT -> {
                        player.addCard(deck.deal());
                        if (player.getScore() > 21) {
                            playerMakesNextMove = false;
                        }
                    }
                    case Player.DOUBLE_DOWN -> {
                        player.addCard(deck.deal());
                        playerMakesNextMove = false;
                        player.winMoney(-playerBet);
                        playerBet = 2 * playerBet;
                    }
                    case Player.SPLIT -> {
                        // making two new ComputerPlayer objects to play each of the hands

                        ComputerPlayer firstHand = new ComputerPlayer(0);
                        ComputerPlayer secondHand = new ComputerPlayer(0);
                        firstHand.setCanSplit(false);
                        secondHand.setCanSplit(false);
                        ArrayList<Card> playersHand = player.getHand();
                        firstHand.setHand(playersHand.get(0), deck.deal());
                        secondHand.setHand(playersHand.get(1), deck.deal());
                        BlackjackUtil.getInstance().
                                calculateDealerProbabilities(dealer.getRevealedCard(), deck.getCardsLeftSimplified());
                        firstHand.calculateBestMove(deck.getCardsLeftSimplified());
                        secondHand.calculateBestMove(deck.getCardsLeftSimplified());
                        boolean firstHandStillPlays = true;
                        boolean secondHandStillPlays = true;
                        boolean firstHandWaitsForDealer = true;
                        boolean secondHandWaitsForDealer = true;
                        int firstHandBet = playerBet / 2;
                        int secondHandBet = playerBet - firstHandBet;
                        while (firstHandStillPlays || secondHandStillPlays) {

                            if (firstHandStillPlays) {
                                switch (firstHand.play()) {
                                    case Player.STAND -> {
                                        firstHandStillPlays = false;
                                    }
                                    case Player.HIT -> {
                                        firstHand.addCard(deck.deal());
                                    }
                                    case Player.DOUBLE_DOWN -> {
                                        firstHand.addCard(deck.deal());
                                        firstHandStillPlays = false;
                                        player.winMoney(-firstHandBet);
                                        firstHandBet = 2 * firstHandBet;
                                    }
                                    case Player.SURRENDER -> {
                                        firstHandStillPlays = false;
                                        firstHandWaitsForDealer = false;
                                        player.winMoney(firstHandBet / 2);
                                    }
                                }

                                if (firstHand.getScore() > 21) {
                                    firstHandStillPlays = false;
                                    firstHandWaitsForDealer = false;
                                }

                            }

                            if (secondHandStillPlays) {
                                switch (secondHand.play()) {
                                    case Player.STAND -> {
                                        secondHandStillPlays = false;
                                    }
                                    case Player.HIT -> {
                                        secondHand.addCard(deck.deal());
                                    }
                                    case Player.DOUBLE_DOWN -> {
                                        secondHand.addCard(deck.deal());
                                        secondHandStillPlays = false;
                                        player.winMoney(-secondHandBet);
                                        secondHandBet = 2 * secondHandBet;
                                    }
                                    case Player.SURRENDER -> {
                                        secondHandStillPlays = false;
                                        firstHandWaitsForDealer = false;
                                        player.winMoney(secondHandBet / 2);
                                    }
                                }
                            }


                            if (secondHand.getScore() > 21) {
                                secondHandWaitsForDealer = false;
                                secondHandStillPlays = false;
                            }

                            if (firstHandStillPlays || secondHandStillPlays) {
                                BlackjackUtil.getInstance().
                                        calculateDealerProbabilities(dealer.getRevealedCard(), deck.getCardsLeftSimplified());
                                if (firstHandStillPlays) {
                                    firstHand.calculateBestMove(deck.getCardsLeftSimplified());
                                }
                                if (secondHandStillPlays) {
                                    secondHand.calculateBestMove(deck.getCardsLeftSimplified());
                                }
                                continue;
                            }

                            if (firstHandWaitsForDealer || secondHandWaitsForDealer) {
                                deck.revealFaceDownCard(dealer.revealCard());

                                while (dealer.play() != Player.STAND && dealer.score <= 21) {
                                    dealer.addCard(deck.deal());
                                }

                                if (firstHandWaitsForDealer) {
                                    if (firstHand.getScore() > dealer.getScore() || dealer.getScore() > 21) {
                                        player.winMoney(2 * firstHandBet);
                                    }
                                }
                                if (secondHandWaitsForDealer) {
                                    if (secondHand.getScore() > dealer.getScore() || dealer.getScore() > 21) {
                                        player.winMoney(2 * secondHandBet);
                                    }
                                }
                            }

                            playerMakesNextMove = false;
                            dealerMoves = false;

                            moneyHistogram[i] = player.getMoney();
                        }
                    }
                    case Player.SURRENDER -> {
                        playerMakesNextMove = false;
                        player.winMoney(playerBet / 2);
                        dealerMoves = false;
                        moneyHistogram[i] = player.getMoney();
                    }
                }


            } while (playerMakesNextMove);


            // If the player is BUST (over 21) the dealer doesn't move
            if (player.getScore() > 21) {
                moneyHistogram[i] = player.getMoney();
                continue;
            }

            if (!dealerMoves) {
                continue;
            }

            deck.revealFaceDownCard(dealer.revealCard());
            while (dealer.play() != 0 && dealer.getScore() <= 21) {
                dealer.addCard(deck.deal());
            }

            // player win condition
            if (dealer.getScore() > 21 || player.getScore() > dealer.getScore()) {
                player.winMoney(2 * playerBet);
            }

            moneyHistogram[i] = player.getMoney();
        }
    }

    void startWithPrinting() {
        Dealer dealer = new Dealer();
        ComputerPlayer player = new ComputerPlayer(startingMoney);
        moneyHistogram = new int[handsPlayed];

        Deck deck = new Deck(nOfDecks);

        for (int i = 0; i < handsPlayed; i++) {

            if (player.getMoney() < minBet) {
                // if players runs out of money the game ends
                Arrays.fill(moneyHistogram, i, handsPlayed, player.getMoney());
                break;
            }

            System.out.println("Hand : " + (i + 1) + "\n");

            System.out.println("Player : ");
            int playerBet = player.bet(minBet, maxBet, deck.getCardsLeftSimplified());
            System.out.println("Player's bet : " + playerBet);

            player.setHand(deck.deal(), deck.deal());
            player.printHand();

            System.out.println("Dealer : ");
            dealer.setHand(deck.deal(), deck.dealFaceDownCard());
            dealer.printHand();

            if (dealer.has21()) {
                deck.revealFaceDownCard(dealer.revealCard());
                System.out.println("Dealer has 21 points and wins the hand");
                dealer.printHand();
                moneyHistogram[i] = player.getMoney();
                continue;
            }

            BlackjackUtil.getInstance().calculateDealerProbabilities(
                    dealer.getRevealedCard(), deck.getCardsLeftSimplified());

            System.out.println("Player move");

            boolean playerMakesNextMove = true;
            boolean dealerMoves = true;

            do {
                player.calculateBestMove(deck.getCardsLeftSimplified());

                switch (player.play()) {
                    case Player.STAND -> {
                        playerMakesNextMove = false;
                        System.out.println("Player stands");
                        player.printHand();
                    }
                    case Player.HIT -> {
                        player.addCard(deck.deal());
                        System.out.println("Player hits");
                        player.printHand();
                        if (player.getScore() > 21) {
                            playerMakesNextMove = false;
                        }
                    }
                    case Player.DOUBLE_DOWN -> {
                        player.addCard(deck.deal());
                        playerMakesNextMove = false;
                        player.winMoney(-playerBet);
                        playerBet = 2 * playerBet;
                        System.out.println("Player doubles bet! New bet : " + playerBet);
                        player.printHand();
                    }
                    case Player.SPLIT -> {
                        // making two new ComputerPlayer objects to play each of the hands

                        ComputerPlayer firstHand = new ComputerPlayer(0);
                        ComputerPlayer secondHand = new ComputerPlayer(0);
                        firstHand.setCanSplit(false);
                        secondHand.setCanSplit(false);
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
                        int firstHandBet = playerBet / 2;
                        int secondHandBet = playerBet - firstHandBet;
                        while (firstHandStillPlays || secondHandStillPlays) {

                            if (firstHandStillPlays) {
                                switch (firstHand.play()) {
                                    case Player.STAND -> {
                                        System.out.println("Player stands with first hand");
                                        firstHand.printHand();
                                        firstHandStillPlays = false;
                                    }
                                    case Player.HIT -> {
                                        firstHand.addCard(deck.deal());
                                        System.out.println("Player hits with first hand");
                                        firstHand.printHand();
                                    }
                                    case Player.DOUBLE_DOWN -> {
                                        firstHand.addCard(deck.deal());
                                        System.out.println();
                                        firstHandStillPlays = false;
                                        player.winMoney(-firstHandBet);
                                        firstHandBet = 2 * firstHandBet;
                                        System.out.println
                                                ("Player doubles bet with first hand. New bet : " + firstHandBet);
                                    }
                                    case Player.SURRENDER -> {
                                        firstHandStillPlays = false;
                                        firstHandWaitsForDealer = false;
                                        System.out.println("Player surrenders with first hand.");
                                        player.winMoney(firstHandBet / 2);
                                    }
                                }

                                if (firstHand.getScore() > 21) {
                                    System.out.println("First hand is bust");
                                    firstHandStillPlays = false;
                                    firstHandWaitsForDealer = false;
                                }

                            }

                            if (secondHandStillPlays) {
                                switch (secondHand.play()) {
                                    case Player.STAND -> {
                                        System.out.println("Player stands with second hand");
                                        secondHand.printHand();
                                        secondHandStillPlays = false;
                                    }
                                    case Player.HIT -> {
                                        secondHand.addCard(deck.deal());
                                        System.out.println("Player hits with second hand");
                                        secondHand.printHand();
                                    }
                                    case Player.DOUBLE_DOWN -> {
                                        secondHand.addCard(deck.deal());
                                        System.out.println();
                                        secondHandStillPlays = false;
                                        player.winMoney(-secondHandBet);
                                        secondHandBet = 2 * secondHandBet;
                                        System.out.println
                                                ("Player doubles bet with second hand. New bet : " + secondHandBet);
                                    }
                                    case Player.SURRENDER -> {
                                        secondHandStillPlays = false;
                                        firstHandWaitsForDealer = false;
                                        System.out.println("Player surrenders with second hand.");
                                        player.winMoney(secondHandBet / 2);
                                    }
                                }
                            }


                            if (secondHand.getScore() > 21) {
                                System.out.println("Second hand is bust");
                                secondHandWaitsForDealer = false;
                                secondHandStillPlays = false;
                            }

                            if (firstHandStillPlays || secondHandStillPlays) {
                                BlackjackUtil.getInstance().
                                        calculateDealerProbabilities(dealer.getRevealedCard(), deck.getCardsLeftSimplified());
                                if (firstHandStillPlays) {
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

                                if (firstHandWaitsForDealer) {
                                    if (firstHand.getScore() > dealer.getScore() || dealer.getScore() > 21) {
                                        player.winMoney(2 * firstHandBet);
                                        System.out.println("First hand wins");
                                    } else {
                                        System.out.println("Dealer wins with first hand");
                                    }
                                }
                                if (secondHandWaitsForDealer) {
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

                            System.out.println("Player's money : " + player.getMoney() + "\n");
                            moneyHistogram[i] = player.getMoney();
                        }
                    }
                    case Player.SURRENDER -> {
                        playerMakesNextMove = false;
                        player.winMoney(playerBet / 2);
                        dealerMoves = false;
                        System.out.println("Player surrenders loosing half bet. New balance : " + player.getMoney());
                        moneyHistogram[i] = player.getMoney();
                    }
                    default ->
                            System.out.println("You shouldn't be seeing this message - something went horribly wrong.");
                }


            } while (playerMakesNextMove);


            // If the player is BUST (over 21) the dealer doesn't move
            if (player.getScore() > 21) {
                System.out.println("BUST - house wins");
                System.out.println("Player's money : " + player.getMoney() + "\n");
                moneyHistogram[i] = player.getMoney();
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
            moneyHistogram[i] = player.getMoney();
        }

        System.out.println("Game over");
    }

    public int[] getMoneyHistogram() {
        return moneyHistogram;
    }

    public static void main(String[] args) {
        Game game = new Game(0);
        game.startWithPrinting();
        
        
    }
}
