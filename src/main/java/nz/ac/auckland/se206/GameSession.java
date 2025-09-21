package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.application.Platform;
import nz.ac.auckland.se206.timer.RoundTimer;
import nz.ac.auckland.se206.timer.VerdictTimer;

public final class GameSession {
  private final RoundTimer roundTimer = new RoundTimer(300);
  private VerdictTimer verdictTimer;
  private boolean roundStarted = false;
  private Runnable onRoundExpire;
  private boolean verdictStarted = false;

  public RoundTimer getRoundTimer() {
    return roundTimer;
  }

  public boolean isRoundStarted() {
    return roundStarted;
  }

  public void configureRoundExpire(Runnable onRoundExpire) {
    this.onRoundExpire = onRoundExpire;
    roundTimer.setOnExpire(onRoundExpire);
  }

  public void startRoundOnce() {
    if (roundStarted) {
      return;
    }
    roundTimer.start();
    roundStarted = true;
  }

  public void resetAndStartNewRound(int seconds) {
    roundTimer.reset(seconds);
    roundStarted = false;
    if (onRoundExpire != null) {
      roundTimer.setOnExpire(onRoundExpire);
    }
    startRoundOnce();
  }

  public void startVerdictWindow(Runnable onVerdictExpire) {
    if (verdictTimer == null) {
      verdictTimer = new VerdictTimer(60);
    } else {
      verdictTimer.reset(60);
    }
    verdictTimer.setOnExpire(onVerdictExpire);
    verdictTimer.start();
  }

  public void transitionToVerdict(Runnable onVerdictExpire) {
    if (verdictStarted) {
      return;
    }
    verdictStarted = true;
    roundTimer.stop();
    Platform.runLater(
        () -> {
          try {
            App.setRoot("MakeGuess");
            if (verdictTimer == null) {
              verdictTimer = new VerdictTimer(60);
            } else {
              verdictTimer.reset(60);
            }
            verdictTimer.setOnExpire(onVerdictExpire);
            verdictTimer.start();
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }

  public VerdictTimer getVerdictTimer() {
    return verdictTimer;
  }

  public RoundTimer getRoundTimerInstance() {
    return roundTimer;
  }

  public void stopAll() {
    roundTimer.stop();
    if (verdictTimer != null) {
      verdictTimer.stop();
    }
  }
}
