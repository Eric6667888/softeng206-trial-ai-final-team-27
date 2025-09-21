package nz.ac.auckland.se206;

import nz.ac.auckland.se206.timer.RoundTimer;
import nz.ac.auckland.se206.timer.VerdictTimer;

public final class GameSession {
  private final RoundTimer roundTimer = new RoundTimer(300);
  private VerdictTimer verdictTimer;

  public RoundTimer getRoundTimer() {
    return roundTimer;
  }

  public void startRound(Runnable onRoundExpire) {
    roundTimer.setOnExpire(onRoundExpire);
    roundTimer.start();
  }

  public void startVerdictWindow(Runnable onVerdictExpire) {
    verdictTimer = new VerdictTimer(60);
    verdictTimer.setOnExpire(onVerdictExpire);
    verdictTimer.start();
  }

  public void stopAll() {
    roundTimer.stop();
    if (verdictTimer != null) {
      verdictTimer.stop();
    }
  }
}
