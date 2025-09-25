package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameSession;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.prompts.PromptEngineering;

/**
 * Controller class for the chat view. Handles user interactions and communication with the GPT
 * model via the API proxy.
 */
public class ChatController {

  @FXML private TextArea txtaChat;
  @FXML private TextField txtInput;
  @FXML private Button btnSend;
  @FXML private Button btnViewEvidence1;
  @FXML private Button btnViewEvidence2;
  @FXML private Button btnViewEvidence3;
  @FXML private Label lblTimer;
  @FXML private Label fbLabel;

  private ChatCompletionRequest chatCompletionRequest;
  private String profession;

  /**
   * Initializes the chat view.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
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

    session.getRoundTimer().start();

    txtInput
        .sceneProperty()
        .addListener(
            (observable, oldScene, newScene) -> {
              if (newScene != null) {
                newScene.setOnKeyPressed(
                    e -> {
                      if (e.getCode().toString().equals("ENTER")) {
                        try {
                          onSendMessage();
                        } catch (ApiProxyException | IOException ex) {
                          ex.printStackTrace();
                        }
                      }
                    });
              }
            });
  }

  private static String format(int totalSeconds) {
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  /**
   * Generates the system prompt based on the profession.
   *
   * @return the system prompt string
   */
  private String getSystemPrompt() {
    Map<String, String> map = new HashMap<>();
    map.put("profession", profession);
    return PromptEngineering.getPrompt("chat.txt", map);
  }

  /**
   * Sets the profession for the chat context and initializes the ChatCompletionRequest.
   *
   * @param profession the profession to set
   */
  public void setProfession(String profession) {
    this.profession = profession;

    try {
      ConversationManager manager = ConversationManager.getInstance();

      // Get or create chat request for this profession
      chatCompletionRequest = manager.getChatRequest(profession);

      // Restore chat history to UI
      txtaChat.setText(manager.getChatHistory(profession));

      // Send introduction only if first time
      if (!manager.hasIntroduced(profession)) {
        runGpt(new ChatMessage("system", getSystemPrompt()));
        manager.markAsIntroduced(profession);
      }

    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  /**
   * Appends a chat message to the chat text area.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    String messageText = msg.getRole() + ": " + msg.getContent() + "\n\n";
    txtaChat.appendText(messageText);

    // Store in conversation manager
    ConversationManager.getInstance().appendToHistory(profession, messageText);
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  private void runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    Task<ChatMessage> gptTask =
        new Task<ChatMessage>() {
          @Override
          protected ChatMessage call() throws Exception {
            chatCompletionRequest.addMessage(msg);
            ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
            Choice result = chatCompletionResult.getChoices().iterator().next();
            chatCompletionRequest.addMessage(result.getChatMessage());
            return result.getChatMessage();
          }

          @Override
          protected void succeeded() {
            Platform.runLater(
                () -> {
                  ChatMessage response = getValue();
                  appendChatMessage(response);
                });
          }

          @Override
          protected void failed() {
            Platform.runLater(
                () -> {
                  System.err.println("ChatGPT API call failed: " + getException().getMessage());
                  getException().printStackTrace();
                });
          }
        };

    Thread thread = new Thread(gptTask);
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Sends a message to the GPT model.
   *
   * @param event the action event triggered by the send button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onSendMessage() throws ApiProxyException, IOException {
    GameSession session = GameStateContext.getSession();
    String message = txtInput.getText().trim();
    if (message.isEmpty()) {
      return;
    }

    int pid = session.getCurrentMemoryPid();
    session.setInteractedWithPerson(pid); // mark as interacted

    txtInput.clear();
    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);

    ConversationManager manager = ConversationManager.getInstance();
    ChatMessage crossChatContext = manager.getCrossChatContextMessage(profession);

    btnSend.setDisable(true);

    Task<ChatMessage> gptTask =
        new Task<ChatMessage>() {
          @Override
          protected ChatMessage call() throws Exception {
            if (crossChatContext != null) {
              chatCompletionRequest.addMessage(crossChatContext);
            }

            chatCompletionRequest.addMessage(msg);
            ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
            Choice result = chatCompletionResult.getChoices().iterator().next();
            chatCompletionRequest.addMessage(result.getChatMessage());
            return result.getChatMessage();
          }

          @Override
          protected void succeeded() {
            Platform.runLater(
                () -> {
                  ChatMessage response = getValue();
                  appendChatMessage(response);
                  btnSend.setDisable(false);
                });
          }

          @Override
          protected void failed() {
            Platform.runLater(
                () -> {
                  btnSend.setDisable(false);
                  System.err.println("ChatGPT API call failed: " + getException().getMessage());
                  getException().printStackTrace();
                });
          }
        };

    Thread thread = new Thread(gptTask);
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Navigates back to the previous view.
   *
   * @param event the action event triggered by the go back button
   * @throws ApiProxyException if there is an error communicating with the API proxy
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onGoBack(ActionEvent event) throws ApiProxyException, IOException {
    App.setRoot("room");
  }

  @FXML
  private void onViewEvidence(ActionEvent event) {
    Button sourceButton = (Button) event.getSource();
    String buttonId = sourceButton.getId();
    try {

      Stage imageStage = new Stage();
      imageStage.setTitle("Evidence");
      imageStage.initModality(Modality.APPLICATION_MODAL);
      Image image = null;

      switch (buttonId) {
        case "btnViewEvidence1": // rectPerson1, AI defendant, chat.fxml
          image = new Image(getClass().getResourceAsStream("/images/evidence1.png"));
          break;
        case "btnViewEvidence2": // rectPerson2, AI witness, AIWitnessChat.fxml
          image = new Image(getClass().getResourceAsStream("/images/evidence2.png"));
          break;
        case "btnViewEvidence3": // rectPerson3, Human witness, HumanChat.fxml
          image = new Image(getClass().getResourceAsStream("/images/evidence3.png"));
          break;
        default:
          break;
      }

      if (image == null) {
        System.err.println("No image found for button ID: " + buttonId);
        return;
      }

      ImageView imageView = new ImageView(image);
      imageView.setFitWidth(400);
      imageView.setFitHeight(300);
      imageView.setPreserveRatio(true);

      VBox layout = new VBox();
      layout.getChildren().add(imageView);

      Scene scene = new Scene(layout);
      imageStage.setScene(scene);
      imageStage.show();

    } catch (Exception e) {
      System.err.println("Error showing image: " + e.getMessage());
    }
  }
}
