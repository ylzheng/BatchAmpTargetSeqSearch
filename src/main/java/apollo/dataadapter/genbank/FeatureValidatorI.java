package apollo.dataadapter.genbank;

import apollo.datamodel.*;

/**
 * FeatureValidator interface
 *
 * Used to perform an integrity check
 * whatever datasource
 */
public interface FeatureValidatorI {
  public String validateFeature (FeatureSetI feature, CurationSet curation,
                                 String prefix, String suffix);
}
