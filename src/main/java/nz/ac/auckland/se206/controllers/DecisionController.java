package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameSession;
import nz.ac.auckland.se206.GameStateContext;

public class DecisionController implements Initializable {
  private static DecisionController currentInstance;

  // Method to update GPT feedback from GuessController
  public static void updateGptFeedback(String feedback) {
    if (currentInstance != null) {
      Platform.runLater(
          () -> {
            currentInstance.gptFeedback.setText(feedback);
          });
    }
  }

  @FXML private Label gptFeedback;
  @FXML private Label result;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    currentInstance = this;

    // Set the result label based on user's decision
    String userChoice = GuessController.getUserDecision();
    if (result != null) {
      FadeTransition fade = new FadeTransition(Duration.seconds(1), result);
      fade.setFromValue(0);
      fade.setToValue(1);
      fade.play();
      if (GuessController.isNoDecision()) {
        result.setText("You haven't made a decision");
        result.setTextFill(Color.SALMON);
      } else if ("Not Guilty".equals(userChoice)) {
        result.setText("You're verdict is INCORRECT");
        result.setTextFill(Color.SALMON);
      } else {
        result.setText("You're verdict is CORRECT"); // "Guilty".equals(userChoice)
        result.setTextFill(Color.LIGHTGREEN);
      }
    }

    // Display the GPT feedback
    String feedback = GuessController.getGptFeedback();
    if (feedback != null && gptFeedback != null) {
      gptFeedback.setText(feedback);
      gptFeedback.setWrapText(true);
    }
  }

  @FXML
  private void onReturnClicked() throws IOException {
    // When return clicked, go back to landing page to restart
    GameSession s = GameStateContext.getSession();
    if (s != null) {
      s.stopAll();
    }
    // Reset data to be fresh for new game
    ConversationManager.getInstance().clearAllConversations();

    GuessController.resetForNewGame();
    // Take user to landing page
    App.setRoot("landing");
  }
}
