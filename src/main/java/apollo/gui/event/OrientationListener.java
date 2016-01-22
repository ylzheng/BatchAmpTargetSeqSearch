package apollo.gui.event;

import java.util.EventListener;

public interface OrientationListener extends EventListener {

  public boolean handleOrientationEvent(OrientationEvent evt);

}
