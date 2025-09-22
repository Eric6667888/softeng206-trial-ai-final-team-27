package nz.ac.auckland.se206.controllers;

import java.net.URL;
import java.util.List;
import java.util.Objects;
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
  @FXML private ImageView imageView;
  @FXML private Button btnNext, btnPrevious, btnExit;
  @FXML private Label lblCaption, lblProgress;

  private int pid = -1;
  private int idx = 0; // current slide index
  private List<FlashbackSlide> slides;

  @FXML
  public void initialize() {
    GameSession session = GameStateContext.getSession();
    this.pid = session.getCurrentFlashbackPid();
    // session.loadFlashbacksIfNeeded();
    this.slides = session.getFlashback(pid);

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
                          onNext();
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
  private void onNext() {
    if (idx < slides.size() - 1) {
      show(idx + 1);
    }
  }

  @FXML
  private void onPrev() {
    if (idx > 0) {
      show(idx - 1);
    }
  }

  @FXML
  private void onExit() {
    GameSession session = GameStateContext.getSession();
    if (pid >= 0) {
      session.setFlashbackPlayed(pid);
    }

    goToMemory(pid);
  }

  // Navigate to the memory view corresponding to the given person ID
  private void goToMemory(int pid) {
    if (pid < 0) {
      System.err.println("[Flashback] Invalid personId=" + pid);
      return;
    }
    try {
      String fxml = "Memory_" + (pid + 1);
      App.setRoot(fxml);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Display the slide at the specified index
  private void show(int i) {
    this.idx = i;
    FlashbackSlide s = slides.get(idx);

    URL url =
        Objects.requireNonNull(
            App.class.getResource(s.getImagePath()), "Missing resource: " + s.getImagePath());
    imageView.setImage(new Image(url.toExternalForm(), true));
    lblCaption.setText(s.getCaption());
    lblProgress.setText((i + 1) + " / " + slides.size());
    btnPrevious.setDisable(i == 0);
    btnNext.setText(i == slides.size() - 1 ? "Finish" : "Next");
  }
}
