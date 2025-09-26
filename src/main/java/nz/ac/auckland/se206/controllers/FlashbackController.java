package nz.ac.auckland.se206.controllers;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.FlashbackSlide;
import nz.ac.auckland.se206.GameSession;
import nz.ac.auckland.se206.GameStateContext;

/**
 * Controller class for the flashback view. Handles user interactions for navigating through
 * flashback slides.
 */
public final class FlashbackController {
  // Set definitions of labels/buttons in scene
  private static GameStateContext context =
      new GameStateContext(); // do not use except via goToMemory()

  // Format timer
  private static String format(int totalSeconds) {
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  @FXML private ImageView imageView;
  @FXML private Button btnNext;
  @FXML private Button btnPrevious;
  @FXML private Button btnExit;
  @FXML private Label lblCaption;
  @FXML private Label lblProgress;
  @FXML private Label lblTimer;

  private int pid = -1;
  private int idx = 0; // current slide index
  private List<FlashbackSlide> slides;

  @FXML
  public void initialize() {
    GameSession session = GameStateContext.getSession();
    // Set timer label to update of Flashback
    lblTimer.textProperty().unbind();
    lblTimer
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> format(session.getRoundTimer().getSecondsLeft()),
                session.getRoundTimer().secondsLeftProperty()));

    session.getRoundTimer().start();
    this.pid = session.getCurrentFlashbackPid();
    // session.loadFlashbacksIfNeeded();
    this.slides = session.getFlashback(pid);
    // if error occir print out error
    if (slides == null || slides.isEmpty()) {
      System.err.println("[Flashback] no slides for pid=" + pid);
      return;
    }

    show(0);

    // Add key event listener to the scene to handle keyboard navigation
    imageView
        .sceneProperty()
        .addListener(
            (observable, oldScene, newScene) -> {
              if (newScene != null) {
                newScene.setOnKeyPressed(
                    e -> {
                      switch (e.getCode()) {
                        case RIGHT:
                        case ENTER:
                        case SPACE:
                          onNextSlide();
                          break;
                        case LEFT:
                        case UP:
                          onPrev();
                          break;
                        case ESCAPE:
                          onExit();
                          break;
                        default:
                          break;
                      }
                    });
              }
            });
  }

  // Button handlers
  @FXML
  private void onNextSlide() {
    // Check if there slides to go forward, else go to memory
    if (idx < slides.size() - 1) {
      show(idx + 1);
    } else {
      onExit(); //
    }
  }

  // Go to previous slide
  @FXML
  // Check if can go previous slide
  private void onPrev() {
    if (idx > 0) {
      show(idx - 1);
    }
  }

  // Return to courtroom
  @FXML
  private void onExit() {
    // Check where to return
    GameSession session = GameStateContext.getSession();
    if (pid >= 0) {
      session.setFlashbackPlayed(pid);
    }

    goToMemory(pid);
  }

  // Navigate to the memory view corresponding to the given person ID
  private void goToMemory(int pid) {
    // Check if go stright to memory or to flashback
    if (pid < 0) {
      System.err.println("[Flashback] Invalid personId=" + pid);
      return;
    }
    // go to the person id memory and exception handle for bugs
    GameSession session = GameStateContext.getSession();
    session.setCurrentMemoryPid(pid);
    try {
      String rectangleId = "rectPerson" + (pid + 1); //
      context.handleRectangleClick(null, rectangleId);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Display the slide at the specified index
  private void show(int i) {
    this.idx = i;
    FlashbackSlide s = slides.get(idx);
    // Check if image for slide is available
    URL url =
        Objects.requireNonNull(
            App.class.getResource(s.getImagePath()), "Missing resource: " + s.getImagePath());
    // Set up flashback to show image and text
    imageView.setImage(new Image(url.toExternalForm(), true));
    lblCaption.setText(s.getCaption());
    lblProgress.setText((i + 1) + " / " + slides.size());
    btnPrevious.setDisable(i == 0);
    btnNext.setText(i == slides.size() - 1 ? "Finish" : "Next");
  }
}
