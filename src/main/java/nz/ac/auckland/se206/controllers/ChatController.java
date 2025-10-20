package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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
import nz.ac.auckland.se206.PopUpManager;
import nz.ac.auckland.se206.prompts.PromptEngineering;

/**
 * Controller class for the chat view. Handles user interactions and communication with the GPT
 * model via the API proxy.
 */
public class ChatController {

  @FXML private ScrollPane chatScrollPane;
  @FXML private VBox chatContainer;
  @FXML private TextField txtInput;
  @FXML private Button btnSend;

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

    // Set up scroll pane and chat container properties
    if (chatScrollPane != null && chatContainer != null) {
      // Configure ScrollPane
      chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
      chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
      chatScrollPane.setFitToWidth(true);

      // Configure VBox
      chatContainer.setFillWidth(true);
      chatContainer.setMaxWidth(Double.MAX_VALUE);
      chatContainer.setPrefWidth(Double.MAX_VALUE);

      // Add listener to auto-scroll when content height changes
      chatContainer
          .heightProperty()
          .addListener(
              (observable, oldValue, newValue) -> {
                Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
              });
    }

    txtInput
        .sceneProperty()
        .addListener(
            (observable, oldScene, newScene) -> {
              if (newScene != null) {
                // Load Eurostile font
                try {
                  Font eurostileFont =
                      Font.loadFont(getClass().getResourceAsStream("/fonts/eurostile.TTF"), 14);
                  if (eurostileFont != null) {
                    System.out.println("Eurostile font loaded successfully for chat");
                  } else {
                    System.out.println("Failed to load Eurostile font for chat");
                  }
                } catch (Exception e) {
                  System.out.println("Error loading Eurostile font for chat: " + e.getMessage());
                }

                // Load cyberpunk chat CSS
                newScene
                    .getStylesheets()
                    .add(getClass().getResource("/css/chat.css").toExternalForm());

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

  @FXML private Label fbLabel;
  @FXML private Label lblTimer;
  @FXML private Button btnViewEvidence1;
  @FXML private Button btnViewEvidence2;
  @FXML private Button btnViewEvidence3;
  private boolean firstViewEvidPerson1 =
      true; // Track if it's the first time viewing evidence, for AI defendant
  private boolean firstViewEvidPerson2 = true; // for AI witness
  private boolean firstViewEvidPerson3 = true; // for Human witness

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

  /** Applies the appropriate theme styling based on the current profession. */
  private void applyTheme() {
    if (chatScrollPane == null) {
      return;
    }

    // Clear existing style classes
    chatScrollPane
        .getStyleClass()
        .removeAll("chat-scroll-pane", "chat-scroll-pane-defendant", "chat-scroll-pane-witness");

    // Apply theme-specific style class
    if ("AI defendant".equals(profession)) {
      chatScrollPane.getStyleClass().add("chat-scroll-pane-defendant");
    } else if ("AI witness".equals(profession)) {
      chatScrollPane.getStyleClass().add("chat-scroll-pane-witness");
    } else {
      chatScrollPane.getStyleClass().add("chat-scroll-pane");
    }
  }

  /**
   * Sets the profession for the chat context and initializes the ChatCompletionRequest.
   *
   * @param profession the profession to set
   */
  public void setProfession(String profession) {
    this.profession = profession;

    // Apply theme-specific styling to scroll pane
    applyTheme();

    try {
      ConversationManager manager = ConversationManager.getInstance();

      // Get or create chat request for this profession
      chatCompletionRequest = manager.getChatRequest(profession);

      // Restore chat history to UI
      loadChatHistory(manager.getChatHistory(profession));

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
   * Loads chat history from a string and displays it as bubbles.
   *
   * @param chatHistory the chat history string
   */
  private void loadChatHistory(String chatHistory) {
    if (chatHistory == null || chatHistory.trim().isEmpty()) {
      return;
    }

    // Clear existing chat bubbles
    chatContainer.getChildren().clear();

    // Set up VBox properties to prevent horizontal scrolling
    chatContainer.setFillWidth(true);
    chatContainer.setMaxWidth(Double.MAX_VALUE);
    chatContainer.setPrefWidth(Double.MAX_VALUE);

    // Parse the chat history and create bubbles
    String[] messages = chatHistory.split("\n\n");
    for (String message : messages) {
      if (message.trim().isEmpty()) {
        continue;
      }

      // Parse role and content
      if (message.startsWith("user: ")) {
        String content = message.substring(6);
        HBox userBubble = createChatBubble(content, true);
        chatContainer.getChildren().add(userBubble);
      } else if (message.startsWith("assistant: ")) {
        String content = message.substring(11);
        HBox aiBubble = createChatBubble(content, false);
        chatContainer.getChildren().add(aiBubble);
      }
    }

    // Auto-scroll to bottom
    Platform.runLater(
        () -> {
          scrollToBottom();
        });
  }

  /**
   * Creates a chat bubble for displaying messages.
   *
   * @param text the message text
   * @param isUser true if the message is from the user, false if from AI
   * @return HBox containing the styled message bubble
   */
  private HBox createChatBubble(String text, boolean isUser) {
    Label label = new Label(text);
    label.setWrapText(true);
    label.setMinWidth(150);
    label.setPrefWidth(200);
    label.setMaxWidth(280);
    label.setPadding(new Insets(5));

    // Determine styling based on profession and message type
    String styleClass;
    if (isUser) {
      if ("AI defendant".equals(profession)) {
        styleClass = "user-message-bubble-human";
      } else if ("AI witness".equals(profession)) {
        styleClass = "user-message-bubble-ai";
      } else {
        styleClass = "user-message-bubble";
      }
    } else {
      if ("AI defendant".equals(profession)) {
        styleClass = "ai-message-bubble-human";
      } else if ("AI witness".equals(profession)) {
        styleClass = "ai-message-bubble-ai";
      } else {
        styleClass = "ai-message-bubble";
      }
    }

    label.getStyleClass().add(styleClass);

    HBox messageContainer = new HBox(label);
    messageContainer.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
    messageContainer.setPadding(new Insets(5, 10, 5, 10));
    messageContainer.setMaxWidth(Double.MAX_VALUE);
    messageContainer.setPrefWidth(Double.MAX_VALUE);
    messageContainer.setFillHeight(false);

    return messageContainer;
  }

  /**
   * Appends a chat message to the chat container.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(ChatMessage msg) {
    boolean isUser = "user".equals(msg.getRole());
    HBox messageBubble = createChatBubble(msg.getContent(), isUser);

    Platform.runLater(
        () -> {
          chatContainer.getChildren().add(messageBubble);
          // Force immediate scroll to bottom with multiple attempts
          scrollToBottom();
        });

    // Store in conversation manager
    String messageText = msg.getRole() + ": " + msg.getContent() + "\n\n";
    ConversationManager.getInstance().appendToHistory(profession, messageText);
  }

  /** Scrolls the chat to the bottom with multiple attempts to ensure it works. */
  private void scrollToBottom() {
    // Immediate attempt
    chatScrollPane.setVvalue(1.0);

    // Force layout and try again
    chatContainer.applyCss();
    chatContainer.layout();
    chatScrollPane.setVvalue(1.0);

    // Final attempt with delay
    Platform.runLater(
        () -> {
          chatScrollPane.applyCss();
          chatScrollPane.layout();
          chatScrollPane.setVvalue(1.0);

          // One more attempt with slight delay
          Platform.runLater(
              () -> {
                chatScrollPane.setVvalue(1.0);
              });
        });
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
          // Get chat message form user and send to GPT
          @Override
          protected ChatMessage call() throws Exception {
            chatCompletionRequest.addMessage(msg);
            ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
            Choice result = chatCompletionResult.getChoices().iterator().next();
            chatCompletionRequest.addMessage(result.getChatMessage());
            return result.getChatMessage();
          }

          // If succeeded then append response from GPT into chat
          @Override
          protected void succeeded() {
            Platform.runLater(
                () -> {
                  ChatMessage response = getValue();
                  appendChatMessage(response);
                });
          }

          // If failed print on terminal stack trace
          @Override
          protected void failed() {
            Platform.runLater(
                () -> {
                  System.err.println("ChatGPT API call failed: " + getException().getMessage());
                  getException().printStackTrace();
                });
          }
        };
    // Create background thread to run task to increase speed
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
    // Send message to GPT
    GameSession session = GameStateContext.getSession();
    String message = txtInput.getText().trim();
    // If message empty then don't do anything
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
          // Send chat message to GPT
          protected ChatMessage call() throws Exception {
            // Check if other chat knowledge needed
            if (crossChatContext != null) {
              chatCompletionRequest.addMessage(crossChatContext);
            }
            // Get user response and send to GPT
            chatCompletionRequest.addMessage(msg);
            ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
            Choice result = chatCompletionResult.getChoices().iterator().next();
            chatCompletionRequest.addMessage(result.getChatMessage());
            return result.getChatMessage();
          }

          @Override
          protected void succeeded() {
            // If GPT response succeeded, append response into chatroom and make send button
            // unavaiable until GPT response sent
            Platform.runLater(
                () -> {
                  ChatMessage response = getValue();
                  appendChatMessage(response);
                  btnSend.setDisable(false);
                });
          }

          @Override
          protected void failed() {
            // If GPT has failed then print stacktrace onto terminal
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

    // temporary if else for testing new interactables
    if (buttonId.equals("btnViewEvidence2")) {
      PopUpManager.showPopup("SecurityCamera", "Security Footage");
    } else if (buttonId.equals("btnViewEvidence1")) {
      PopUpManager.showPopup("BrainWashBottle", "Brain Wash Bottle");
    } else {
      try {

        Stage imageStage = new Stage();
        imageStage.setTitle("Evidence");
        imageStage.initModality(Modality.APPLICATION_MODAL);
        Image image = null;

        switch (buttonId) {
          case "btnViewEvidence1": // rectPerson1, AI defendant, chat.fxml
            image = new Image(getClass().getResourceAsStream("/images/evidence1.png"));
            // If first time viewing evidence
            if (firstViewEvidPerson1) {
              appendChatMessage(
                  new ChatMessage("assistant", "This is the substance provided by my owner."));
              firstViewEvidPerson1 = false;
            } else {
              // Second or more times viewing evidence
              appendChatMessage(
                  new ChatMessage("assistant", "Sir, do you have questions about the substance?"));
            }
            break;
          case "btnViewEvidence2": // rectPerson2, AI witness, AIWitnessChat.fxml
            image = new Image(getClass().getResourceAsStream("/images/evidence2.png"));
            if (firstViewEvidPerson2) {
              appendChatMessage(
                  new ChatMessage(
                      "assistant",
                      "This is the security footage I retrieved, and it clearly shows who"
                          + " tampered with the food."));
              firstViewEvidPerson2 = false;
            } else {
              appendChatMessage(
                  new ChatMessage(
                      "assistant", "Sir, do you have any questions relevant to the footage?"));
            }
            break;
          case "btnViewEvidence3": // rectPerson3, Human witness, HumanChat.fxml
            image = new Image(getClass().getResourceAsStream("/images/evidence3.png"));
            if (firstViewEvidPerson3) {
              appendChatMessage(
                  new ChatMessage(
                      "assistant",
                      "I can't imagine who would do such a thing, what do they want from"
                          + " me?"));
              firstViewEvidPerson3 = false;
            } else {
              appendChatMessage(
                  new ChatMessage("assistant", "Why is my AI meeting someone I don't recognize?"));
            }
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

    } catch (Exception e) {
      System.err.println("Error showing image: " + e.getMessage());
    }
  }
}
