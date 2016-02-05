package apollo.analysis;

import apollo.datamodel.template.PrimerBlastResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by yzheng on 12/21/15.
 */
public class BatchPrimerPairSubmit {
    ArrayList<Primer> primersInfors_Result = new ArrayList<Primer>();
    public BatchPrimerPairSubmit(String cvsFileName, String path) throws IOException {
        LoadPrimerCSV ob = new LoadPrimerCSV(cvsFileName);
        ob.addTransformedSeq2FRPrimer(ob.primersInfors);/**transform the mixedbase to the base*/
        primersInfors_Result = ob.primersInfors;
        String[] outputRetriedInforFile = new String[ob.primersInfors.size()];

        for (int i = 0; i < ob.primersInfors.size(); i++) {
            outputRetriedInforFile[i] = path + "/" + ob.primersInfors.get(i).primerName_F.trim();
            System.out.println("\n~~~~~~~~i is~~~~~~~~~~ " + i);
            System.out.println("\n" + ob.primersInfors.get(i).primerName_F + "\n" +
                    ob.primersInfors.get(i).originalSequence_F + "\n" +
                    ob.primersInfors.get(i).originalSequence_R+ "\n");
            System.out.println("The following has no mixed base \n" + ob.primersInfors.get(i).primerName_F + "\n" +
                    ob.primersInfors.get(i).originalSequence_F_noMixedBase + "\n" +
                    ob.primersInfors.get(i).originalSequence_R_noMixedBase+ "\n");
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
            // 1. create a primer-blast data model for collecting the results
            PrimerBlastResult result = new PrimerBlastResult();

            // 2. build primer-blast analysis options
            RemotePrimerBlastNCBI.PrimerBlastOptions opts = new RemotePrimerBlastNCBI.PrimerBlastOptions();
            opts.setPrimerSpecificityDatabase(RemotePrimerBlastNCBI.PrimerBlastOptions.Database.nt);

            // 3. load left and right primers
            opts.setPrimerLeftInput(ob.primersInfors.get(i).originalSequence_F_noMixedBase);
            opts.setPrimerRightInput(ob.primersInfors.get(i).originalSequence_R_noMixedBase);

            // 4. submit request to NCBI Primer-Blast web service
            RemotePrimerBlastNCBI primer = new RemotePrimerBlastNCBI(opts);
            try {
                primer.runAnalysis(result, outputRetriedInforFile[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 5. retrieve result and do downstream analysis
            int[] SEPos_dif =  findMinMaxPosValue(result.getfTemplateStart(),
                    result.getfTemplateStop(), result.getrTemplateStart(), result.getrTemplateStop());
            primersInfors_Result.get(i).genomeID = result.getGenomeID();
            primersInfors_Result.get(i).expectedAmpliconSize = SEPos_dif[2];
            primersInfors_Result.get(i).startPosinGenome = SEPos_dif[0];
            primersInfors_Result.get(i).endPosinGenome = SEPos_dif[1];

            System.out.println("Forward Template Start: " + result.getfTemplateStart() + "\t" +
                    "Stop:" + result.getfTemplateStop());
            System.out.println("Reverse Template Start: " + result.getrTemplateStart() + "\t" +
                    "Stop:" + result.getrTemplateStop());
            System.out.println("the genome ID is: " + result.getGenomeID());
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } /** end of for*/
    }

    public static int[] findMinMaxPosValue(int Fstart, int Fstop, int Rstart, int Rstop){
        int[] a = new int[4];
        int[] b = new int[3]; /** return the start, stop and the their difference +1 values */
        a[0] = Fstart; a[1] = Fstop; a[2] = Rstart; a[3] = Rstop;
        int tmpmin = 0;
        for(int i = 0; i < a.length; i++){
            for(int j = i+1; j < a.length; j++){
                if(a[i] < a[j]){
                    tmpmin = a[i];
                    a[i] = a[j];
                    a[j] = tmpmin;
                }
            }
        }
        b[0] = a[3]; //a[3] is the smallest value, a[0] is the largest value;
        b[1] = a[0];
        b[2] = (a[0] - a[3] + 1);
        return b;
    }

    public void printOutResult(ArrayList<Primer> primerInforResult, String outputFile) throws IOException{
        FileWriter outputStream = null;
        try{
            outputStream = new FileWriter(new File(outputFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert outputStream != null;
        for (Primer eachElement : primerInforResult){
            if (eachElement.expectedAmpliconSize > 2 ){
                outputStream.write(
                                eachElement.primerName_F + ","
                                + eachElement.sequencewithIllumina5Tag_F + ","
                                + eachElement.originalSequence_F + ","
                                + eachElement.organismName + ","
                                + eachElement.originalSequence_F_noMixedBase + ","
                                + eachElement.expectedAmpliconSize + ","
                                //+ (eachElement.plusIllumina + eachElement.expectedAmpliconSize) + ","
                                //+ (eachElement.plusIlluminaNextera + eachElement.expectedAmpliconSize) + ","
                                + eachElement.startPosinGenome + ","
                                + eachElement.endPosinGenome + ","
                                + eachElement.genomeID + "\n"
                                + eachElement.primerName_R + ","
                                + eachElement.sequencewithIllumina5Tag_R + ","
                                + eachElement.originalSequence_R + ","
                                + eachElement.organismName + ","
                                + eachElement.originalSequence_R_noMixedBase + "\n");
            } else{
                outputStream.write(
                        eachElement.primerName_F + ","
                                + eachElement.sequencewithIllumina5Tag_F + ","
                                + eachElement.originalSequence_F + ","
                                + eachElement.organismName + ","
                                + eachElement.originalSequence_F_noMixedBase + ","
                                + 0 + "\n"
                                //+ 0 + ","
                                //+ 0 + ","

                                + eachElement.primerName_R + ","
                                + eachElement.sequencewithIllumina5Tag_R + ","
                                + eachElement.originalSequence_R + ","
                                + eachElement.organismName + ","
                                + eachElement.originalSequence_R_noMixedBase + "\n");
            }

        }
        outputStream.close();
    }

    //public void printOut

    public static void main(String[] args) throws Exception {
        BatchPrimerPairSubmit ob1 = new BatchPrimerPairSubmit(args[0], args[1]);
        ob1.printOutResult(ob1.primersInfors_Result, args[2]);
        //int a = ob1.findMinMaxPosValue(6624, 6644, 6727, 6711);
        //System.out.println("min and max is" + String.valueOf(a));
    }

}
