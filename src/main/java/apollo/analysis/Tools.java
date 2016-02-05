package apollo.analysis;

import java.util.*;

/**
 * Created by yzheng on 12/28/15.
 */
public class Tools {
    private static Map<String, String> IUPACcode = new HashMap<String, String>(11);
    private static Map<String, String> IMPOSSIBLE_IUPACcode = new HashMap<String, String>(20);
    static{
        IUPACcode.put("R", "AG");
        IUPACcode.put("Y", "CT");
        IUPACcode.put("S", "GC");
        IUPACcode.put("W", "AT");
        IUPACcode.put("K", "GT");
        IUPACcode.put("M", "AC");
        IUPACcode.put("H", "ACT");
        IUPACcode.put("B", "CGT");
        IUPACcode.put("V", "ACG");
        IUPACcode.put("D", "AGT");
        IUPACcode.put("N", "ACGT");
        IMPOSSIBLE_IUPACcode.put("IMPOSSIBLE_IUPACcode", "NULLVALUE");
    }

    public static String isMixedBase (Character primerChar) {// for example : str = "MC" ; ch = "C"
        if (primerChar.toString().equals("R")) {
            return "A";
        } else if (primerChar.toString().equals("Y")) {
            return "C";
        } else if (primerChar.toString().equals("M")) {
            return "A";
        } else if (primerChar.toString().equals("K")) {
            return "G";
        } else if (primerChar.toString().equals("S")) {
            return "C";
        } else if (primerChar.toString().equals("W")) {
            return "T";
        } else if (primerChar.toString().equals("H")) {
            return "C";
        } else if (primerChar.toString().equals("B")) {
            return "G";
        } else if (primerChar.toString().equals("V")) {
            return "A";
        } else if (primerChar.toString().equals("D")) {
            return "T";
        } else if (primerChar.toString().equals("N")) {
            return "G";
        } else if (primerChar.toString().equals("I")) {
            return "A";
        }
        else
        return ("nullValue");
    }
    /** this method is used to find out the mixed base in the primer sequence and save this infor into
     * hapmap data type into FRPrimersInfor*/
    public static void findMixedBasePrimerSeqPos (ArrayList<Primer> primersInfor) {
        for (int i = 0; i < primersInfor.size(); i++) {
            Map<Integer, String> mixBasePos_F = new HashMap<Integer, String>();
            Map<Integer, String> mixBasePos_R = new HashMap<Integer, String>();
            for (int j = 0; j < primersInfor.get(i).originalSequence_F.length(); j++) {
                if (isMixedBase(primersInfor.get(i).originalSequence_F.charAt(j)) != "nullValue") {
                    mixBasePos_F.put(j, isMixedBase(primersInfor.get(i).originalSequence_F.charAt(j)));
                }
            }
            for (int k = 0; k < primersInfor.get(i).originalSequence_R.length(); k++) {
                if (isMixedBase(primersInfor.get(i).originalSequence_R.charAt(k)) != "nullValue") {
                    mixBasePos_R.put(k, isMixedBase(primersInfor.get(i).originalSequence_R.charAt(k)));
                }
            }
            /** update the primerinfor with mixedBase infor*/
            primersInfor.get(i).mixedCodePositions_F = mixBasePos_F;
            primersInfor.get(i).mixedCodePositions_R = mixBasePos_R;
            /** update the primerinfor with no mixed base primer sequence*/

        }
    }
    /** this method is used to assign the value to the primer-noMixedBase according to the information on the
     * FPRimersInfor*/
    public static void transformMixedBase (ArrayList<Primer> primersInfor){
        for(int i = 0; i < primersInfor.size(); i++){
            if(!primersInfor.get(i).mixedCodePositions_F.isEmpty()){
                primersInfor.get(i).originalSequence_F_noMixedBase =
                        iteratorFunction(primersInfor.get(i).mixedCodePositions_F, primersInfor.get(i).originalSequence_F);
            }
            if(!primersInfor.get(i).mixedCodePositions_R.isEmpty()){
                primersInfor.get(i).originalSequence_R_noMixedBase =
                        iteratorFunction(primersInfor.get(i).mixedCodePositions_R, primersInfor.get(i).originalSequence_R);
            }
            if(primersInfor.get(i).mixedCodePositions_F.isEmpty()){
                primersInfor.get(i).originalSequence_F_noMixedBase =
                        primersInfor.get(i).originalSequence_F;
            }
            if(primersInfor.get(i).mixedCodePositions_R.isEmpty()){
                primersInfor.get(i).originalSequence_R_noMixedBase =
                        primersInfor.get(i).originalSequence_R;
            }

        }
    }

    public static String iteratorFunction(Map<Integer, String> mixedbase_pos, String primerSeq){

        StringBuilder primerNoMixedBase = new StringBuilder(primerSeq);
        /** sort the map */
        Map<Integer, String> treeMap = new TreeMap<Integer, String>(mixedbase_pos);
        for(Integer eachKey: treeMap.keySet()){
            primerNoMixedBase.setCharAt(eachKey, treeMap.get(eachKey).charAt(0));
        }
        return primerNoMixedBase.toString();
    }
}
