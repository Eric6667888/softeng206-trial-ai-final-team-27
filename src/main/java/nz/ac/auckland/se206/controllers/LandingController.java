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

    s.resetAndStartNewRound(
        3); // If you want to test the timer, only change the line above change to 300 once done
    s.configureRoundExpire(
        () ->
            Platform.runLater(
                () -> {
                  try {
                    App.setRoot("MakeGuess");
                    s.transitionToVerdict(
                        () ->
                            Platform.runLater(
                                () -> {
                                  try {
                                    App.setRoot("NotGuilty");
                                  } catch (IOException e) {
                                    e.printStackTrace();
                                  }
                                }));
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                }));

    App.setRoot("room");
  }
}
