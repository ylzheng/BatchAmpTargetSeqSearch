package jalview.gui.event;

import java.util.EventListener;

public interface EditListener extends EventListener {

  public boolean handleEditEvent(EditEvent evt);

}
