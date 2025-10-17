package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
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
}
