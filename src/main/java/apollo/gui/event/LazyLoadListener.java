package apollo.gui.event;

import java.util.EventListener;

public interface LazyLoadListener extends EventListener {

  public boolean handleLazyLoadEvent(LazyLoadEvent evt);
}
