package iniarski.blackjack;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.*;

public class InputController {

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

    private short nOfDecks;
    private int minBet;
    private int maxBet;
    private int startingMoney;
    private int handsDealt;

    @FXML
    void handle(ActionEvent event) {

        if (tf1.getText() == null || tf2.getText() == null || tf3.getText() == null || tf4.getText() == null
                || tf5.getText() == null) {
            // TODO: Create error message window
        }
        LOGGER.info("Button pressed");
        nOfDecks = Short.parseShort(tf1.getText());
        LOGGER.info("nOfDecks  = " + nOfDecks);
        minBet = Integer.parseInt(tf2.getText());
        LOGGER.info("minBet = " + minBet);
        maxBet = Integer.parseInt(tf3.getText());
        LOGGER.info("maxBet = " + maxBet);
        startingMoney = Integer.parseInt(tf4.getText());
        LOGGER.info("money = " + startingMoney);
        handsDealt = Integer.parseInt(tf5.getText());
        LOGGER.info("Hands = " + handsDealt);

    }

}
