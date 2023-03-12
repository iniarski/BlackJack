package iniarski.blackjack;

// class for handling miscellaneous tasks in the project

public class BlackjackUtil {

    private static BlackjackUtil instance = new BlackjackUtil();
    private BlackjackUtil(){
    }

    public static BlackjackUtil getInstance() {
        return instance;
    }

    public int calculateScore(int[] cardRanks) {

        int score = 0;
        boolean hasAce = false;

        for(int n : cardRanks) {
            if(n == 0) {
                hasAce = true;
            }

            if (n < 10){
                score += n + 1;
            } else {
                score += 10;
            }
        }

        if(hasAce && score <= 11) {
            score += 10;
        }

        return score;
    }
}
