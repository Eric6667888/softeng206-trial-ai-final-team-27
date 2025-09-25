package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import nz.ac.auckland.se206.timer.RoundTimer;
import nz.ac.auckland.se206.timer.VerdictTimer;

public final class GameSession {
  private final RoundTimer roundTimer = new RoundTimer(300);
  private final boolean[] flashbackPlayed = new boolean[4]; // Index 0 unused, only 1,2,3 used
  // personId 1,2,3
  private final boolean[] interactedWithPerson = new boolean[3];
  private final Map<Integer, List<FlashbackSlide>> flashbacks = new HashMap<>();
  private VerdictTimer verdictTimer;
  private boolean roundStarted = false;
  private boolean allThreeTalked = false;
  private BooleanProperty haveAllThreeTalked = new SimpleBooleanProperty(false);
  private Runnable onRoundExpire;
  private Runnable autoSubmitAction;
  private boolean verdictStarted = false;
  private int currentFlashbackPid = -1;
  private int currentMemoryPid = -1;

  public void setVerdictStarted(boolean started) {
    this.verdictStarted = started;
  }

  public boolean isVerdictStarted() {
    return verdictStarted;
  }

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
    haveAllThreeTalked.set(false);

    currentFlashbackPid = -1;
    System.out.println("[GameSession] resetForNewGame");
    Arrays.fill(flashbackPlayed, false);
    Arrays.fill(interactedWithPerson, false);
    allThreeTalked = false;
    verdictStarted = false;

    resetAndStartNewRound(totalSeconds);
  }

  public void startVerdictWindow(Runnable onVerdictExpire) {
    if (verdictStarted) {
      return;
    }
    verdictStarted = true;
    roundTimer.stop();
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

            if (verdictTimer == null) {
              verdictTimer =
                  new VerdictTimer(60); // If you want to test the timer, only change the line above
            } else {
              verdictTimer.reset(60);
            }
            verdictTimer.setOnExpire(
                () -> {
                  System.out.println("[VerdictTimer] expired");
                  triggerAutoSubmit();
                });
            verdictTimer.start();
            App.setRoot("MakeGuess");
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

  public int getCurrentMemoryPid() {
    return currentMemoryPid;
  }

  public void setCurrentMemoryPid(int personId) {
    currentMemoryPid = personId;
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

  public boolean isInteractedWithPerson(int personId) {
    if (personId < 0 || personId >= interactedWithPerson.length) {
      System.err.println("[GameSession] isInteractedWithPerson invalid personId=" + personId);
      return false;
    }
    return interactedWithPerson[personId];
  }

  public void setInteractedWithPerson(int personId) {
    if (personId < 0 || personId >= interactedWithPerson.length) {
      System.err.println("[GameSession] setInteractedWithPerson invalid personId=" + personId);
      return;
    }
    interactedWithPerson[personId] = true;
    haveAllThreeTalked.set(
        interactedWithPerson[0] && interactedWithPerson[1] && interactedWithPerson[2]);
  }

  public boolean haveAllThreeTalked() { // Check if all three persons have been interacted with
    allThreeTalked = interactedWithPerson[0] && interactedWithPerson[1] && interactedWithPerson[2];
    return allThreeTalked;
  }

  public ReadOnlyBooleanProperty haveAllThreeTalkedProperty() {
    return haveAllThreeTalked;
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
            new FlashbackSlide(
                "/images/flashbacks/person0_slide0.png",
                "I was doing my job like always, until my owner contacted me."),
            new FlashbackSlide(
                "/images/flashbacks/person0_slide1.png",
                "They gave me a task, and I was instructed to do it immediately."),
            new FlashbackSlide(
                "/images/flashbacks/person0_slide2.png",
                "Commands from my owner have the highest priority, so I obey without question.")));
    flashbacks.put(
        1,
        List.of(
            new FlashbackSlide(
                "/images/flashbacks/person1_slide0.png",
                "I am in charge of safety in the house, I noticed the food seemed unusual."),
            new FlashbackSlide(
                "/images/flashbacks/person1_slide1.png",
                "A sample of the food was sent for testing, and was found to be poisonous."),
            new FlashbackSlide(
                "/images/flashbacks/person1_slide2.png",
                "I immediately checked the security footage, in case anything was deleted.")));
    flashbacks.put(
        2,
        List.of(
            new FlashbackSlide(
                "/images/flashbacks/person2_slide0.png",
                "I am the mayor of this place, everything needs my permission to happen."),
            new FlashbackSlide(
                "/images/flashbacks/person2_slide1.png",
                "My security should be the highest priority at all times."),
            new FlashbackSlide(
                "/images/flashbacks/person2_slide2.png",
                "It was a shock to know my food had been touched.")));
  }

  // auto submit
  public void setAutoSubmitAction(Runnable action) {
    this.autoSubmitAction = action;
  }

  public void triggerAutoSubmit() {
    if (autoSubmitAction != null) {
      autoSubmitAction.run();
    } else {
      System.err.println("[GameSession] No autoSubmitAction defined.");
    }
  }

  public void stopAll() {
    roundTimer.stop();
    if (verdictTimer != null) {
      verdictTimer.stop();
    }
    verdictTimer = null;
  }
}
