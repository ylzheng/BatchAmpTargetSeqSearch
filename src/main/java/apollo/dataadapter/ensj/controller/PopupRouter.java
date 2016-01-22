package apollo.dataadapter.ensj.controller;
import javax.swing.event.*;

public class PopupRouter extends EventRouter implements PopupMenuListener
{
  public PopupRouter(Controller handler, String key){
    super(handler, key);
  }

  public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
    getHandler().handleEventForKey(getKey());
  }
  
  public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
  }
  
  public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
  }
  
}
