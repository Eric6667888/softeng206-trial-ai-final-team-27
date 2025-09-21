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
    GameStateContext.setSession(new GameSession());
    App.setRoot("room");
  }
}
