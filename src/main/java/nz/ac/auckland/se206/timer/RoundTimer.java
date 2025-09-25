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
  private boolean running = false;
  private Runnable onExpire = () -> {};

  public RoundTimer(int totalSeconds) {
    reset(totalSeconds);
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.getKeyFrames().setAll(new KeyFrame(Duration.seconds(1), e -> tick()));
  }

  public void setOnExpire(Runnable onExpire) {
    this.onExpire = (onExpire != null) ? onExpire : () -> {};
    System.out.println("[RoundTimer] onExpire set: " + this.onExpire);
  }

  public IntegerProperty secondsLeftProperty() {
    return secondsLeft;
  }

  public int getSecondsLeft() {
    return secondsLeft.get();
  }

  public boolean isRunning() {
    return timeline.getStatus() == Animation.Status.RUNNING;
  }

  public void reset(int totalSeconds) {
    System.out.println("[RoundTimer] reset to " + totalSeconds);
    timeline.stop();
    running = false;
    secondsLeft.set(Math.max(0, totalSeconds));
  }

  public void start() {
    System.out.println(
        "[RoundTimer] start @" + System.identityHashCode(this) + ", left=" + secondsLeft.get());
    if (secondsLeft.get() <= 0) {
      return; // Do not start if time is already up
    }

    running = true;
    timeline.playFromStart();
  }

  public void stop() {
    System.out.println("[RoundTimer] stop @" + System.identityHashCode(this));
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
      System.out.println("[RoundTimer] reached zero, stop, onExpire=" + onExpire);
    }
  }
}
