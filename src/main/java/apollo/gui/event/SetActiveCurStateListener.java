package apollo.gui.event;

import java.util.EventListener;

public interface SetActiveCurStateListener extends EventListener {

  public boolean handleSetActiveCurStateEvent(SetActiveCurStateEvent evt);
}
