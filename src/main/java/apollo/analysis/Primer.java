package apollo.analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yzheng on 12/21/15.
 */
public class Primer {
    public String primerName_F;
    public String primerName_R;
    public String sequencewithIllumina5Tag_F;
    public String sequencewithIllumina5Tag_R;
    public String originalSequence_F;
    public String originalSequence_R;
    public String originalSequence_F_noMixedBase;
    public String originalSequence_R_noMixedBase;
    public String organismName = null;
    public int expectedAmpliconSize = 0; //need to find out value from NCBI website
    public int plusIllumina = 67;      // default number given
    public int plusIlluminaNextera = 72;    //default number given
    public Map<Integer, String> mixedCodePositions_F = new HashMap<Integer, String>();
    public Map<Integer, String> mixedCodePositions_R = new HashMap<Integer, String>();
    public String genomeID = null;
    public int startPosinGenome = 0;
    public int endPosinGenome = 0;

    public Primer(String primerNameF, String primerNameR, String sequencewithIllumina5TagF,
                  String sequencewithIllumina5TagR, String originalSequenceF, String originalSequenceR,
                  String organismNa, int expectedAmpliconSiz ){

        this.primerName_F = primerNameF;
        this.primerName_R = primerNameR;
        this.sequencewithIllumina5Tag_F = sequencewithIllumina5TagF;
        this.sequencewithIllumina5Tag_R = sequencewithIllumina5TagR;
        this.originalSequence_F = originalSequenceF;
        this.originalSequence_R = originalSequenceR;
        this.organismName = organismNa;
        this.expectedAmpliconSize = expectedAmpliconSiz;
    }

    public Primer(String primerNameF, String primerNameR, String sequencewithIllumina5TagF,
                  String sequencewithIllumina5TagR, String originalSequenceF, String originalSequenceR,
                  String organismNa, int expectedAmpliconSiz, int plusIllumina, int plusIlluminaNextera,
                  String originalSequence_F_noMixedBase, String originalSequence_R_noMixedBase,
                  String genomeID, int startPosinGenome, int endPosinGenome) {

        this.primerName_F = primerNameF;
        this.primerName_R = primerNameR;
        this.sequencewithIllumina5Tag_F = sequencewithIllumina5TagF;
        this.sequencewithIllumina5Tag_R = sequencewithIllumina5TagR;
        this.originalSequence_F = originalSequenceF;
        this.originalSequence_R = originalSequenceR;
        this.organismName = organismNa;
        this.expectedAmpliconSize = expectedAmpliconSiz;
        this.plusIllumina = plusIllumina;
        this.plusIlluminaNextera = plusIlluminaNextera;
        this.originalSequence_F_noMixedBase = originalSequence_F_noMixedBase;
        this.originalSequence_R_noMixedBase = originalSequence_R_noMixedBase;
        this.genomeID =  genomeID;
        this.startPosinGenome = startPosinGenome;
        this.endPosinGenome = endPosinGenome;
    }
}


