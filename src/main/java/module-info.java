module sfu.cmpt371.group7.game {
    requires javafx.controls;
    requires javafx.fxml;
    requires dotenv.java;


    opens sfu.cmpt371.group7.game to javafx.fxml;
    exports sfu.cmpt371.group7.game;
    opens sfu.cmpt371.group7.game.ui to javafx.fxml;
    exports sfu.cmpt371.group7.game.ui;
}