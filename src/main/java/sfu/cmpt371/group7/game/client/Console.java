package sfu.cmpt371.group7.game.client;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import sfu.cmpt371.group7.game.model.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static sfu.cmpt371.group7.game.client.Menu.*;


/**
 * The Console class serves as a player's main menu screen in the Capture the Flag game.
 * A {@link Player} object can host a new game of join an existing one, choose a name and a
 * team and transition to the actual game.
 *
 * @see Player
 */
public class Console {
    /**
     * Minimum number of players required to start the game
     */
    private static final int NUM_PLAYERS = 4;

    /**
     * The Port number at which the server runs.
     */
    private static final int PORT = 65000;

    /**
     * Number of characters allowed in a player's name
     */
    private static final int NAME_LENGTH = 2;

    /**
     * The IP Address of the sever hosting the game.
     */
    private final String ip;

    /**
     * Label displaying number of players connected
     */
    private static Label countLabel;

    /**
     * Label informing new player's connection to the game
     */
    private static Label newPlayerLabel;

    /**
     * Total players joined
     */
    private static int totalCount = 0;

    /**
     * Space for the player to write their name
     */
    private TextField nameField;

    /**
     * Join red team
     */
    private Button redButton;

    /**
     * Join blue team
     */
    private Button blueButton;

    /**
     * Player name error message
     */
    private Label nameErrorLabel;


    /**
     * Input stream for network communication
     */
    private BufferedReader in;

    /**
     * Output stream for network communication
     */
    private PrintWriter out;

    /**
     * Player playing the game
     */
    private Player player;

    /**
     * JavaFX Stage to display the GUI
     */
    private final Stage primaryStage;

    /**
     * This method is responsible for connecting to the server, launching the game console and
     * listen for incoming server messages.
     */
    public Console(String ip) {
        this.ip = ip;
        this.primaryStage = new Stage();

        try {
            // Connect to the server
            connectToServer();

            // Create the UI
            createUI();

            // Start listening for server messages
            listenForServerMessages();
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            showConnectionError(primaryStage);
        }
    }

    /**
     * Method displaying connection error dialog
     * @param stage The main JavaFX stage
     */
    private void showConnectionError(Stage stage) {
        Label errorLabel = new Label("Failed to connect to server at " + ip + ":" + PORT);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> System.exit(0));

        VBox errorBox = new VBox(10, errorLabel, exitButton);
        errorBox.setPadding(new Insets(20));

