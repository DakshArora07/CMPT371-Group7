package sfu.cmpt371.group7.game.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Console extends Application {
    static private Label countLabel;
    static private int totalCount = 0;
    // global variables required for the server, to send messages and to get the updates back from the server
    // in used to read the messages from the server
    // out used to send messages to the server
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage stage) {
        connectToServer(); // connect to the server before starting the game.
        getUpdatesFromServer(); // get the updates from the server
        countLabel = new Label("Total count: " + totalCount);
        countLabel.setStyle("-fx-font-size: 16px;");

        Button redButton = new Button("Join red team");
        redButton.setStyle("-fx-font-size: 14px; -fx-background-color: #ff5555; -fx-text-fill: white;");
        redButton.setOnAction(e -> {
            sendToServer("teamSelection red");
        });

        Button blueButton = new Button("Join blue team");
        blueButton.setStyle("-fx-font-size: 14px; -fx-background-color: #5555ff; -fx-text-fill: white;");
        blueButton.setOnAction(e -> {
            sendToServer("teamSelection blue");
        });

        HBox buttonBox = new HBox(10, redButton, blueButton);
        buttonBox.setPadding(new Insets(10));

        VBox root = new VBox(10,countLabel,  buttonBox);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 300, 200);
        stage.setTitle("Select team");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }

    private void sendToServer(String message) {
        out.println(message);
    }

    private void connectToServer(){
        try {
            socket = new Socket("localhost", 1234);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }
        catch (IOException e) {
            System.err.println("error in console -> connect to server");
            e.printStackTrace();
        }
    }

    private void updateCount() {
        totalCount++;
        countLabel.setText("Total count: " + totalCount);
    }

    private void getUpdatesFromServer(){
        // the only update going to be recieved, is the total new count of players
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("updateCount")) {
                        String[] tokens =  message.split(" ");
                        int newCount = Integer.parseInt(tokens[1]);
                        totalCount = newCount;
                        Platform.runLater(() -> countLabel.setText("Total count: " + totalCount));
                    }
                }
            } catch (IOException e) {
                System.err.println("error in console -> get updates from server");
                e.printStackTrace();
            }
        }).start();
    }
}
