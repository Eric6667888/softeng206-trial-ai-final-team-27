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
    // Clear all variables for new game
    stopAll();
    haveAllThreeTalked.set(false);
    // Adjust checks on if user talked with characters
    currentFlashbackPid = -1;
    System.out.println("[GameSession] resetForNewGame");
    Arrays.fill(flashbackPlayed, false);
    Arrays.fill(interactedWithPerson, false);
    allThreeTalked = false;
    verdictStarted = false;
    // Function to reset everything to orignal state
    resetAndStartNewRound(totalSeconds);
  }

  public void startVerdictWindow(Runnable onVerdictExpire) {
    // When timer expire go to verdict window
    if (verdictStarted) {
      return;
    }
    verdictStarted = true;
    roundTimer.stop();
    // Set up verdict timer to start at 60s
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
    // Change timer to be one minute for verdict, and set actions for once timer expires and where
    // the make verdict scene is
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
            // When timer expire auto send whatever user has written and say timer has expired
            verdictTimer.setOnExpire(
                () -> {
                  System.out.println("[VerdictTimer] expired");
                  triggerAutoSubmit();
                });
            verdictTimer.start();
            // Change scene to make verdict scene and handle errors
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

  // Get images of flashbacks
  public void loadFlashbacksIfNeeded() {
    // Check if flashback already played, if so do nothing and go ot memory
    if (!flashbacks.isEmpty()) {
      return;
    }
    // Need new pictures and captions
    // Flashback images for AI defendant
    flashbacks.put(
        0,
        List.of(
            new FlashbackSlide(
                "/images/flashbacks/person0_slide0.png",
                "I was doing my job, cooking for the mayor like always. That is until my creator"
                    + " contacted me."),
            new FlashbackSlide(
                "/images/flashbacks/person0_slide1.png",
                "They gave me a task, and I was instructed to do it immediately."),
            new FlashbackSlide(
                "/images/flashbacks/person0_slide2.png",
                "Commands from my creator have the highest priority, as I am programmed to obey"
                    + " without question.")));
    // Flashback context for AI witness
    flashbacks.put(
        1,
        List.of(
            new FlashbackSlide(
                "/images/flashbacks/person1_slide0.png",
                "I am in charge of security at the mayor's house. One day the security system"
                    + " picked up an anomaly coming from the mayor's food."),
            new FlashbackSlide(
                "/images/flashbacks/person1_slide1.png",
                "A sample of the food was sent for testing, and was found to contain a chemical"
                    + " which brainwashed the consumer."),
            new FlashbackSlide(
                "/images/flashbacks/person1_slide2.png",
                "I immediately checked the security footage to find signs of the culprit.")));
    // Flashback context for Human Witness
    flashbacks.put(
        2,
        List.of(
            new FlashbackSlide(
                "/images/flashbacks/mayor_parade.png",
                "I am the mayor of this city making me the most powerful man with plenty of"
                    + " enemies."),
            new FlashbackSlide(
                "/images/flashbacks/person2_slide1.png",
                "As a result I am heavily guarded to ensure my safety."),
            new FlashbackSlide(
                "/images/flashbacks/person2_slide2.png",
                "To learn I was being brainwashed by the AI which cooked my food was shocking.")));
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
