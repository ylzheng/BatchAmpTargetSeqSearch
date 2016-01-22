package apollo.gui.drawable;

import java.awt.Rectangle;
import java.awt.Graphics;

import apollo.datamodel.*;
import apollo.gui.TierManagerI;
import apollo.gui.Transformer;

/**
 * A drawable for drawing ensembl style (box around set) feature sets.
 */
public class DrawableEnsemblFeatureSet extends DrawableFeatureSet
  implements DrawableSetI {

  // Optimisation use static array for edges so we don't have to keep
  // allocating it
  static int maxEdge = 100;
  static int edges[] = new int[maxEdge];

  public DrawableEnsemblFeatureSet() {
    super(true);
  }

  public DrawableEnsemblFeatureSet(FeatureSetI fset) {
    super(fset, true);
  }

  public Drawable createDrawable (SeqFeatureI sf) {
    return new DrawableSeqFeature(sf, true);
  }

  public void drawUnselected(Graphics g,
                             Rectangle boxBounds,
                             Transformer transformer,
                             TierManagerI manager) {
    int setSize = size();

    if (setSize > 1) {
      g.setColor(getDrawableColor());

      // change to getBoxBounds?
      DrawableUtil.setBoxBounds(this, transformer, manager);

      g.drawRect(boxBounds.x, boxBounds.y, boxBounds.width, boxBounds.height);
    }
  }
}
