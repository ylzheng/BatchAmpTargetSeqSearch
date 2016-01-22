package apollo.gui.event;

import java.util.EventListener;

public interface NamedFeatureSelectionListener extends EventListener {

    public boolean handleNamedFeatureSelectionEvent(NamedFeatureSelectionEvent evt);

}//end NamedFeatureSelectionListener


