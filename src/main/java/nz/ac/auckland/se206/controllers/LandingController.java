package nz.ac.auckland.se206.controllers;

import java.io.IOException;
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
    GameSession old = GameStateContext.getSession();
    if (old != null) {
      old.stopAll();
    }
    GameSession session = new GameSession();
    GameStateContext.setSession(session);
    session.getRoundTimer().reset(300);
    App.setRoot("room");
  }
}
