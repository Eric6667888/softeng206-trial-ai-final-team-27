package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import nz.ac.auckland.se206.App;

public class DecisionController {
  @FXML
  public void onReturnClicked() throws IOException {
    App.setRoot("landing");
  }
}
