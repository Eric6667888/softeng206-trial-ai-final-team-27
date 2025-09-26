package nz.ac.auckland.se206.timer;

/**
 * A countdown timer for a game round. It counts down from a specified number of seconds and
 * triggers an action when the time expires.
 */
public final class RoundTimer extends GameTimer {

  public RoundTimer(int totalSeconds) {
    super(totalSeconds, "RoundTimer");
  }
}
