package apollo.gui.detailviewers.sequencealigner.DNAPanel;

import apollo.datamodel.SeqFeatureI;
import apollo.gui.detailviewers.sequencealigner.AbstractTierPanel;
import apollo.gui.detailviewers.sequencealigner.FeaturePlaceFinder;
import apollo.gui.detailviewers.sequencealigner.MultiTierPanel;
import apollo.gui.detailviewers.sequencealigner.Orientation;
import apollo.gui.detailviewers.sequencealigner.PanelFactory;
import apollo.gui.detailviewers.sequencealigner.ReadingFrame;
import apollo.gui.detailviewers.sequencealigner.Strand;

public class HomogeneousPanel extends MultiTierPanel {
  
  private PanelFactory panelFactory;

  public HomogeneousPanel(PanelFactory panelFactory) {
    super(FeaturePlaceFinder.Type.COMPACT);
    this.panelFactory = panelFactory;
    addPanel(panelFactory.makePanel());
  }
  
  public HomogeneousPanel(PanelFactory panelFactory, 
      FeaturePlaceFinder.Type finder) {
    super(finder);
    this.panelFactory = panelFactory;
    addPanel(panelFactory.makePanel());
  }
  
  public boolean canAddFeature(SeqFeatureI feature) {
    return true;
  }

  public AbstractTierPanel addFeature(SeqFeatureI feature) {
    if (!super.canAddFeature(feature)) {
      addPanel(panelFactory.makePanel());
    }
    
    return super.addFeature(feature);
  }
  
  public void switchStrand() {
    Strand strand = getStrand().getOpposite();
    panelFactory.setStrand(strand);
    super.switchStrand();
  }
  
  public void setStrand(Strand s) {
    panelFactory.setStrand(s);
    super.setStrand(s);
  }
  
  public void setReadingFrame(ReadingFrame rf) {
    panelFactory.setReadingFrame(rf);
    super.setReadingFrame(rf);
  }
  
  public void switchOrientation() {
    Orientation o = getOrientation().getOpposite();
    panelFactory.setOrientation(o);
    super.setOrientation(o);
  }
  
  public void setOrientation(Orientation o){
    panelFactory.setOrientation(o);
    super.setOrientation(o);
  }
  
}
