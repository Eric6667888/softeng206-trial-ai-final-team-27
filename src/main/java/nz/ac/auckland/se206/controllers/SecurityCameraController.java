package nz.ac.auckland.se206.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

/**
 * Controller class for the security camera view. Handles user interactions related to security
 * camera functionalities during evidence collection.
 */
public class SecurityCameraController {
  @FXML private ImageView camera;
  @FXML private BorderPane backgBorderPane;
  @FXML private Slider cameraSlider;

  /** Initializes the security camera view. */
  @FXML
  private void initialize() {
    // Set initial values for the camera view and slider
    camera.setImage(new Image("/images/Security_NoAI.png"));
    cameraSlider.setValue(0);
    cameraSlider
        .valueProperty()
        .addListener(
            new ChangeListener<Number>() {
              @Override
              public void changed(
                  ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                // Checking if slider is between certain ranges to change the image accordingly
                if (newValue.doubleValue() >= 55 && newValue.doubleValue() <= 60) {
                  camera.setImage(new Image("/images/Security_WithAI.png"));
                } else {
                  camera.setImage(new Image("/images/Security_NoAI.png"));
                }
              }
            });
  }
}
