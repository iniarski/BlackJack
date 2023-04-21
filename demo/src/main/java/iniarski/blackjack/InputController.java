package iniarski.blackjack;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.*;

public class InputController extends Game {

    @FXML
    private Button button;

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

    static final Logger LOGGER = LogManager.getLogger(InputController.class);
    
    @FXML
    void handle(ActionEvent event) 
    {

        try 
        {
            if(Short.parseShort(tf1.getText()) < 0){tf1.clear();throw new NumberFormatException();}
            if(Integer.parseInt(tf2.getText()) < 0){tf2.clear();throw new NumberFormatException();}
            if(Integer.parseInt(tf3.getText()) < 0){tf3.clear();throw new NumberFormatException();}
            if(Integer.parseInt(tf4.getText()) < 0){tf4.clear();throw new NumberFormatException();}
            if(Integer.parseInt(tf5.getText()) < 0){tf5.clear();throw new NumberFormatException();}
            
            LOGGER.info("Button pressed");
            setnOfDecks(Short.parseShort(tf1.getText()));
            
            setmaxBet(Integer.parseInt(tf2.getText()));
            setminBet(Integer.parseInt(tf3.getText()));
            setstartingMoney(Integer.parseInt(tf4.getText()));
            sethandsPlayed(Integer.parseInt(tf5.getText()));
            LOGGER.info("Game Rules set");
            
            Game.main(null);
        } 
        catch (NumberFormatException e) 
        {
            
            Alert alert = new Alert(AlertType.ERROR, "Please, provide positive integers.", ButtonType.CLOSE);
            alert.showAndWait();

            LOGGER.error("Runtime error, string");
        }

    }

   

    

    
}
