package jalview.gui.event;

import java.util.EventListener;

public interface SequenceSelectionListener extends EventListener {

  public boolean handleSequenceSelectionEvent(SequenceSelectionEvent evt);

}
