package apollo.gui.event;

import java.util.EventListener;

public interface BaseFocusListener extends EventListener {

  public boolean handleBaseFocusEvent(BaseFocusEvent evt);

}
