package apollo.analysis;

import apollo.datamodel.template.PrimerBlastResult;
import junit.framework.TestCase;

public class NCBIPrimerBlastTest extends TestCase {
  String outputRetriedInforFileName = "/Users/yzheng/Documents/CDCProject/IHRC_CDC/testretriedInfor";
  public void testRunAnalysis() throws Exception
  {
    // 1. create a primer-blast data model for collecting the results
    PrimerBlastResult result = new PrimerBlastResult();

    // 2. build primer-blast analysis options
    RemotePrimerBlastNCBI.PrimerBlastOptions opts = new RemotePrimerBlastNCBI.PrimerBlastOptions();
    opts.setPrimerSpecificityDatabase(RemotePrimerBlastNCBI.PrimerBlastOptions.Database.nt);

    // 3. load left and right primers
    opts.setPrimerLeftInput("CATTTGTTCAGCTGTCCCAGTC");
    opts.setPrimerRightInput("CTCACCCTTCCCATGAATTGA");

    // 4. submit request to NCBI Primer-Blast web service
    RemotePrimerBlastNCBI primer = new RemotePrimerBlastNCBI(opts);
    primer.runAnalysis(result, outputRetriedInforFileName);

    // 5. retrieve result and do downstream analysis
    System.out.println("Forward Template Start: " + result.getfTemplateStart() + "\t" + "Stop:" + result.getfTemplateStop());
    System.out.println("Reverse Template Start: " + result.getrTemplateStart() + "\t" + "Stop:" + result.getrTemplateStop());
  }


}
