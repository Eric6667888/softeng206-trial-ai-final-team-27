package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameSession;
import nz.ac.auckland.se206.GameStateContext;

public class LandingController {
  // nz.ac.auckland.se206.controllers.LandingController

  @FXML private Button btnPlay;

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
}
