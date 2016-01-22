package apollo.dataadapter;

import apollo.seq.io.*;
import apollo.datamodel.*;
import apollo.config.Config;

import org.bdgp.util.*;

public class GFFAdapter extends BaseGFFAdapter {


  // extID is ignored here, all data from file is returned
  public StrandedFeatureSetI getAnalysisRegion()  throws org.bdgp.io.DataAdapterException {
    GFFFile gff  = getGFFFile();

    fireProgressEvent(new ProgressEvent(this, new Double(50.0),
                                        "Populating data structures..."));

    StrandedFeatureSet fset = new StrandedFeatureSet(new FeatureSet(), new FeatureSet());
    FeatureSetBuilder  fsb  = new FeatureSetBuilder();

    fsb.makeSetFeatures (fset, gff.seqs, Config.getPropertyScheme());

    fireProgressEvent(new ProgressEvent(this, new Double(100.0),
                                        "Done..."));
    return fset;
  }
}
