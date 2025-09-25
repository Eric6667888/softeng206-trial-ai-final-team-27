package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameSession;
import nz.ac.auckland.se206.GameStateContext;

public class GuessController implements Initializable {
  @FXML private Label lblTimer;
  @FXML private ChoiceBox<String> choiceBox;
  @FXML private TextArea textField;
  private String decision;

  private String[] options = {"Guilty", "Not Guilty"};

  private GameSession session = GameStateContext.getSession();

  @FXML
  public void initialize(URL arg0, ResourceBundle arg1) {
    GameSession session = GameStateContext.getSession();

    choiceBox.getItems().addAll(options);
    choiceBox.setOnAction(this::getOptions);

    if (session.getVerdictTimer() == null) {
      session.startVerdictWindow(
          () ->
              Platform.runLater(
                  () -> {
                    try {
                      App.setRoot("NotGuilty");
                    } catch (IOException e) {
                      e.printStackTrace();
                    }
                  }));
    }

    lblTimer.textProperty().unbind();
    lblTimer
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () -> format(session.getVerdictTimer().getSecondsLeft()),
                session.getVerdictTimer().secondsLeftProperty()));
  }

  public void getOptions(ActionEvent event) {
    this.decision = choiceBox.getValue();
  }

  public void submit(ActionEvent event) {
    String text = textField.getText();
  }

  @FXML
  private void onYesClicking() throws IOException {
    session.getRoundTimer().stop();
    App.setRoot("Guilty");
  }

  @FXML
  private void onNoClicking() throws IOException {
    session.getRoundTimer().stop();
    App.setRoot("NotGuilty");
  }

  private String format(int totalSeconds) {
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }
}
