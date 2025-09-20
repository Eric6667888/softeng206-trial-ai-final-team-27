package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.Timer;

public class GuessController {
  @FXML private Label lblTimer;

  @FXML
  public void initialize() throws ApiProxyException {
    Timer timer = Timer.getInstance();
    lblTimer.setText(timer.getLabel().getText());
    lblTimer.textProperty().bind(timer.getLabel().textProperty());
  }

  @FXML
  private void onYesClicking() throws IOException {
    Timer timer = Timer.getInstance();
    timer.stop();
    App.setRoot("Guilty");
  }

  @FXML
  private void onNoClicking() throws IOException {
    App.setRoot("NotGuilty");
  }
}
