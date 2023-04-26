module iniarski.blackjack {
    requires javafx.controls;
    requires javafx.fxml;
            
                            
    opens iniarski.blackjack to javafx.fxml, javafx.controls;
    exports iniarski.blackjack;
}