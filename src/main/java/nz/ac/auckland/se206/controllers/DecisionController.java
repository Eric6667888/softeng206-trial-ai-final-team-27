package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameSession;
import nz.ac.auckland.se206.GameStateContext;

public class DecisionController {
  @FXML
  public void onReturnClicked() throws IOException {
    GameSession session = GameStateContext.getSession();
    if (session != null) {
      session.stopAll();
    }
    GameStateContext.clearSession();
    App.setRoot("landing");
  }
}
