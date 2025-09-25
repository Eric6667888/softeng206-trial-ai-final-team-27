package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameSession;
import nz.ac.auckland.se206.GameStateContext;

public class DecisionController implements Initializable {

  @FXML private Label result;
  @FXML private Label gptFeedback;

  private static DecisionController currentInstance;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    currentInstance = this;

    // Display the GPT feedback
    String feedback = GuessController.getGptFeedback();
    gptFeedback.setText(feedback);
    gptFeedback.setWrapText(true);
  }

  // Method to update GPT feedback from GuessController
  public static void updateGptFeedback(String feedback) {
    if (currentInstance != null) {
      Platform.runLater(
          () -> {
            currentInstance.gptFeedback.setText(feedback);
          });
    }
  }

  @FXML
  public void onReturnClicked() throws IOException {
    GameSession s = GameStateContext.getSession();
    if (s != null) {
      s.stopAll();
    }
    App.setRoot("landing");
  }
}
