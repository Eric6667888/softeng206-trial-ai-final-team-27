package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class Timer {
  public static Timer getInstance(int initialTime) {
    if (instance == null) {
      instance = new Timer(initialTime);
    }
    return instance;
  }

  public static Timer getInstance() {
    if (instance == null) {
      throw new IllegalStateException("Timer not initialized. Call getInstance(int) first.");
    }
    return instance;
  }

  private static Timer instance;
  private int timeRemaining;
  private Label timerLabel;
  private Timeline timeline;

  private Timer(int initialTime) {
    this.timeRemaining = initialTime;
    this.timerLabel = new Label(formatTime(timeRemaining));
    this.timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                e -> {
                  if (timeRemaining > 0 && timeRemaining != 10) {
                    timeRemaining--;
                    timerLabel.setText(formatTime(timeRemaining));
                  } else if (timeRemaining == 10) {
                    timeRemaining--;
                    try {
                      App.setRoot("MakeGuess");
                    } catch (IOException e1) {
                      e1.printStackTrace();
                    }
                  } else {
                    timeline.stop();
                    try {
                      App.setRoot("NotGuilty");
                    } catch (IOException e1) {
                      e1.printStackTrace();
                    }
                  }
                }));
    timeline.setCycleCount(Timeline.INDEFINITE);
  }

  private String formatTime(int seconds) {
    int minutes = seconds / 60;
    int remainingSeconds = seconds % 60;
    return String.format("%02d:%02d", minutes, remainingSeconds);
  }

  public void start() {
    timeline.play();
  }

  public Label getLabel() {
    return timerLabel;
  }

  public void reset(int newInitialTime) {
    timeline.stop();
    this.timeRemaining = newInitialTime;
    timerLabel.setText(formatTime(timeRemaining));
  }

  public void stop() {
    if (timeline != null) {
      timeline.stop();
    }
  }
}
