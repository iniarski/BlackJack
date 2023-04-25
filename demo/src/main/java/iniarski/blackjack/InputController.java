package iniarski.blackjack;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

public class InputController {

    @FXML
    private Button button;

    @FXML
    private Button playButton;

    @FXML
    private TextField tf1;

    @FXML
    private TextField tf2;

    @FXML
    private TextField tf3;

    @FXML
    private TextField tf4;

    @FXML
    private TextField tf5;

    @FXML
    private TextField tf6;

    private short Decks;
    private int min;
    private int max;
    private int money;
    private int hands;

    
    public InputController() 
    {

        
        
    }




    
    
    @FXML
    void handle(ActionEvent event) 
    {

        try 
        {
            if(Short.parseShort(tf1.getText()) < 0){tf1.clear();throw new NumberFormatException();}
            if(Short.parseShort(tf1.getText()) > 8)
            {
                tf1.clear(); 
                Alert alert = new Alert(AlertType.ERROR, "Please provide up to 8 decks", ButtonType.CLOSE);
                alert.showAndWait();

            }
            if(Integer.parseInt(tf2.getText()) < 0){tf2.clear();throw new NumberFormatException();}
            if(Integer.parseInt(tf3.getText()) < 0){tf3.clear();throw new NumberFormatException();}
            if(Integer.parseInt(tf4.getText()) < 0){tf4.clear();throw new NumberFormatException();}
            if(Integer.parseInt(tf5.getText()) < 0){tf5.clear();throw new NumberFormatException();}
            setnOfDecks(Short.parseShort(tf1.getText()));
            setMinBet(Integer.parseInt(tf2.getText()));
            setMaxBet(Integer.parseInt(tf3.getText()));
            setStartingMoney(Integer.parseInt(tf4.getText()));
            setHandsPlayed(Integer.parseInt(tf5.getText()));
            System.out.println (Decks);
            System.out.println (min);
            System.out.println (max);
            Game game = new Game(0);
            game.nOfDecks = getnOfDecks();
            game.maxBet = getMaxBet();
            game.minBet = getMinBet();
            game.startingMoney = getStartingMoney();
            game.handsPlayed = getStartingMoney();
            game.startWithPrinting();

            
            
            
            
            
            
        } 
        catch (NumberFormatException e) 
        {
            
            Alert alert = new Alert(AlertType.ERROR, "Please, provide positive integers.", ButtonType.CLOSE);
            alert.showAndWait();

            
        }

    }

    @FXML
    void handle2(ActionEvent event) 
    {
        Game.main(null);

    }

    public short getnOfDecks() {
        return Decks;
    }

    public void setnOfDecks(short Decks) {
        this.Decks = Decks;
    }

    public int getMinBet() {
        return min;
    }

    public void setMinBet(int min) {
        this.min = min;
    }

    public int getMaxBet() {
        return max;
    }

    public void setMaxBet(int max) {
        this.max = max;
    }

    public int getStartingMoney() {
        return money;
    }

    public void setStartingMoney(int money) {
        this.money = money;
    }

    public int getHandsPlayed() {
        return hands;
    }

    public void setHandsPlayed(int hands) {
        this.hands = hands;
    }

   

    

    
}
