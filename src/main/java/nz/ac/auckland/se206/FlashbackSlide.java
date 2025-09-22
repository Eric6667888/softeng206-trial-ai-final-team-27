package nz.ac.auckland.se206;

public class FlashbackSlide {
  private final String imagePath;
  private final String caption;

  public FlashbackSlide(String imagePath, String caption) {
    this.imagePath = imagePath;
    this.caption = caption;
  }

  public String getImagePath() {
    return imagePath;
  }

  public String getCaption() {
    return caption;
  }
}
