package apollo.gui.genomemap;

import apollo.gui.Tier;

public class SiteTier extends Tier {

  // default starting height for start/stop codons
  private final static int siteHeight = 6;

  public SiteTier() {
    setDrawSpace(siteHeight);
  }

  public String getTierLabel() {
    return null;
  }

  public boolean isLabeled() {
    return false;
  }
}
