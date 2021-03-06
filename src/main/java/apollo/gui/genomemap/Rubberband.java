// Adapted from code in "Graphic JAVA volume 1".
package apollo.gui.genomemap;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import apollo.gui.event.RubberbandEvent;
import apollo.gui.event.MouseButtonEvent;
import apollo.gui.event.RubberbandListener;

/**
 * Draws and controls the rubberband used for selections.
 */
abstract public class Rubberband implements MouseListener, MouseMotionListener {
  protected Point anchorPt    = new Point(0,0);
  protected Point stretchedPt = new Point(0,0);
  protected Point lastPt      = new Point(0,0);
  protected Point endPt       = new Point(0,0);

  private Component component;
  private boolean   firstStretch = true;
  private boolean   active = false;
  private int       modifiers;

  Vector  listeners;

  abstract public void drawLast(Graphics g);
  abstract public void drawNext(Graphics g);

  public Rubberband() {}

  public Rubberband(Component c) {
    listeners = new Vector();
    setComponent(c);
  }

  public void setActive(boolean b) {
    active = b;
  }

  public boolean isRbButton(MouseEvent e) {
    if (MouseButtonEvent.isMiddleMouseClick(e)) {
      return true;
    }
    return false;
  }

  public Component getComponent() {
    return this.component;
  }

  public void setComponent(Component c) {
    component = c;

    component.addMouseListener(this);
    component.addMouseMotionListener(this);
  }

  public void    setModifiers(int modifiers) {
    this.modifiers = modifiers;
  }
  public void mousePressed(MouseEvent event) {
    if(isActive() && isRbButton(event)) {
      anchor(event.getPoint());
    }
  }
  // Is this needed?  Commenting it out doesn't appear to make any difference.
  public void mouseClicked(MouseEvent event) {
//     if(isActive() && isRbButton(event)) {
//       end(event.getPoint());
//     }
  }
  public void mouseReleased(MouseEvent event) {
    //    System.out.println("Rubberband.mouseReleased: " + event); // DEL
    if(isActive() && isRbButton(event)) {
      setModifiers(event.getModifiers());
      end(event.getPoint());
    }
  }
  public void mouseDragged(MouseEvent event) {
    //    System.out.println("Rubberband.mouseDragged: " + event); // DEL
    if(isActive() && isRbButton(event)) {
      stretch(event.getPoint());
    }
  }
  public void mouseEntered(MouseEvent event) {}
  public void mouseExited(MouseEvent event) {}
  public void mouseMoved(MouseEvent event) {}

  public boolean isActive    () {
    return active;
  }
  public Point   getAnchor   () {
    return anchorPt;
  }
  public int     getModifiers() {
    return modifiers;
  }
  public Point   getStretched() {
    return stretchedPt;
  }
  public Point   getLast     () {
    return lastPt;
  }
  public Point   getEnd      () {
    return endPt;
  }

  public void anchor(Point p) {
    firstStretch = true;
    anchorPt.x = p.x;
    anchorPt.y = p.y;

    stretchedPt.x = lastPt.x = anchorPt.x;
    stretchedPt.y = lastPt.y = anchorPt.y;
  }
  public void stretch(Point p) {
    lastPt.x      = stretchedPt.x;
    lastPt.y      = stretchedPt.y;
    stretchedPt.x = p.x;
    stretchedPt.y = p.y;
    //      RepaintManager rm = RepaintManager.currentManager(component);
    //      rm.setDoubleBufferingEnabled(false);

    Graphics g = component.getGraphics();
    if(g != null) {
      try {
        g.setXORMode(component.getBackground());

        if(firstStretch == true)
          firstStretch = false;
        else
          drawLast(g);

        drawNext(g);
      }
      finally {
        g.dispose();
      }
    }
  }
  public void end(Point p) {
    lastPt.x = endPt.x = p.x;
    lastPt.y = endPt.y = p.y;

    Graphics g = component.getGraphics();
    if(g != null) {
      try {
        g.setXORMode(component.getBackground());
        drawLast(g);
      }
      finally {
        g.dispose();
        if (!firstStretch) {
          fireRubberbandEvent(new RubberbandEvent(this,getBounds()));
        }
      }
    }
  }
  public Rectangle getBounds() {
    return new Rectangle(stretchedPt.x < anchorPt.x ?
                         stretchedPt.x : anchorPt.x,
                         stretchedPt.y < anchorPt.y ?
                         stretchedPt.y : anchorPt.y,
                         Math.abs(stretchedPt.x - anchorPt.x),
                         Math.abs(stretchedPt.y - anchorPt.y));
  }

  public Rectangle lastBounds() {
    return new Rectangle(lastPt.x < anchorPt.x ? lastPt.x : anchorPt.x,
                         lastPt.y < anchorPt.y ? lastPt.y : anchorPt.y,
                         Math.abs(lastPt.x - anchorPt.x),
                         Math.abs(lastPt.y - anchorPt.y));
  }
  public void addListener(RubberbandListener l) {
    listeners.addElement(l);
  }
  public void removeListener(RubberbandListener l) {
    if (listeners.contains(l)) {
      listeners.removeElement(l);
    }
  }
  public void fireRubberbandEvent(RubberbandEvent evt) {
    for (int i=0;i<listeners.size();i++) {
      RubberbandListener l = (RubberbandListener)listeners.elementAt(i);
      l.handleRubberbandEvent(evt);
    }
  }
}
