package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BrainWashBottleController {

  @FXML private ImageView imgUnscrewCap;

  @FXML private ImageView imgTurnBottle;

  @FXML private ImageView imgFlipBottle;

  @FXML private ImageView imgBrainWashBottle;

  @FXML private Button btnBack;

  @FXML
  private void initialize() {
    // Initialize images for the brain wash bottle view
    imgBrainWashBottle.setImage(new Image("/images/Evidence1.png"));
    imgUnscrewCap.setImage(new Image("/images/UnscrollArrow.png"));
    imgTurnBottle.setImage(new Image("/images/FlipArrow.png"));
    imgFlipBottle.setImage(new Image("/images/LiftBottleArrow.png"));
    btnBack.setVisible(false);
    btnBack.setDisable(true);
  }

  @FXML
  private void handleUnscrewCap() {
    // Change to bird's eye view image of unscrewing cap
    imgBrainWashBottle.setImage(new Image("/images/BirdsEyeView.png"));
    imgUnscrewCap.setDisable(true);
    imgTurnBottle.setDisable(true);
    imgFlipBottle.setDisable(true);
    imgUnscrewCap.setVisible(false);
    imgTurnBottle.setVisible(false);
    imgFlipBottle.setVisible(false);
    btnBack.setVisible(true);
    btnBack.setDisable(false);
  }

  @FXML
  private void handleTurnBottle() {
    // Change to side view image
    imgBrainWashBottle.setImage(new Image("/images/SideView.png"));
    imgUnscrewCap.setDisable(true);
    imgTurnBottle.setDisable(true);
    imgFlipBottle.setDisable(true);
    imgUnscrewCap.setVisible(false);
    imgTurnBottle.setVisible(false);
    imgFlipBottle.setVisible(false);
    btnBack.setVisible(true);
    btnBack.setDisable(false);
  }

  @FXML
  private void handleFlipBottle() {
    // Change to bottom view image
    imgBrainWashBottle.setImage(new Image("/images/BottomView.png"));
    imgUnscrewCap.setDisable(true);
    imgTurnBottle.setDisable(true);
    imgFlipBottle.setDisable(true);
    imgUnscrewCap.setVisible(false);
    imgTurnBottle.setVisible(false);
    imgFlipBottle.setVisible(false);
    btnBack.setVisible(true);
    btnBack.setDisable(false);
  }

  @FXML
  private void onBack() {
    // Back to original view
    imgBrainWashBottle.setImage(new Image("/images/Evidence1.png"));
    imgUnscrewCap.setDisable(false);
    imgTurnBottle.setDisable(false);
    imgFlipBottle.setDisable(false);
    imgUnscrewCap.setVisible(true);
    imgTurnBottle.setVisible(true);
    imgFlipBottle.setVisible(true);
    btnBack.setVisible(false);
    btnBack.setDisable(true);
  }
}
