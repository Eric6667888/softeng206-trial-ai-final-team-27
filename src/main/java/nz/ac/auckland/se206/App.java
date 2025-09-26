package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import nz.ac.auckland.se206.controllers.ChatController;

/**
 * This is the entry point of the JavaFX application. This class initializes and runs the JavaFX
 * application.
 */
public class App extends Application {

  private static Scene scene;
  private static Stage primaryStage;

  /**
   * The main method that launches the JavaFX application.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    launch();
  }

  /**
   * Sets the root of the scene to the specified FXML file.
   *
   * @param fxml the name of the FXML file (without extension)
   * @throws IOException if the FXML file is not found
   */
  public static void setRoot(String fxml) throws IOException {
    scene.setRoot(loadFxml(fxml));
  }

  /**
   * Loads the FXML file and returns the associated node. The method expects that the file is
   * located in "src/main/resources/fxml".
   *
   * @param fxml the name of the FXML file (without extension)
   * @return the root node of the FXML file
   * @throws IOException if the FXML file is not found
   */
  private static Parent loadFxml(final String fxml) throws IOException {
    return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
  }

  /**
   * Opens the chat view and sets the profession in the chat controller.
   *
   * @param event the mouse event that triggered the method
   * @param profession the profession to set in the chat controller
   * @throws IOException if the FXML file is not found
   */
  public static void openChat(MouseEvent event, String profession, String rectangleId)
      throws IOException {
    String sceneName;
    // Set characters as buttons
    switch (rectangleId) {
      // AI defendant
      case "rectPerson1":
        sceneName = "chat";
        profession = "AI defendant";
        break;
      // AI witness - security
      case "rectPerson2":
        sceneName = "AIWitnessChat";
        profession = "AI witness";
        break;
      // Human witness - mayor
      case "rectPerson3":
        sceneName = "HumanChat";
        profession = "Human witness";
        break;
      default:
        sceneName = "chat";
        break;
    }
    // Load initial scene
    FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + sceneName + ".fxml"));
    Parent root = loader.load();

    ChatController chatController = loader.getController();
    chatController.setProfession(profession);
    // Create stage
    Stage stage;
    if (event == null) {
      stage = App.getPrimaryStage();
    } else {
      stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    scene = new Scene(root);
    stage.setScene(scene);
    stage.show();
  }

  /**
   * This method is invoked when the application starts. It loads and shows the "room" scene.
   *
   * @param stage the primary stage of the application
   * @throws IOException if the "src/main/resources/fxml/room.fxml" file is not found
   */
  @Override
  public void start(final Stage stage) throws IOException {
    primaryStage = stage;
    Parent root =
        loadFxml("landing"); // Takes user initially to landing page where they press play to begin
    scene = new Scene(root);
    stage.setScene(scene);
    stage.show();
    root.requestFocus();
  }

  // Get the stage
  public static Stage getPrimaryStage() {
    return primaryStage;
  }
}
