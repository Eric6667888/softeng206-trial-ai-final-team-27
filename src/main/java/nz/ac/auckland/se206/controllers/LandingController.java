package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameSession;
import nz.ac.auckland.se206.GameStateContext;

public class LandingController implements Initializable {
  // nz.ac.auckland.se206.controllers.LandingController

  @FXML private Button btnPlay;
  @FXML private Label titleLabel;

  @FXML
  private void onPlay(ActionEvent event) throws ApiProxyException, IOException {
    GameSession s = GameStateContext.getSession();
    // Reset all chat history
    ConversationManager.getInstance().clearAllConversations();

    GuessController.resetForNewGame();

    s.resetForNewGame(300); // If you want to test the timer, only change the line above
    // Adjust scenarios for if round timer expires depending on what user has done in the time
    s.configureRoundExpire(
        () ->
            Platform.runLater(
                () -> {
                  // if user not talked to all 3 characters
                  try {
                    GameSession session = GameStateContext.getSession();
                    if (!session.haveAllThreeTalked()) {
                      App.setRoot("NotGuilty");
                      return;
                    }
                    // Else send user to make verdict scene

                    if (session.isVerdictStarted()) {
                      return;
                    }

                    session.startVerdictWindow(
                        () -> {
                          Platform.runLater(
                              () -> {
                                try {
                                  App.setRoot("GameOver");
                                } catch (IOException e) {
                                  e.printStackTrace();
                                }
                              });
                        });

                    App.setRoot("MakeGuess");

                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                }));

    App.setRoot("room");
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Load the custom font and apply it to the controls
    Font customFont140 =
        Font.loadFont(getClass().getResourceAsStream("/fonts/NeonVampire-AnRp.ttf"), 140);
    Font customFont29 =
        Font.loadFont(getClass().getResourceAsStream("/fonts/NeonVampire-AnRp.ttf"), 29);

    if (customFont140 != null && titleLabel != null) {
      titleLabel.setFont(customFont140);
      // Add stroke/outline programmatically
      titleLabel.setStyle(
          "-fx-text-fill: #ffd900; -fx-stroke: black; -fx-stroke-width: 4px; -fx-stroke-type:"
              + " outside;");
    }

    if (customFont29 != null && btnPlay != null) {
      btnPlay.setFont(customFont29);
    }
  }
}
