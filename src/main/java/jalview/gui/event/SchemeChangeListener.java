package jalview.gui.event;

import java.util.EventListener;

public interface SchemeChangeListener extends EventListener {

  public boolean handleSchemeChangeEvent(SchemeChangeEvent evt);

}
