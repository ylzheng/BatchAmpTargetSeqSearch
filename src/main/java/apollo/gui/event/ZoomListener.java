package apollo.gui.event;

import java.util.EventListener;

public interface ZoomListener extends EventListener {

  public boolean handleZoomEvent(ZoomEvent evt);

}
