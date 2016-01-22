package apollo.gui.event;

import java.util.EventListener;

public interface ViewListener extends EventListener {

  public boolean handleViewEvent(ViewEvent evt);

}