        Scene errorScene = new Scene(errorBox, 300, 100);
        stage.setTitle("Connection Error");
        stage.setScene(errorScene);
        stage.show();
    }

    /**
     * Create the user interface
     */
    private void createUI() {

        // Create styled root container
        VBox root = setupRoot();

        // Game title
        //Label titleLabel = setupTitleLabel();

        // Player counter with styled box

        HBox counterBox = new HBox(10);
        counterBox.setAlignment(Pos.CENTER);
        counterBox.setPadding(new Insets(10, 20, 10, 20));
        counterBox.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 5;");

        Label counterPrefix = new Label("Players:");
        counterPrefix.setTextFill(Color.LIGHTGRAY);

        countLabel = new Label(totalCount + " / " + NUM_PLAYERS);
        countLabel.setTextFill(Color.WHITE);
        countLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        newPlayerLabel = new Label("New player joined");
        newPlayerLabel.setTextFill(Color.LIGHTGRAY);
        newPlayerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        counterBox.getChildren().addAll(counterPrefix, countLabel, newPlayerLabel);

        // Name input section
        VBox nameSection = new VBox(8);
        nameSection.setAlignment(Pos.CENTER);

        Label nameLabel = new Label("ENTER YOUR NAME (AT MOST "+ NAME_LENGTH +" CHARACTERS)");
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        nameField = new TextField();
        nameField.setPromptText("3 Characters Only");
        nameField.setPrefHeight(40);
        nameField.setMaxWidth(300);
        nameField.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 5; -fx-font-size: 14px;");

        // Add character limit to text field
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > NAME_LENGTH) {
                nameField.setText(oldValue);
            }
        });

        // Add error label for name validation
        nameErrorLabel = new Label("Name must be exactly 3 characters");
        nameErrorLabel.setTextFill(Color.ORANGE);
        nameErrorLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        nameErrorLabel.setVisible(false);

        nameSection.getChildren().addAll(nameLabel, nameField, nameErrorLabel);

        // Team selection buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        redButton = new Button("JOIN RED TEAM");
        redButton.setPrefWidth(150);
        redButton.setPrefHeight(50);
        redButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 5);");
        redButton.setOnMouseEntered(e -> redButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #c0392b; " +
                "-fx-text-fill: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 8);"));
        redButton.setOnMouseExited(e -> redButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 5);"));
        redButton.setOnAction(e -> {
            String playerName = nameField.getText().trim().toUpperCase();
            if (validatePlayerName(playerName)) {
                // Disable buttons to prevent multiple submissions
                redButton.setDisable(true);
                blueButton.setDisable(true);
                nameErrorLabel.setVisible(false);

                sendToServer("teamSelection red " + playerName);

                // Create player
                player = new Player("red", 0, 0, playerName);
                player.setName(playerName);
                player.setTeam("red");
            }
        });

        blueButton = new Button("JOIN BLUE TEAM");
        blueButton.setPrefWidth(150);
        blueButton.setPrefHeight(50);
        blueButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #3498db; " +
                "-fx-text-fill: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 5);");
        blueButton.setOnMouseEntered(e -> blueButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #2980b9; " +
                "-fx-text-fill: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 8);"));
        blueButton.setOnMouseExited(e -> blueButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #3498db; " +
                "-fx-text-fill: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 5);"));
        blueButton.setOnAction(e -> {
            String playerName = nameField.getText().trim().toUpperCase();
            if (validatePlayerName(playerName)) {
                // Disable buttons to prevent multiple submissions
                redButton.setDisable(true);
                blueButton.setDisable(true);
                nameErrorLabel.setVisible(false);

                sendToServer("teamSelection blue " + playerName);

                // Create player
                player = new Player("blue", 0, 0, playerName);
                player.setName(playerName);
                player.setTeam("blue");
            }
        });

        buttonBox.getChildren().addAll(redButton, blueButton);

