package nz.ac.auckland.se206.timer;

/**
 * A countdown timer for displaying the verdict. It counts down from 60 seconds and triggers an
 * action when the time expires.
 */
public final class VerdictTimer extends GameTimer {

  public VerdictTimer(int totalSeconds) {
    super(totalSeconds, "VerdictTimer");
  }
}
