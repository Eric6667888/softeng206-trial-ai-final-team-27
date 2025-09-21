package nz.ac.auckland.se206.timer;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

/**
 * A countdown timer for displaying the verdict. It counts down from 60 seconds and triggers an
 * action when the time expires.
 */
public final class VerdictTimer {
  private final IntegerProperty secondsLeft = new SimpleIntegerProperty();
  private final Timeline timeline = new Timeline();
  private boolean running = false;
  private Runnable onExpire = () -> {};

  public VerdictTimer(int totalSeconds) {
    reset(totalSeconds);
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.getKeyFrames().setAll(new KeyFrame(Duration.seconds(1), e -> tick()));
  }

  public void setOnExpire(Runnable onExpire) {
    this.onExpire = (onExpire != null) ? onExpire : () -> {};
  }

  public IntegerProperty secondsLeftProperty() {
    return secondsLeft;
  }

  public int getSecondsLeft() {
    return secondsLeft.get();
  }

  public void reset(int totalSeconds) {
    timeline.stop();
    running = false;
    secondsLeft.set(Math.max(0, totalSeconds));
  }

  public void start() {
    if (secondsLeft.get() <= 0) {
      return; // Do not start if time is already up
    }
    running = true;
    timeline.playFromStart();
  }

  public void stop() {
    running = false;
    timeline.stop();
  }

  private void tick() {
    if (!running) {
      return; // Skip the first tick to ensure accurate timing
    }
    int s = secondsLeft.get();
    if (s > 1) {
      secondsLeft.set(s - 1);
    } else if (s == 1) {
      secondsLeft.set(0);
      stop();
      onExpire.run();
    }
  }
}
