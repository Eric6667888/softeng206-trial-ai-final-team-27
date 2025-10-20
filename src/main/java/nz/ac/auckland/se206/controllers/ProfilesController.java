package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

// Controller class for the AI Profiles view
public class ProfilesController {

  @FXML private Rectangle rectUnknown;

  @FXML
  private void initialize() {
    // Initialization is not required for this controller as of now
  }

  // Method to handle click event on the unknown profile rectangle
  @FXML
  private void onUnknownClicked() {
    Stage imageStage = new Stage();
    imageStage.setTitle("Evidence");
    imageStage.initModality(Modality.APPLICATION_MODAL);
    Image image = new Image(getClass().getResourceAsStream("/images/evidence3.png"));
    ImageView imageView = new ImageView(image);
    imageView.setFitWidth(400);
    imageView.setFitHeight(300);
    imageView.setPreserveRatio(true);

    // Create a layout to hold the image
    VBox layout = new VBox();
    layout.getChildren().add(imageView);

    Scene scene = new Scene(layout);
    imageStage.setScene(scene);
    imageStage.show();
  }
}
