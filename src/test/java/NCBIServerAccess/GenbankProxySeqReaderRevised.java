package NCBIServerAccess;

/**
 * Created by yzheng on 1/6/16.
 */
/*public class GenbankProxySeqReaderRevised <C extends Compound> extends StringProxySequenceReader<C>
        implements FeaturesKeyWordInterface, DatabaseReferenceInterface, FeatureRetriever {

    private final static Logger logger = LoggerFactory.getLogger(<C extends Compound> extends
    StringProxySequenceReader<C> implements FeaturesKeyWordInterface, DatabaseReferenceInterface, FeatureRetriever {

        private final static Logger logger = LoggerFactory.getLogger(GenbankProxySeqReaderRevised.class);

        private static final String eutilBaseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/"; //
        private String genbankDirectoryCache = null;
        private GenbankSequenceParser<AbstractSequence<C>, C> genbankParser;
        private GenericGenbankHeaderParser<AbstractSequence<C>, C> headerParser;
        private String header;
        private HashMap<String, ArrayList<AbstractFeature>> features;


        *//**
         *
         * @throws InterruptedException
         * @throws IOException
         * @throws CompoundNotFoundException
         *//*
        public GenbankProxySeqReaderRevised(
                String genbankDirectoryCache,
                String accessionID,
                CompoundSet<C> compoundSet ) throws IOException, InterruptedException, CompoundNotFoundException {

            setGenbankDirectoryCache(genbankDirectoryCache);
            setCompoundSet(compoundSet);

            String db = compoundSet instanceof AminoAcidCompoundSet ? "protein" : "nuccore";

            InputStream inStream = getBufferedInputStream(accessionID, db);
            genbankParser = new GenbankSequenceParser<AbstractSequence<C>, C>();

            setContents(genbankParser.getSequence(new BufferedReader(new InputStreamReader(inStream)), 0));
            headerParser = genbankParser.getSequenceHeaderParser();
            header = genbankParser.getHeader();
            features = genbankParser.getFeatures();

            if (compoundSet.getClass().equals(AminoAcidCompoundSet.class)) {
                if (!genbankParser.getCompoundType().equals(compoundSet)) {
                    logger.error("Declared compount type {} does not mach the real: {}", genbankParser.getCompoundType().toString(), compoundSet.toString());
                    throw new IOException("Wrong declared compound type for: " + accessionID);
                }
            }

            inStream.close();
        }.class);

    private static final String eutilBaseURL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/"; //
    private String genbankDirectoryCache = null;
    private GenbankSequenceParser<AbstractSequence<C>, C> genbankParser;
    private GenericGenbankHeaderParser<AbstractSequence<C>, C> headerParser;
    private String header;
    private HashMap<String, ArrayList<AbstractFeature>> features;


    *//**
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws CompoundNotFoundException
     *//*
    public GenbankProxySequenceReader(
            String genbankDirectoryCache,
            String accessionID,
            CompoundSet<C> compoundSet ) throws IOException, InterruptedException, CompoundNotFoundException {

        setGenbankDirectoryCache(genbankDirectoryCache);
        setCompoundSet(compoundSet);

        String db = compoundSet instanceof AminoAcidCompoundSet ? "protein" : "nuccore";

        InputStream inStream = getBufferedInputStream(accessionID, db);
        genbankParser = new GenbankSequenceParser<AbstractSequence<C>, C>();

        setContents(genbankParser.getSequence(new BufferedReader(new InputStreamReader(inStream)), 0));
        headerParser = genbankParser.getSequenceHeaderParser();
        header = genbankParser.getHeader();
        features = genbankParser.getFeatures();

        if (compoundSet.getClass().equals(AminoAcidCompoundSet.class)) {
            if (!genbankParser.getCompoundType().equals(compoundSet)) {
                logger.error("Declared compount type {} does not mach the real: {}", genbankParser.getCompoundType().toString(), compoundSet.toString());
                throw new IOException("Wrong declared compound type for: " + accessionID);
            }
        }

        inStream.close();
    }
    }
}  */
