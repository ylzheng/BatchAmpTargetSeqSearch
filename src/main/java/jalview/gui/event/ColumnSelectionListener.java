package jalview.gui.event;

import java.util.EventListener;

public interface ColumnSelectionListener extends EventListener {

  public boolean handleColumnSelectionEvent(ColumnSelectionEvent evt);

}
