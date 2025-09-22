package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import nz.ac.auckland.se206.timer.RoundTimer;
import nz.ac.auckland.se206.timer.VerdictTimer;

public final class GameSession {
  private final RoundTimer roundTimer = new RoundTimer(300);
  private final boolean[] flashbackPlayed = new boolean[4]; // Index 0 unused, only 1,2,3 used
  // personId 1,2,3
  private final Map<Integer, List<FlashbackSlide>> flashbacks = new HashMap<>();
  private VerdictTimer verdictTimer;
  private boolean roundStarted = false;
  private Runnable onRoundExpire;
  private boolean verdictStarted = false;
  private int currentFlashbackPid = -1;

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

  public void resetForNewGame(int totalSeconds) {

    stopAll();

    currentFlashbackPid = -1;
    System.out.println("[GameSession] resetForNewGame");
    Arrays.fill(flashbackPlayed, false);

    resetAndStartNewRound(totalSeconds);
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

  public boolean isFlashbackPlayed(int personId) {
    return flashbackPlayed[personId];
  }

  public void setFlashbackPlayed(int personId) {
    if (personId < 0 || personId >= flashbacks.size()) {
      System.err.println("[Flashback] Invalid personId=" + personId);
      return;
    }
    flashbackPlayed[personId] = true;
  }

  public List<FlashbackSlide> getFlashback(int pid) {
    if (pid < 0 || pid >= flashbacks.size()) {
      System.err.println("[GameSession] getFlashback invalid pid=" + pid);
      return Collections.emptyList();
    }
    return flashbacks.get(pid);
  }

  public int getCurrentFlashbackPid() {
    return currentFlashbackPid;
  }

  public void setCurrentFlashbackPid(int personId) {
    currentFlashbackPid = personId;
  }

  public void loadFlashbacksIfNeeded() {
    if (!flashbacks.isEmpty()) {
      return;
    }
    // Need new pictures and captions
    flashbacks.put(
        0,
        List.of(
            new FlashbackSlide("/images/flashbacks/person0_slide0.png", "I love painting."),
            new FlashbackSlide(
                "/images/flashbacks/person0_slide1.png", "I often visit art galleries."),
            new FlashbackSlide(
                "/images/flashbacks/person0_slide2.png", "I have a pet parrot named Picasso.")));
    flashbacks.put(
        1,
        List.of(
            new FlashbackSlide("/images/flashbacks/person1_slide0.png", "I enjoy cooking."),
            new FlashbackSlide("/images/flashbacks/person1_slide1.png", "I often try new recipes."),
            new FlashbackSlide(
                "/images/flashbacks/person1_slide2.png", "I have a collection of cookbooks.")));
    flashbacks.put(
        2,
        List.of(
            new FlashbackSlide("/images/flashbacks/person2_slide0.png", "I love hiking."),
            new FlashbackSlide(
                "/images/flashbacks/person2_slide1.png", "I often explore new trails."),
            new FlashbackSlide(
                "/images/flashbacks/person2_slide2.png", "I have a blog about my adventures.")));
  }

  public void stopAll() {
    roundTimer.stop();
    if (verdictTimer != null) {
      verdictTimer.stop();
    }
  }
}
