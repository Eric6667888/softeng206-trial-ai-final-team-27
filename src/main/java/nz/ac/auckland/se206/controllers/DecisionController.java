package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameSession;
import nz.ac.auckland.se206.GameStateContext;

public class DecisionController {

  @FXML private Label result;
  @FXML private Label gptFeedback;

  @FXML
  public void onReturnClicked() throws IOException {
    GameSession s = GameStateContext.getSession();
    if (s != null) {
      s.stopAll();
    }
    App.setRoot("landing");
  }
}
