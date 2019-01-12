//unique package
package obd.edu.pdx.mohak.ece558.object_detection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

/** Graphic instance for rendering image labels. */
public class LabelGraphic extends GraphicOverlay.Graphic {

  //variable
  private final Paint textPaint;
  private final GraphicOverlay overlay;
  private List<String> labels;

  /**
   * overlay the lable on the camera screen
   * @param overlay
   * @param labels
   */

  LabelGraphic(GraphicOverlay overlay, List<String> labels) {
    super(overlay);
    this.overlay = overlay;
    this.labels = labels;
    textPaint = new Paint();
    textPaint.setColor(Color.WHITE);
    textPaint.setTextSize(60.0f);
  }

  /**
   * draw the lable according to height and weight
   * @param canvas drawing canvas
   */
  @Override
  public synchronized void draw(Canvas canvas) {
    float x = overlay.getWidth() / 4.0f;
    float y = overlay.getHeight() / 4.0f;

    for (String label : labels) {
      canvas.drawText(label, x, y, textPaint);
      y = y - 62.0f;
    }
  }
}
