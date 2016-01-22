package apollo.gui.genomemap;

import apollo.gui.BaseScrollable;

public class GuideLine implements BaseScrollable {
  ScaleView sv;
  int base;

  public GuideLine(ScaleView sv) {
    this.sv = sv;
    base = (int)sv.getCentre();
  }

  public void scrollToBase(int pos) {
    base = pos;
  }

  public int getVisibleBase() {
    if (base < (int)sv.getVisibleRange()[0]) {
      base = (int)sv.getVisibleRange()[0];
    } else if (base > (int)sv.getVisibleRange()[1]) {
      base = (int)sv.getVisibleRange()[1]-2;
    }
    // System.out.println("Current base = " + base);
    return base;
  }

  public int getVisibleBaseCount() {
    return 0;
  }
}
