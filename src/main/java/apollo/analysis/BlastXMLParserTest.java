package apollo.analysis;

import java.io.File;

import apollo.datamodel.StrandedFeatureSet;

import junit.framework.TestCase;

public class BlastXMLParserTest extends TestCase {

  public void testParse() throws Exception
  {
    File f = new File("/Users/elee/blah/blast_3.xml");
    BlastXMLParser parser = new BlastXMLParser();
    StrandedFeatureSet results = new StrandedFeatureSet();
    parser.parse(f.toURL().toString(), results);
  }
  
}
