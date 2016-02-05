package NCBIServerAccess;


import apollo.analysis.Primer;
import apollo.analysis.LoadPrimerCSV;
import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.compound.AmbiguityDNACompoundSet;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;
import org.biojava.nbio.core.sequence.loader.GenbankProxySequenceReader;
import org.junit.Assert;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by yzheng on 1/4/16.
 */
public class GenbankProxySeqBatchAccessTest {
    public static org.slf4j.Logger logger = LoggerFactory.getLogger(GenbankProxySeqBatchAccessTest.class);

    GenbankProxySeqBatchAccessTest(String txtFileName, String flag, String outputPath)
            throws IOException, CompoundNotFoundException, InterruptedException {
        LoadPrimerCSV ob = new LoadPrimerCSV(txtFileName, flag);
        for(int i = 0; i < ob.primersInfors.size(); i++){
            System.out.println("~~~~ i " + i);
            Primer tmp = ob.primersInfors.get(i);
            String primerName = outputPath + "/" + tmp.primerName_F.trim() + ".fasta";
            getTargetSeq(tmp.genomeID, tmp.startPosinGenome, tmp.endPosinGenome, primerName);
            TimeUnit.SECONDS.sleep(2);
        }
    }

    public void getTargetSeq(String gi, int genomeStartPos, int genomeStopPos, String primerName)
            throws InterruptedException, IOException, CompoundNotFoundException { /**gi is genomeID*/
        logger.info("run test for DNA: {}", gi);
        /*GenbankProxySequenceReader<NucleotideCompound> genbankReader
                = new GenbankProxySequenceReader<NucleotideCompound>(System.getProperty("java.io.tmpdir"),
                gi, DNACompoundSet.getDNACompoundSet());   */
        GenbankProxySequenceReader<NucleotideCompound> genbankReader
                = new GenbankProxySequenceReader<NucleotideCompound>(System.getProperty("java.io.tmpdir"),
                gi, AmbiguityDNACompoundSet.getDNACompoundSet());

        DNASequence seq = new DNASequence(genbankReader);
        Assert.assertNotNull("DNA sequence is null", seq);
        String tmp = seq.getSequenceAsString().substring(genomeStartPos - 1, genomeStopPos);
        System.out.println("tmp " + gi + " " + tmp);
        seq.getAccession();
        seq.getDescription();
        seq.getOriginalHeader();
        printoutTargetSeqasFasta(gi, genomeStartPos, genomeStopPos,primerName, tmp);
        //return tmp;
    }

    public void printoutTargetSeqasFasta (String genomeID, int startPos, int endPos, String outputName, String targetedSeq)
            throws IOException {
        FileWriter outputStream = null;
        try{
            outputStream = new FileWriter(new File(outputName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert outputStream != null;
        outputStream.write(">gb|" + genomeID + "|:" + startPos + "-" + endPos + "\n" + targetedSeq);
        outputStream.close();
    }

    public static void main(String[] args) throws InterruptedException, IOException, CompoundNotFoundException {
        GenbankProxySeqBatchAccessTest ob = new GenbankProxySeqBatchAccessTest(args[0], args[1], args[2]);
    }

}
