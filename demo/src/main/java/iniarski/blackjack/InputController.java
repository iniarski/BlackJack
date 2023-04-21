package iniarski.blackjack;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
            LOGGER.info("Button pressed");
            setnOfDecks(Short.parseShort(tf1.getText()));
            
            setmaxBet(Integer.parseInt(tf2.getText()));
            setminBet(Integer.parseInt(tf3.getText()));
            setstartingMoney(Integer.parseInt(tf4.getText()));
            sethandsPlayed(Integer.parseInt(tf5.getText()));
            LOGGER.info("Game Rules set");
            
            Game.main(null);
        } catch (NumberFormatException e) 
        {
            
            // TODO: Create an error message.
            LOGGER.error("Runtime error, string");
        }

    }

   

    

    
}
