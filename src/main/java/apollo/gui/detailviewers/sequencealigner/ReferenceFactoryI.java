package apollo.gui.detailviewers.sequencealigner;

import apollo.datamodel.SequenceI;

public interface ReferenceFactoryI {

  public abstract SequenceI getReference(Strand s);
  
  public abstract SequenceI getReference(Strand s, ReadingFrame rf);
  
}
