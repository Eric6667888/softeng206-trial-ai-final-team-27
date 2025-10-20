package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest.Model;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameSession;
import nz.ac.auckland.se206.GameStateContext;

public class GuessController implements Initializable {
  // Static field to store user's decision for the DecisionController
  private static String userDecision = null;

  // Static field to store GPT feedback for the DecisionController
  private static String gptFeedbackResponse = "Analyzing your decision...";

  private static boolean noDecision = false;

  // Reset static fields for new games
  public static void resetForNewGame() {
    gptFeedbackResponse = "Analyzing your decision...";
    userDecision = null;
    noDecision = false;
  }

  // Getter method for the GPT feedback
  public static String getGptFeedback() {
    return gptFeedbackResponse;
  }

  // Getter method for the user's decision
  public static String getUserDecision() {
    return userDecision;
  }

  public static boolean isNoDecision() {
    return noDecision;
  }

  private String decision;
  @FXML private ChoiceBox<String> choiceBox;
  @FXML private TextArea textField;
  @FXML private Label lblTimer;

  private String[] options = {"Guilty", "Not Guilty"};

  private GameSession session = GameStateContext.getSession();

  @FXML
  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    GameSession session = GameStateContext.getSession();
    // Set up choicebox to store user input and setup with guilty or not guilty
    choiceBox.getItems().setAll(options);
    choiceBox.getSelectionModel().clearSelection();
    choiceBox.setOnAction(this::getOptions);
    // Make timer for verdict scene of 1 minute
    lblTimer.textProperty().unbind();
    lblTimer
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> format(session.getVerdictTimer().getSecondsLeft()),
                session.getVerdictTimer().secondsLeftProperty()));
    // Auto submit user rationale if timer ran out
    session.setAutoSubmitAction(
        () -> {
          Platform.runLater(
              () -> {
                try {
                  System.out.println("[AutoSubmit] decision due to timer expiry.");
                  getOptions(null); // Ensure decision is captured
                  onSubmit(null);
                } catch (Exception e) {
                  e.printStackTrace();
                }
              });
        });
  }

  public void getOptions(ActionEvent event) {
    this.decision = choiceBox.getValue();
    if (this.decision == null || this.decision.isEmpty()) {
      noDecision = true;
    } else {
      noDecision = false;
    }
  }

  @FXML
  private void onSubmit(ActionEvent event) {
    getOptions(null); // Ensure we have the latest decision
    String rationale = textField.getText();
    String guess = this.decision;

    // Store the user's decision for the DecisionController
    userDecision = guess;

    System.out.println("Sending to GPT - Guess: " + guess + ", Rationale: " + rationale);

    // Send to GPT for analysis
    new Thread(
            () -> {
              try {
                sendToGpt(rationale, guess);
              } catch (Exception e) {
                System.err.println("Error sending to GPT: " + e.getMessage());
                e.printStackTrace();
              }
            })
        .start();

    // Navigate to guilty page after submitting
    try {
      session.getVerdictTimer().stop();
      App.setRoot("Guilty");
    } catch (IOException e) {
      System.err.println("Error navigating to Guilty page: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void sendToGpt(String rationale, String guess) {
    try {
      // Use ConversationManager to access previous conversations
      ConversationManager manager = ConversationManager.getInstance();
      ChatCompletionRequest chatCompletionRequest = manager.getChatRequest("analysis");

      chatCompletionRequest.setMaxTokens(900);
      chatCompletionRequest.setModel(Model.GPT_4_1_MINI);

      // Read the feedback prompt from the file
      String feedbackPrompt = "";
      try (InputStream inputStream =
          getClass().getClassLoader().getResourceAsStream("prompts/feedback.txt")) {
        if (inputStream != null) {
          feedbackPrompt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
      } catch (IOException e) {
        e.printStackTrace();
        // Fallback prompt if file reading fails
        feedbackPrompt = "Please provide feedback on the user's reasoning for their decision.";
      }

      // Create analysis message with current decision and rationale
      String userDecision = (guess != null ? guess : "No decision made");
      String userReasoning =
          (rationale != null && !rationale.trim().isEmpty() ? rationale : "No reasoning provided");

      // Determine if user chose AI to be guilty or not guilty
      String aiVerdict;
      if (userDecision.toLowerCase().contains("not guilty")) {
        aiVerdict = "The user chose that the AI is NOT GUILTY.";
      } else {
        aiVerdict = "The user chose that the AI is GUILTY.";
      }

      String analysisMessage =
          feedbackPrompt
              + "\n\n"
              + aiVerdict
              + "\n"
              + "User's Full Decision: "
              + userDecision
              + "\n"
              + "User's Reasoning: "
              + userReasoning;

      ChatMessage userMessage = new ChatMessage("user", analysisMessage);
      chatCompletionRequest.addMessage(userMessage);

      // Create task for GPT call
      Task<ChatMessage> gptTask =
          new Task<ChatMessage>() {
            @Override
            protected ChatMessage call() throws Exception {
              ChatCompletionResult result = chatCompletionRequest.execute();
              Choice choice = result.getChoices().iterator().next();
              return choice.getChatMessage();
            }

            @Override
            protected void succeeded() {
              Platform.runLater(
                  () -> {
                    ChatMessage response = getValue();

                    // Store GPT feedback for the DecisionController
                    gptFeedbackResponse = response.getContent();

                    // Update the DecisionController if it's loaded
                    DecisionController.updateGptFeedback(response.getContent());
                  });
            }

            @Override
            protected void failed() {
              Platform.runLater(
                  () -> {
                    System.err.println("GPT analysis failed: " + getException().getMessage());
                    getException().printStackTrace();
                  });
            }
          };

      // Start the task in a daemon thread
      Thread thread = new Thread(gptTask);
      thread.setDaemon(true);
      thread.start();

    } catch (ApiProxyException e) {
      System.err.println("Error setting up GPT analysis: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // Handle user clicking "Yes" button
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

  // Format timer display as mm:ss
  private String format(int totalSeconds) {
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    // Change color to red if less than 10 seconds
    if (totalSeconds < 10) {
      lblTimer.setTextFill(Color.RED);
    }

    return String.format("%02d:%02d", minutes, seconds);
  }
}
