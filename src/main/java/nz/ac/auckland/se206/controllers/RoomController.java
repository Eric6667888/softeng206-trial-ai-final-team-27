package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameSession;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.speech.TextToSpeech;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController {

  @FXML private Rectangle rectCashier;
  @FXML private Rectangle rectPerson1;
  @FXML private Rectangle rectPerson2;
  @FXML private Rectangle rectPerson3;
  @FXML private Rectangle rectWaitress;

  private static boolean isFirstTimeInit = true;
  private static GameStateContext context = new GameStateContext();
  @FXML private Label lblTimer;

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {
    GameSession session = GameStateContext.getSession();
    lblTimer.textProperty().unbind();
    lblTimer
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> format(session.getRoundTimer().getSecondsLeft()),
                session.getRoundTimer().secondsLeftProperty()));

    if (isFirstTimeInit) {
      TextToSpeech.speak("Click on characters to learn their perspective. ");
      isFirstTimeInit = false;
    }

    session.getRoundTimer().reset(300);

    // Start the round timer and set up the end-of-round behavior (5 minutes, then 1 minute)
    session.startRound(
        () -> {
          Platform.runLater(
              () -> {
                try {
                  App.setRoot("MakeGuess");
                  session.startVerdictWindow(
                      () -> {
                        Platform.runLater(
                            () -> {
                              try {
                                App.setRoot("NotGuilty");
                              } catch (IOException e) {
                                e.printStackTrace();
                              }
                            });
                      });

                } catch (IOException e) {
                  e.printStackTrace();
                }
              });
        });
  }

  private static String format(int totalSec) {
    int minutes = totalSec / 60;
    int seconds = totalSec % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " pressed");
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    System.out.println("Key " + event.getCode() + " released");
  }

  /**
   * Handles mouse clicks on rectangles representing people in the room.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleRectangleClick(MouseEvent event) throws IOException {
    Rectangle clickedRectangle = (Rectangle) event.getSource();
    String rectangleId = clickedRectangle.getId();

    context.handleRectangleClick(event, rectangleId);
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGuessClick(ActionEvent event) throws IOException {
    context.handleGuessClick();
  }
}
