package apollo.gui.event;

import java.util.EventListener;

public interface TierManagerListener extends EventListener {

  public boolean handleTierManagerEvent(TierManagerEvent evt);

}
