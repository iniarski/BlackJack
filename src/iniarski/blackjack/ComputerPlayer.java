package iniarski.blackjack;

public class ComputerPlayer extends Player{


    protected int money;
    public ComputerPlayer(int money){
        this.money = money;
    }

    @Override
    public int play() {
        // TODO : Implement logic
        return Player.STAND;
    }

    //
    public int bet(int minBet, int maxBet) {
        // TODO : Implement betting logic

        int preferredBet = money / 20;

        if (preferredBet < minBet) {
            preferredBet = minBet;
        } else if (preferredBet > maxBet) {
            preferredBet = maxBet;
        }

        money -= preferredBet;

        return preferredBet;
    }

    public void winMoney(int winnings) {
        money += winnings;
    }

    public int getMoney() {
        return money;
    }
}
