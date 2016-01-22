package NCBIServerAccess;

import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.compound.DNACompoundSet;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;
import org.biojava.nbio.core.sequence.io.GenericGenbankHeaderParser;
import org.biojava.nbio.core.sequence.loader.GenbankProxySequenceReader;
import org.biojava.nbio.core.sequence.template.AbstractSequence;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by yzheng on 12/30/15.
 */
public class GenbankProxySequenceReaderTest {
    //private String gi = "800893094";
    private String gi = "KU341742.1";

    private final static Logger logger = LoggerFactory.getLogger(GenbankProxySequenceReaderTest.class);

    @Test
    public void testFeatures() throws IOException, InterruptedException, CompoundNotFoundException {
        logger.info("run test for DNA: {}", gi);

        GenbankProxySequenceReader<NucleotideCompound> genbankReader
                = new GenbankProxySequenceReader<NucleotideCompound>(System.getProperty("java.io.tmpdir"),
                this.gi, DNACompoundSet.getDNACompoundSet());
        genbankReader.getHeaderParser();
        GenericGenbankHeaderParser<AbstractSequence<NucleotideCompound>, NucleotideCompound> headerParser1 =
                genbankReader.getHeaderParser();


        /*
         * test get DNA sequence
         *
         */
        DNASequence seq = new DNASequence(genbankReader);
        Assert.assertNotNull("DNA sequence is null", seq);
        String tmp = seq.getSequenceAsString().substring(797113-1,797274);
        System.out.println("tmp " + tmp);
        seq.getAccession();
        seq.getDescription();
        seq.getOriginalHeader();

        /**
         parse description from header. There is no separate interface/abstract class for method getHeader()
         so it should be done here (manualy).
         */
        genbankReader.getHeaderParser().parseHeader(genbankReader.getHeader(), seq);

        // test description
        Assert.assertTrue(seq.getDescription() != null);

        // test accession Id
        logger.info("accession id: {}", seq.getAccession().getID());
        Assert.assertNotNull(seq.getAccession().getID());
        seq.getAccession().getVersion();
        seq.getDescription();

        // test GID number
        Assert.assertEquals(gi, seq.getAccession().getIdentifier());
        logger.info("found identifier '{}'", seq.getAccession().getIdentifier());

        // test taxonomy id
        logger.info("taxonomy id: {}", seq.getTaxonomy().getID());
        Assert.assertNotNull(seq.getTaxonomy().getID());
        Assert.assertNotNull(Integer.decode(seq.getTaxonomy().getID().split(":")[1]));

        // test taxonomy name
        String taxonName = seq.getFeaturesByType("source").get(0).getQualifiers().get("organism").getValue();
        logger.info("taxonomy name '{}'", taxonName);
        Assert.assertNotNull(taxonName);
    }

}
