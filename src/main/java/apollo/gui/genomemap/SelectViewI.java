package apollo.gui.genomemap;

import apollo.gui.Selection;

public interface SelectViewI extends TierViewI {
  public void select(Selection modelSelection);

  /** SelectViewI vertically scrolls to the Selection.
   * Is this the appropriate place for this.
   * Its needed for ApolloPanel.handleFeatureSelectionEvent
   */
  public void verticalScrollToSelection();
}