//        // Game Instructions
//        VBox instructionsBox = new VBox(5);
//        instructionsBox.setAlignment(Pos.CENTER);
//        instructionsBox.setPadding(new Insets(15));
//        instructionsBox.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-background-radius: 5;");
//        instructionsBox.setMaxWidth(400);
//
//        Label instructionsTitle = new Label("HOW TO PLAY");
//        instructionsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
//        instructionsTitle.setTextFill(Color.WHITE);
//
//        Label instructionsText = new Label(
//                """
//                        • Capture flags by standing on them
//                        • Use W, A, S, D keys to move
//                        • First team to capture 2 flags wins"""
//        );
//        instructionsText.setTextFill(Color.LIGHTGRAY);
//        instructionsText.setWrapText(true);
//
//        instructionsBox.getChildren().addAll(instructionsTitle, instructionsText);

        // Waiting indicator (initially invisible)
        HBox waitingBox = new HBox(10);
        waitingBox.setAlignment(Pos.CENTER);
        waitingBox.setPadding(new Insets(10));
        waitingBox.setStyle("-fx-background-color: rgba(52, 152, 219, 0.3); -fx-background-radius: 5;");
        waitingBox.setVisible(false);

        ProgressIndicator waitingIndicator = new ProgressIndicator();
        waitingIndicator.setPrefSize(24, 24);
        waitingIndicator.setStyle("-fx-progress-color: white;");

        Label waitingLabel = new Label("Waiting for players...");
        waitingLabel.setTextFill(Color.WHITE);

        waitingBox.getChildren().addAll(waitingIndicator, waitingLabel);

        // Add all elements to root
        root.getChildren().addAll(counterBox, nameSection, buttonBox, waitingBox);
        root.setPadding(new Insets(100,30,30,30));

        // Create scene
        setupScene(primaryStage, root);
    }

    /**
     * This method validates player's name <br>
     * A 'valid name' is of length less than or equal to 3.
     *
     * @param name The name provided by the player
     *
     * @return true if name is valid, false otherwise
     */
    private boolean validatePlayerName(String name) {
        if (name.length() > NAME_LENGTH) {
            System.out.println("Name can be at most 3 " + NAME_LENGTH + " characters");

            // Show error message
            nameErrorLabel.setText("Name can be at most 3 " + NAME_LENGTH + " characters");
            nameErrorLabel.setVisible(true);

            // Visual indication of error
            nameField.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 5; -fx-font-size: 14px; -fx-border-color: #e74c3c; -fx-border-width: 2px;");

            // Reset styling after short delay
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(1.5), e -> nameField.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 5; -fx-font-size: 14px;"))
            );
            timeline.play();

            return false;
        }
        return true;
    }

    /**
     * This method sends a message to the server
     *
     * @param message The message to send
     */
    private void sendToServer(String message) {
        if (out != null) {
            System.out.println("Sending to server: " + message);
            out.println(message);
        }
    }

    /**
     * Connect to the server
     */
    private void connectToServer() throws IOException {
        try {
            Socket socket = new Socket(ip, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to server at " + ip + ":" + PORT);
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            throw e;
        }
    }

    /**
     * This method listens for messages from the server and appropriately handles them.
     */
    private void listenForServerMessages() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    String[] parts = message.split(" ");
                    String messageType = parts[0];
                    System.out.println("Received from server: " + message);

                    // Process messages based on type
                    switch (messageType) {
                        case "updateCount" -> handleUpdateCount(parts);
                        case "startGame" -> handleStartGame();
                        case "sendingPlayer" -> handlePlayerData(parts);
                        case "showPlayerJoined" -> showPlayerJoined(parts);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());

                // Show error on UI thread
                Platform.runLater(() -> {
                    if (primaryStage != null && primaryStage.isShowing()) {
                        showConnectionError(primaryStage);
                    }
                });
            }
        }).start();
    }

    /**
     * Handles the updateCount message
     *
     * @param parts The complete message received from the server
     */
    private void handleUpdateCount(String[] parts) {

        // updateCount <number of players>
        try {
            if (parts.length >= 2) {
                totalCount = Integer.parseInt(parts[1]);

                Platform.runLater(() -> countLabel.setText(totalCount + " / " + NUM_PLAYERS));
            }
        } catch (Exception e) {
            System.err.println("Error parsing update count message: " + e.getMessage());
        }
    }

    /**
     * Handle startGame message
     */
    private void handleStartGame() {

        Platform.runLater(() -> {
            try {
                System.out.println("Starting the game...");
                Stage mazeStage = new Stage();
                new Maze(ip, player).initiate(mazeStage);

                // Close the console window
                if (primaryStage != null) {
                    primaryStage.close();
                }
            } catch (Exception e) {
                System.err.println("Error starting game: " + e.getMessage());
            }
        });
    }

    /**
     * Handle sendingPlayer message
     *
     * @param parts The complete message received from the server
     */
    private void handlePlayerData(String[] parts) {
        try {
            //sendingPlayer <name> <team> <x> <y>

            if (parts.length >= 5 && player != null && parts[1].equals(player.getName())) {
                player.setName(parts[1]);
                player.setTeam(parts[2]);
                player.setX(Integer.parseInt(parts[3]));
                player.setY(Integer.parseInt(parts[4]));

                System.out.println("Updated player position: " + player.getX() + ", " + player.getY());
            }
        } catch (Exception e) {
            System.err.println("Error parsing player data: " + e.getMessage());
        }
    }

    /**
     * Handles showPlayerJoined message
     *
     * @param parts The complete message received from the server
     */
    private void showPlayerJoined(String[] parts) {
        String team = parts[1];
        String name = parts[2];

        Platform.runLater(() -> {
            newPlayerLabel.setText(name + " joined " + team + " team");
            newPlayerLabel.setVisible(true);
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> newPlayerLabel.setVisible(false)));
            timeline.play();
        });
    }
}