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
  @FXML private Label lblTimer;
  @FXML private ChoiceBox<String> choiceBox;
  @FXML private TextArea textField;
  private String decision;

  private String[] options = {"Guilty", "Not Guilty"};

  private GameSession session = GameStateContext.getSession();

  @FXML
  public void initialize(URL arg0, ResourceBundle arg1) {
    GameSession session = GameStateContext.getSession();

    choiceBox.getItems().addAll(options);
    choiceBox.setOnAction(this::getOptions);

    if (session.getVerdictTimer() == null) {
      session.startVerdictWindow(
          () ->
              Platform.runLater(
                  () -> {
                    try {
                      App.setRoot("NotGuilty");
                    } catch (IOException e) {
                      e.printStackTrace();
                    }
                  }));
    }

    lblTimer.textProperty().unbind();
    lblTimer
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> format(session.getVerdictTimer().getSecondsLeft()),
                session.getVerdictTimer().secondsLeftProperty()));
  }

  public void getOptions(ActionEvent event) {
    this.decision = choiceBox.getValue();
  }

  public void submit(ActionEvent event) {
    String rationale = textField.getText();
    String guess = this.decision;

    System.out.println("Sending to GPT - Guess: " + guess + ", Rationale: " + rationale);

    // Send to GPT for analysis
    sendToGpt(rationale, guess);

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
      String analysisMessage =
          feedbackPrompt
              + "\n\n"
              + "User's Decision: "
              + (guess != null ? guess : "No decision made")
              + "\n"
              + "User's Reasoning: "
              + (rationale != null && !rationale.trim().isEmpty()
                  ? rationale
                  : "No reasoning provided");

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

                    // Print GPT feedback to terminal
                    System.out.println("=== GPT FEEDBACK ===");
                    System.out.println(response.getContent());
                    System.out.println("==================");
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
