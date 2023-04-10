module iniarski.blackjack {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    opens iniarski.blackjack to javafx.fxml, org.apache.logging.log4j, org.apache.logging.log4j.core;
    exports iniarski.blackjack;
}
