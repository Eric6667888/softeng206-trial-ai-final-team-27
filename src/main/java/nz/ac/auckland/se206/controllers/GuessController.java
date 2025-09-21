package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameSession;
import nz.ac.auckland.se206.GameStateContext;

public class GuessController {
  @FXML private Label lblTimer;
  private GameSession session = GameStateContext.getSession();

  @FXML
  public void initialize() throws ApiProxyException {
    GameSession session = GameStateContext.getSession();

    lblTimer.textProperty().unbind();
    lblTimer
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> format(session.getRoundTimer().getSecondsLeft()),
                session.getRoundTimer().secondsLeftProperty()));
  }

  @FXML
  private void onYesClicking() throws IOException {
    session.getRoundTimer().stop();
    App.setRoot("Guilty");
  }

  @FXML
  private void onNoClicking() throws IOException {
    session.getRoundTimer().stop();
    App.setRoot("NotGuilty");
  }

  private String format(int totalSeconds) {
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }
}
