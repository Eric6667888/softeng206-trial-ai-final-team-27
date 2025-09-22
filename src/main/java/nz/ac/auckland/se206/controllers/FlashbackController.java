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

  private int pid;
  private int idx = 0;
  private List<FlashbackSlide> slides;

  @FXML
  public void initialize() {
    GameSession session = GameStateContext.getSession();
    this.pid = session.getCurrentFlashbackPid();
    this.slides = session.getFlashback(pid);
    show(0);

    imageView
        .getScene()
        .setOnKeyPressed(
            e -> {
              switch (e.getCode()) {
                case RIGHT, ENTER, SPACE -> onNext();
                case LEFT, UP -> onPrev();
                case ESCAPE -> onExit();
                default -> {}
              }
            });
  }

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
    session.setFlashbackPlayed(pid);
    goToMemory(pid);
  }

  private void goToMemory(int pid) {
    try {
      App.setRoot("Memory_" + pid);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void show(int i) {
    this.idx = i;
    FlashbackSlide s = slides.get(idx);

    URL url =
        Objects.requireNonNull(
            App.class.getResource(s.imagePath()), "Missing resource: " + s.imagePath());
    imageView.setImage(new Image(url.toExternalForm(), true));
    lblCaption.setText(s.caption());
    lblProgress.setText((i + 1) + " / " + slides.size());
    btnPrevious.setDisable(i == 0);
    btnNext.setText(i == slides.size() - 1 ? "Finish" : "Next");
  }
}
