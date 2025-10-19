package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BrainWashBottleController {

  @FXML private ImageView imgUnscrewCap;

  @FXML private ImageView imgTurnBottle;

  @FXML private ImageView imgFlipBottle;

  @FXML private ImageView imgBrainWashBottle;

  @FXML
  private void initialize() {
    // Initialize images for the brain wash bottle view
    imgBrainWashBottle.setImage(new Image("/images/Evidence1.png"));
    imgUnscrewCap.setImage(new Image("/images/UnscrollArrow.png"));
    imgTurnBottle.setImage(new Image("/images/FlipArrow.png"));
    imgFlipBottle.setImage(new Image("/images/LiftBottleArrow.png"));
  }

  @FXML
  private void handleUnscrewCap() {
    imgBrainWashBottle.setImage(new Image("/images/BirdsEyeView.png"));
  }

  @FXML
  private void handleTurnBottle() {
    imgBrainWashBottle.setImage(new Image("/images/SideView.png"));
  }

  @FXML
  private void handleFlipBottle() {
    imgBrainWashBottle.setImage(new Image("/images/BottomView.png"));
  }
}
