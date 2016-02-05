package apollo.analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by yzheng on 12/21/15.
 */
public class LoadPrimerCSV {
    public ArrayList<Primer> primersInfors = new ArrayList<Primer>();
    String[] content;

    public LoadPrimerCSV(String primerCSVFileName) throws IOException {
        String content1 = new String(Files.readAllBytes(Paths.get(primerCSVFileName)));
        /** save row information into array*/
        if(content1.contains("\r")){
            content = content1.split("\r");
        }else{
            content = content1.split("\n");
        }
        /** seperate the row information into the FRPrimerInfor ArrayList */
        int contentLineCounter = 0;
        /** to find the start line of the primer content*/
        int i_index = 0;
        while(contentLineCounter < content.length){
            if(content[contentLineCounter].contains("Sequence with Illumina 5' Tags")){
                i_index = contentLineCounter + 1;
            }
            contentLineCounter ++;
        }
        for(int i = i_index; i < content.length; i = i+2){
            String[] rowFElement = new String[0];
            String[] rowRElement = new String[0];
            //System.out.println(" i is " + i);
            Primer tmpPrimer = null;
            //if(content[i].length() > 60 && i+1 < content.length && !content[i].contains("Sequence with Illumina 5' Tags")){
            if ( i+1 < content.length && !content[i].contains("Sequence with Illumina 5' Tags")){
                /**col infor: c0 primerName, c1 originalSeq, c2 fPrimer, c3 rPrimer, c4 organism name,c5 expected value */
                rowFElement = content[i].split(",");
                rowRElement = content[i+1].split(",");
                /** Three conditions according to the data, row[3] is empty, row[4] is the organism name */
                if(rowFElement.length > 5){
                    if(!rowFElement[4].equals("") && !rowFElement[5].equals("")){
                        tmpPrimer = new Primer(rowFElement[0], rowRElement[0], rowFElement[1], rowRElement[1],
                                rowFElement[2], rowRElement[2], rowFElement[4], Integer.parseInt(rowFElement[5]));
                        primersInfors.add(tmpPrimer);
                    }
                    if(!rowFElement[4].equals("") && rowFElement[5].equals("")){
                        tmpPrimer = new Primer(rowFElement[0], rowRElement[0], rowFElement[1], rowRElement[1],
                                rowFElement[2], rowRElement[2], rowFElement[4], Integer.parseInt("0"));
                        primersInfors.add(tmpPrimer);
                    }
                    if(rowFElement[4].equals("") && !rowFElement[5].equals("")){
                        tmpPrimer = new Primer(rowFElement[0], rowRElement[0], rowFElement[1], rowRElement[1],
                                rowFElement[2], rowRElement[2], "NoName", Integer.parseInt(rowFElement[5]));
                        primersInfors.add(tmpPrimer);
                    }
                    if(rowFElement[4].equals("") && rowFElement[5].equals("")){
                        tmpPrimer = new Primer(rowFElement[0], rowRElement[0], rowFElement[1], rowRElement[1],
                                rowFElement[2], rowRElement[2], "NoName", Integer.parseInt("0"));
                        primersInfors.add(tmpPrimer);
                    }
                }
                if (rowFElement.length == 5){
                    if(!rowFElement[4].equals("")){
                        tmpPrimer = new Primer(rowFElement[0], rowRElement[0], rowFElement[1], rowRElement[1],
                                rowFElement[2], rowRElement[2], rowFElement[4], Integer.parseInt("0"));
                        primersInfors.add(tmpPrimer);
                    }
                    if(rowFElement[4].equals("")){
                        tmpPrimer = new Primer(rowFElement[0], rowRElement[0], rowFElement[1], rowRElement[1],
                                rowFElement[2], rowRElement[2], "NoName", Integer.parseInt("0"));
                        primersInfors.add(tmpPrimer);
                    }
                }
                if(rowFElement.length < 5){
                    tmpPrimer = new Primer(rowFElement[0], rowRElement[0], rowFElement[1], rowRElement[1],
                            rowFElement[2], rowRElement[2], "NoName", Integer.parseInt("0"));
                    primersInfors.add(tmpPrimer);
                }
            }

        }
        System.out.println("finish");
    }

    public LoadPrimerCSV(String primerCSVFileName, String flag) throws IOException {
        /** this flag is used to label that the loaded file should be the result of batch searching from NCBI depends on
         * the fusion primers */
        if (flag.equals("TargetGenomeSeq")) {
            String content1 = new String(Files.readAllBytes(Paths.get(primerCSVFileName)));
            /** save row information into array*/
            if (content1.contains("\r")) {     /** \r is for mac, \n is for linux */
                content = content1.split("\r");
            } else {
                content = content1.split("\n");
            }
            String[] rowFElement = new String[0];
            String[] rowRElement = new String[0];
            /** seperate the row information into the FRPrimerInfor ArrayList */
            for (int i = 0; i < content.length; i = i + 2) {
                if( i + 1 < content.length){
                    if(content[i].contains("\t")){
                        rowFElement = content[i].split("\t");
                        rowRElement = content[i+1].split("\t");
                    }else{
                        rowFElement = content[i].split(",");
                        rowRElement = content[i+1].split(",");
                    }
                    Primer tmpPrimer = null;
                    /** 0: String primerNameF, 1: String primerNameR, 2: String sequencewithIllumina5TagF,
                     3: String sequencewithIllumina5TagR, 4: String originalSequenceF, 5: String originalSequenceR,
                     6: String organismNa, 7: int expectedAmpliconSiz, 8: int plusIllumina, 9: int plusIlluminaNextera,
                     10: String originalSequence_F_noMixedBase, 11: String originalSequence_R_noMixedBase,
                     12: String genomeID, 13: int startPosinGenome, 14: int endPosinGenome*/
                    tmpPrimer = new Primer(rowFElement[0], rowRElement[0], rowFElement[1], rowRElement[1],
                            rowFElement[2], rowRElement[2], rowFElement[4], Integer.parseInt(rowFElement[4]),
                            Integer.parseInt(rowFElement[5]), Integer.parseInt(rowFElement[6]), rowFElement[7],
                            rowRElement[4], rowFElement[10],
                            Integer.parseInt(rowFElement[8]), Integer.parseInt(rowFElement[9]));
                    primersInfors.add(tmpPrimer);
                }
            }
        }
    }
    public void addTransformedSeq2FRPrimer(ArrayList<Primer> primersInfor ){
        Tools.findMixedBasePrimerSeqPos(primersInfor);
        Tools.transformMixedBase(primersInfor);
        System.out.println("finish convert the mixedbase to corresponding base");
    }


    /** test */
    public static void main(String[] args) throws IOException{
        //LoadPrimerCSV ob = new LoadPrimerCSV(args[0]);
        LoadPrimerCSV ob = new LoadPrimerCSV(args[0], args[1]);
        //ob.addTransformedSeq2FRPrimer(ob.primersInfors);

    }

}
