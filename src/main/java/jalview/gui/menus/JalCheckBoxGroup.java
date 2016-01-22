package jalview.gui.menus;

import java.awt.event.*;

public class JalCheckBoxGroup implements ItemListener {
  JalToggleGroup group = new JalToggleGroup();

  public void add(JalCheckBoxMenuItem item) {
    group.add(item);
//    item.addItemListener(this);
  }

  public void setNoSelection() {
    group.setNoSelection();
  }

  public void itemStateChanged(ItemEvent evt) {
//    group.stateChanged((JalCheckBoxMenuItem)evt.getSource());
  }
}
