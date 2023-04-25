module iniarski.blackjack {
    requires javafx.controls;
    requires javafx.fxml;
    

    opens iniarski.blackjack to javafx.fxml;
    exports iniarski.blackjack;
}
