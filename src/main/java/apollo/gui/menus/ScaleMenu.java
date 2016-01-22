package apollo.gui.menus;

import apollo.gui.genomemap.ScaleView;

import java.awt.*;
import javax.swing.*;

public class ScaleMenu extends JPopupMenu {

  ScaleView view;
  Point pos;

  JMenuItem colouring;
  JMenuItem guideline;


  public ScaleMenu(ScaleView view,Point pos) {
    super("Scale operations");
    this.view = view;
    this.pos  = pos;

    menuInit();
  }

  public void menuInit() {

    colouring = add(view.getColouringAction());
    //guideline = add(view.getGuideLineAction());
  }
}
