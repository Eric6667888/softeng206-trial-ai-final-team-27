package nz.ac.auckland.se206.timer;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

/**
 * A common countdown timer for game phases. It counts down from a specified number of seconds and
 * triggers an action when the time expires.
 */
public class GameTimer {
  private final IntegerProperty secondsLeft = new SimpleIntegerProperty();
  private final Timeline timeline = new Timeline();
  private boolean running = false;
  private Runnable onExpire = () -> {};
  private final String timerName;

  protected GameTimer(int totalSeconds, String timerName) {
    this.timerName = timerName;
    reset(totalSeconds);
    timeline.setCycleCount(Animation.INDEFINITE);
    timeline.getKeyFrames().setAll(new KeyFrame(Duration.seconds(1), e -> tick()));
  }

  public void setOnExpire(Runnable onExpire) {
    this.onExpire = (onExpire != null) ? onExpire : () -> {};
    System.out.println("[" + timerName + "] onExpire set: " + this.onExpire);
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
    System.out.println("[" + timerName + "] reset to " + totalSeconds);
    timeline.stop();
    running = false;
    secondsLeft.set(Math.max(0, totalSeconds));
  }

  public void start() {
    System.out.println("[" + timerName + "] started"); // testing log
    System.out.println(
        "["
            + timerName
            + "] start @"
            + System.identityHashCode(this)
            + ", left="
            + secondsLeft.get());
    if (secondsLeft.get() <= 0) {
      return; // Do not start if time is already up
    }

    running = true;
    timeline.playFromStart();
  }

  public void stop() {
    System.out.println("[" + timerName + "] stop @" + System.identityHashCode(this));
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
      System.out.println("[" + timerName + "] reached zero, stop, onExpire=" + onExpire);
      stop();
      if (onExpire != null) {
        System.out.println("[" + timerName + "] expire fired");
        onExpire.run();
      }
    }
  }
}
