package nz.ac.auckland.se206.controllers;

import java.io.IOException;
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
  @FXML private Rectangle rectVerdict;

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
    updateVerdictState(session.haveAllThreeTalked()); // Enable verdict if all three have talked
    lblTimer.textProperty().unbind();
    lblTimer
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () ->
                    String.format(
                        "%02d:%02d",
                        session.getRoundTimer().getSecondsLeft() / 60,
                        session.getRoundTimer().getSecondsLeft() % 60),
                session.getRoundTimer().secondsLeftProperty()));

    if (rectVerdict != null) {
      rectVerdict.setMouseTransparent(!session.haveAllThreeTalked());
      session
          .haveAllThreeTalkedProperty()
          .addListener(
              (obs, oldV, newV) -> {
                rectVerdict.setMouseTransparent(!newV);
              });
    }

    if (isFirstTimeInit) {
      TextToSpeech.speak("Click on characters to learn their perspective. ");
      isFirstTimeInit = false;
    }

    if (!session.getRoundTimer().isRunning()
        && session.getRoundTimer().getSecondsLeft() > 0
        && session.isRoundStarted()) {
      session.getRoundTimer().start();
    }
  }

  private void updateVerdictState(boolean canclick) {
    rectVerdict.setDisable(!canclick);
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
    Rectangle clickedRectangle = (Rectangle) event.getSource(); //
    String rectangleId = clickedRectangle.getId(); //
    // Extract the ID number
    int pid = Integer.parseInt(clickedRectangle.getUserData().toString());
    System.out.println("Clicked on rectangle: " + rectangleId + ", pid=" + pid); // test log

    if (pid < 0 || pid >= 3) {
      System.err.println("[Room] invalid pid=" + pid + ", rectangleId=" + rectangleId);
      return;
    }

    GameSession session = GameStateContext.getSession();

    if (!session.isFlashbackPlayed(pid)) {
      session.setCurrentFlashbackPid(pid);
      session.loadFlashbacksIfNeeded();
      App.setRoot("Flashback");
    } else {
      context.handleRectangleClick(event, rectangleId); // go to memory
      session.setInteractedWithPerson(pid);
      session.setCurrentMemoryPid(pid);
    }
  }

  public GameStateContext getContext() {
    return context;
  }

  @FXML
  private void onVerdictClicked(MouseEvent event) {
    GameSession session = GameStateContext.getSession();
    if (!session.haveAllThreeTalked()) {
      System.out.println("[Room] Cannot click verdict before talking to all three.");
      return;
    }
    session.transitionToVerdict(null);
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
