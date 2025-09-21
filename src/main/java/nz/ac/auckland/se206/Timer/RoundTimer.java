package nz.ac.auckland.se206.timer;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

/**
 * A countdown timer for a game round. It counts down from a specified number of seconds and
 * triggers an action when the time expires.
 */
public final class RoundTimer {
  private final IntegerProperty secondsLeft = new SimpleIntegerProperty();
  private final Timeline timeline = new Timeline();
  private Runnable onExpire = () -> {};

  public RoundTimer(int totalSeconds) {
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
    secondsLeft.set(totalSeconds);
  }

  public void start() {
    timeline.playFromStart();
  }

  public void stop() {
    timeline.stop();
  }

  private void tick() {
    int s = secondsLeft.get();
    if (s <= 0) {
      secondsLeft.set(s - 1);
    } else {
      stop();
      onExpire.run();
    }
  }
}
