package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PopUpManager {

  private static final Map<String, Stage> popupStages =
      new HashMap<>(); // Map to store fxmlName to titles, for checking existing popups

  public static void showPopup(String fxmlName, String title) {
    try {
      // Check if a popup with the same fxmlName is already open
      Stage existingStage = popupStages.get(fxmlName);
      if (existingStage == null) {
        // If first time opening, create and store the new popup stage
        FXMLLoader fxmlLoader =
            new FXMLLoader(App.class.getResource("/fxml/" + fxmlName + ".fxml"));
        Parent root = fxmlLoader.load();
        final Stage newStage = new Stage();
        Scene scene = new Scene(root);

        // Load appropriate CSS based on the FXML name
        if ("AIProfiles".equals(fxmlName)) {
          scene.getStylesheets().add(App.class.getResource("/css/profiles.css").toExternalForm());
        } else if ("SecurityCamera".equals(fxmlName)) {
          scene.getStylesheets().add(App.class.getResource("/css/security.css").toExternalForm());
        } else if ("BrainWashBottle".equals(fxmlName)) {
          scene.getStylesheets().add(App.class.getResource("/css/evidence.css").toExternalForm());
        }

        newStage.setScene(scene);
        newStage.setTitle(title);
        newStage.initModality(Modality.APPLICATION_MODAL);

        newStage.setOnCloseRequest(
            event -> {
              event.consume(); // Prevent default close behavior
              newStage.hide(); // Hide the stage instead of closing
            });
        popupStages.put(fxmlName, newStage);
        existingStage = newStage;
      }

      existingStage.show();
      existingStage.toFront(); // Bring the popup to the front if it already exists

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
