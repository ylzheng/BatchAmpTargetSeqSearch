package apollo.gui.event;

import java.util.EventListener;

public interface FeatureSelectionListener extends EventListener {

  public boolean handleFeatureSelectionEvent(FeatureSelectionEvent evt);

}


