package apollo.analysis;

import apollo.analysis.PrimerBlastHtmlParser.PrimerBlastHtmlParserException;
import apollo.datamodel.CurationSet;
import apollo.datamodel.SequenceI;
import apollo.datamodel.template.PrimerBlastResult;
import apollo.util.FeatureList;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

/** Sends and retrieves a request to NCBI's Primer-BLAST CGI.
 *
 * @author elee
 *
 */
public class RemotePrimerBlastNCBI {

  private static final String ENCODING = "UTF-8";
  private static final String PRIMER_BLAST_URL = "http://www.ncbi.nlm.nih.gov/tools/primer-blast/primertool.cgi";
  private static final int SLEEP = 3000;  //1000 milliseconds is one second

  private PrimerBlastOptions opts;

  /** Constructor.
   *
   * @param opts - Primer-BLAST options
   */
  public RemotePrimerBlastNCBI(PrimerBlastOptions opts)
  {
    this.opts = opts;
  }

  /** Run Primer-BLAST analysis.
   * @param result - Primer-Blast result
   * @throws Exception - All encompassing exception should something go wrong
   */
  public String runAnalysis(PrimerBlastResult result, String outputRetriInforFileName) throws Exception
  {
    InputStream response = sendRequest();
    String type = retrieveResponse(response, result, outputRetriInforFileName);
    response.close();
    return type;
  }

  /** Run Primer-BLAST analysis.
   *
   * @param cs - CurationSet which will hold the BLAST results
   * @param seq - genomic sequence that will be blasted
   * @param offset - genomic position of the start of segment
   * @return name of the analysis run
   * @throws Exception - All encompassing exception should something go wrong
   */
  public String runAnalysis(CurationSet cs, SequenceI seq, int offset) throws Exception
  {
    return runAnalysis(cs, seq, offset, null);
  }

  /** Run Primer-BLAST analysis.
   *
   * @param cs - CurationSet which will hold the BLAST results
   * @param seq - genomic sequence that will be blasted
   * @param offset - genomic position of the start of segment
   * @param fl - FeatureList of selected features to filter against
   * @return name of the analysis run
   * @throws Exception - All encompassing exception should something go wrong
   */
  public String runAnalysis(CurationSet cs, SequenceI seq, int offset, FeatureList fl) throws Exception
  {
    InputStream response = sendRequest(seq);
    String type = retrieveResponse(response, cs, offset, fl);
    response.close();
    return type;
  }

  private InputStream sendRequest() throws IOException, InterruptedException {
    StringBuilder putBuf = new StringBuilder();
    processOptions(putBuf);
    URL url = new URL(PRIMER_BLAST_URL);
    HttpURLConnection conn;
    do {
      conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
      wr.write(putBuf.toString());
      wr.flush();
      wr.close();
      TimeUnit.SECONDS.sleep(2);
    } while (conn.getResponseCode() == 502);

    return conn.getInputStream();
  }

  private InputStream sendRequest(SequenceI seq) throws UnsupportedEncodingException, IOException
  {
    StringBuilder putBuf = new StringBuilder();
    processOptions(putBuf);
    putBuf.append("INPUT_SEQUENCE=");
    putBuf.append(URLEncoder.encode(">" + seq.getName() + "\n", ENCODING));
    putBuf.append(URLEncoder.encode(seq.getResidues(), ENCODING));
    URL url = new URL(PRIMER_BLAST_URL);
    URLConnection conn = url.openConnection();
    conn.setDoOutput(true);
    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
    wr.write(putBuf.toString());
    wr.flush();
    wr.close();
    apollo.util.IOUtil.informationDialog("Primer-BLAST request sent");
    return conn.getInputStream();
  }

  private String retrieveResponse(InputStream is, PrimerBlastResult result, String outputRetrivedInfoFN)
          throws IOException, InterruptedException,
       NCBIPrimerBlastTemplateHtmlParser.PrimerBlastHtmlParserException {
    InputStream copy = copyStream(is);
    String resultsUrl;
    while ((resultsUrl = getRedirectionUrl(copy)) != null) {
      Thread.sleep(SLEEP);
      is = new URL(resultsUrl).openStream();
      copy = copyStream(is);
    }
    copy.reset();
    NCBIPrimerBlastTemplateHtmlParser parser = new NCBIPrimerBlastTemplateHtmlParser();
    return parser.parse(copy, result, outputRetrivedInfoFN);
  }

  private String retrieveResponse(InputStream is, CurationSet cs, int offset, FeatureList fl) throws IOException, InterruptedException,
  PrimerBlastHtmlParserException
  {
    InputStream copy = copyStream(is);
    String resultsUrl;
    while ((resultsUrl = getRedirectionUrl(copy)) != null) {
      Thread.sleep(SLEEP);
      is = new URL(resultsUrl).openStream();
      copy = copyStream(is);
    }
    copy.reset();
    PrimerBlastHtmlParser parser = new PrimerBlastHtmlParser();
    return parser.parse(copy, cs.getResults(), offset, opts.isRemovePairsNotInExons() ? fl : null);
  }

  private InputStream copyStream(InputStream is) throws IOException
  {
    StringBuilder buf = new StringBuilder();
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line;
    while ((line = br.readLine()) != null) {
      buf.append(line + "\n");
    }

    return new ByteArrayInputStream(buf.toString().getBytes());
  }

  private String getRedirectionUrl(InputStream is) throws IOException
  {
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line;
    while ((line = br.readLine()) != null) {
      // System.out.println(line);
      if (line.contains("META HTTP-EQUIV=Refresh")) {
        String []tokens = line.split("\\s+");
        return tokens[3].substring(4, tokens[3].length() - 2);
      }
    }
    return null;
  }

  private void processOptions(StringBuilder buf) throws UnsupportedEncodingException
  {
    buf.append("PRIMER5_START=" + convertOptionToString(opts.getPrimer5Start()) + "&");
    buf.append("PRIMER5_END=" + convertOptionToString(opts.getPrimer5End()) + "&");
    buf.append("PRIMER3_START=" + convertOptionToString(opts.getPrimer3Start()) + "&");
    buf.append("PRIMER3_END=" + convertOptionToString(opts.getPrimer3End()) + "&");

    buf.append("PRIMER_LEFT_INPUT=" + convertOptionToString(opts.getPrimerLeftInput()) + "&");
    buf.append("PRIMER_RIGHT_INPUT=" + convertOptionToString(opts.getPrimerRightInput()) + "&");
    buf.append("PRIMER_PRODUCT_MIN=" + convertOptionToString(opts.getPrimerProductMin()) + "&");
    buf.append("PRIMER_PRODUCT_MAX=" + convertOptionToString(opts.getPrimerProductMax()) + "&");
    buf.append("PRIMER_NUM_RETURN=" + convertOptionToString(opts.getPrimerNumReturn()) + "&");
    buf.append("PRIMER_MIN_TM=" + convertOptionToString(opts.getPrimerMinTm()) + "&");
    buf.append("PRIMER_OPT_TM=" + convertOptionToString(opts.getPrimerOptTm()) + "&");
    buf.append("PRIMER_MAX_TM=" + convertOptionToString(opts.getPrimerMaxTm()) + "&");
    buf.append("PRIMER_MAX_DIFF_TM=" + convertOptionToString(opts.getPrimerMaxDiffTm()) + "&");
    buf.append("PRIMER_ON_SPLICE_SITE=" + convertOptionToString(opts.getPrimerOnSpliceSite()) + "&");
    buf.append("SPLICE_SITE_OVERLAP_5END=" + convertOptionToString(opts.getSpliceSiteOverlap5end()) + "&");
    buf.append("SPLICE_SITE_OVERLAP_3END=" + convertOptionToString(opts.getSpliceSiteOverlap3end()) + "&");
    buf.append("MIN_INTRON_SIZE=" + convertOptionToString(opts.getMinIntronSize()) + "&");
    buf.append("MAX_INTRON_SIZE=" + convertOptionToString(opts.getMaxIntronSize()) + "&");
    buf.append("SEARCH_SPECIFIC_PRIMER=" + (opts.isSearchSpecificPrimer() ? "on" : "off") + "&");
    buf.append("SEARCHMODE=" + convertOptionToString(opts.searchMode) + "&");
    buf.append("PRIMER_SPECIFICITY_DATABASE=" + convertOptionToString(opts.getPrimerSpecificityDatabase().toCGIParameter()) + "&");
    buf.append("ORGANISM=" + convertOptionToString(opts.getOrganism()) + "&");
    buf.append("TOTAL_PRIMER_SPECIFICITY_MISMATCH=" + convertOptionToString(opts.getTotalPrimerSpecificityMismatch()) + "&");
    buf.append("PRIMER_3END_SPECIFICITY_MISMATCH=" + convertOptionToString(opts.getPrimer3endSpecificityMismatch()) + "&");
    buf.append("MISMATCH_REGION_LENGTH=" + convertOptionToString(opts.getMismatchRegionLength()) + "&");
    buf.append("TOTAL_MISMATCH_IGNORE=" + convertOptionToString(opts.getTotalMismatchIgnore()) + "&");
    buf.append("MAX_TARGET_SIZE=" + convertOptionToString(opts.getMaxTargetSize()) + "&");
    buf.append("HITSIZE=" + convertOptionToString(opts.getHitSize()) + "&");
    buf.append("UNGAPPED_BLAST=" + convertOptionToString(opts.isUngappedBlast() ? "on" : "off") + "&");
    buf.append("EVALUE=" + convertOptionToString(opts.geteValue()) + "&");
    buf.append("WORD_SIZE=" + convertOptionToString(opts.getWordSize()) + "&");
    buf.append("MAX_CANDIDATE_PRIMER=" + convertOptionToString(opts.getMaxCandidatePrimer()) + "&");
    buf.append("NUM_TARGETS=" + convertOptionToString(opts.getNumTargers()) + "&");
    buf.append("NUM_TARGETS_WITH_PRIMERS=" + convertOptionToString(opts.getNumTargersWithPrimers()) + "&");
    buf.append("MAX_TARGET_PER_TEMPLATE=" + convertOptionToString(opts.getMaxTargerPerTemplate()) + "&");
    buf.append("PRIMER_MIN_SIZE=" + convertOptionToString(opts.getPrimerMinSize()) + "&");
    buf.append("PRIMER_OPT_SIZE=" + convertOptionToString(opts.getPrimerOptSize()) + "&");
    buf.append("PRIMER_MAX_SIZE=" + convertOptionToString(opts.getPrimerMaxSize()) + "&");
    buf.append("PRIMER_MIN_GC=" + convertOptionToString(opts.getPrimerMinGC()) + "&");
    buf.append("PRIMER_MAX_GC=" + convertOptionToString(opts.getPrimerMaxGC()) + "&");
    buf.append("GC_CLAMP=" + convertOptionToString(opts.getGcClamp()) + "&");
    buf.append("POLYX=" + convertOptionToString(opts.getPolyx()) + "&");
    buf.append("PRIMER_MAX_END_STABILITY=" + convertOptionToString(opts.getPrimerMaxEndStability()) + "&");
    buf.append("PRIMER_MAX_END_GC=" + convertOptionToString(opts.getPrimerMaxEndGC()) + "&");
    buf.append("PRIMER_MAX_TEMPLATE_MISPRIMING_TH=" + convertOptionToString(opts.getPrimerMaxTemplateMisprimingTH()) + "&");
    buf.append("PRIMER_PAIR_MAX_TEMPLATE_MISPRIMING_TH" + convertOptionToString(opts.getPrimerPairMaxTemplateMisprimingTH()) + "&");
    buf.append("PRIMER_MAX_SELF_ANY_TH=" + convertOptionToString(opts.getPrimerMaxSelfAnyTH()) + "&");
    buf.append("PRIMER_MAX_SELF_END_TH=" + convertOptionToString(opts.getPrimerMaxSelfEndTH()) + "&");
    buf.append("PRIMER_PAIR_MAX_COMPL_ANY_TH=" + convertOptionToString(opts.getPrimerPairMaxComplAnyTH()) + "&");
    buf.append("PRIMER_PAIR_MAX_COMPL_END_TH=" + convertOptionToString(opts.getPrimerPairMaxComplEndTH()) + "&");
    buf.append("PRIMER_MAX_HAIRPIN_TH=" + convertOptionToString(opts.getPrimerMaxHairpinTH()) + "&");
    buf.append("PRIMER_MAX_TEMPLATE_MISPRIMING=" + convertOptionToString(opts.getPrimerMaxTemplateMispriming()) + "&");
    buf.append("PRIMER_PAIR_MAX_TEMPLATE_MISPRIMING=" + convertOptionToString(opts.getPrimerPairMaxTemplateMispriming()) + "&");
    buf.append("SELF_ANY=" + convertOptionToString(opts.getSelfAny()) + "&");
    buf.append("SELF_END=" + convertOptionToString(opts.getSelfEnd()) + "&");
    buf.append("PRIMER_PAIR_MAX_COMPL_ANY=" + convertOptionToString(opts.getPrimerPairMaxComplAny) + "&");
    buf.append("PRIMER_PAIR_MAX_COMPL_END=" + convertOptionToString(opts.getPrimerPairMaxComplEnd) + "&");
    buf.append("OVERLAP_5END=" + convertOptionToString(opts.getOverlap5End()) + "&");
    buf.append("OVERLAP_3END=" + convertOptionToString(opts.getOverlap3End()) + "&");
    buf.append("MONO_CATIONS=" + convertOptionToString(opts.getMonoCations()) + "&");
    buf.append("DIVA_CATIONS=" + convertOptionToString(opts.getDivaCations()) + "&");
    buf.append("CON_DNTPS=" + convertOptionToString(opts.getConDNTPS()) + "&");
    buf.append("SALT_FORMULAR=" + convertOptionToString(opts.getSaltFormular()) + "&");
    buf.append("TM_METHOD=" + convertOptionToString(opts.getTmMethod()) + "&");
    buf.append("CON_ANEAL_OLIGO=" + convertOptionToString(opts.getConAnealOligo()) + "&");
    buf.append("PRIMER_MISPRIMING_LIBRARY=" + convertOptionToString(opts.getPrimerMisprimingLibrary()) + "&");
    buf.append("LOW_COMPLEXITY_FILTER=" + convertOptionToString(opts.isLowComplexityFilter() ? "on" : "off") + "&");
    buf.append("PRIMER_INTERNAL_OLIGO_MIN_SIZE=" + convertOptionToString(opts.getPrimerInternalOligoMinSize()) + "&");
    buf.append("PRIMER_INTERNAL_OLIGO_OPT_SIZE=" + convertOptionToString(opts.getPrimerInternalOligoOptSize()) + "&");
    buf.append("PRIMER_INTERNAL_OLIGO_MAX_SIZE=" + convertOptionToString(opts.getPrimerInternalOligoMaxSize()) + "&");
    buf.append("PRIMER_INTERNAL_OLIGO_MIN_TM=" + convertOptionToString(opts.getPrimerInternalOligoMinTM()) + "&");
    buf.append("PRIMER_INTERNAL_OLIGO_OPT_TM=" + convertOptionToString(opts.getPrimerInternalOligoOptTM()) + "&");
    buf.append("PRIMER_INTERNAL_OLIGO_MAX_TM=" + convertOptionToString(opts.getPrimerInternalOligoMaxTM()) + "&");
    buf.append("PRIMER_INTERNAL_OLIGO_MIN_GC=" + convertOptionToString(opts.getPrimerInternalOligoMinGC()) + "&");
    buf.append("PRIMER_INTERNAL_OLIGO_OPT_GC_PERCENT=" + convertOptionToString(opts.getPrimerInternalOligoOptGCPercent()) + "&");
    buf.append("PRIMER_INTERNAL_OLIGO_MAX_GC=" + convertOptionToString(opts.getPrimerInternalOligoMaxGC()) + "&");
    buf.append("SHOW_SVIEWER=" + convertOptionToString(opts.isShowSviewer() ? "on" : "off") + "&");
    buf.append("CMD=" + convertOptionToString(opts.getCmd()) + "&");
    buf.append("NUM_DIFFS=" + convertOptionToString(opts.getNumDiffs()) + "&");
    buf.append("NUM_OPTS_DIFFS=" + convertOptionToString(opts.getNumOptsDiffs()) + "&");
    //buf.append("PRODUCT_SIZE_DEVIATION=" + convertOptionToString(opts.getProductSizeDeviation()) + "&");
  }

  private String convertOptionToString(Object opt) throws UnsupportedEncodingException
  {
    if (opt == null) {
      return "";
    }
    return URLEncoder.encode(opt.toString(), ENCODING);
  }

  /** Options for running Primer-BLAST.
   *
   */
  public static class PrimerBlastOptions
  {
    /** Database to search against for primer specificity.
     * refseq_rna - Refseq mRNA
     * genome_selected_species - Genome (reference assembly from selected organisms)
     * ref_assembly - Genome (chromosomes from all organisms)
     * nt - non-redundant set of transcripts
     *
     */
    public enum Database
    {
      refseq_rna,
      genome_selected_species,
      ref_assembly,
      nt;

      /** Convert database to the corresponding CGI parameter.
       *
       * @return database convereted to the correspoding CGI parameter
       */
      public String toCGIParameter()
      {
        switch (this) {
        case refseq_rna:
          return "refseq_rna";
        case genome_selected_species:
          return "primerdb/genome_selected_species";
        case ref_assembly:
          return "ref_assembly";
        case nt:
          return "nt";
        default:
          return null;
        }
      }

      /** Displays the corresponding label to the database.
       *
       * @return database label
       */
      public String toString()
      {
        switch (this) {
        case refseq_rna:
          return "Refseq mRNA (refseq_rna)";
        case genome_selected_species:
          return "Genome (reference assembly from selected organisms)";
        case ref_assembly:
          return "Genome (chromosomes from all organisms)";
        case nt:
          return "nr";
        default:
          return null;
        }
      }

    }

    // primer-blast parameters and default values
    private Integer start;
    private Integer end;
    private Integer primer5Start;
    private Integer primer5End;
    private Integer primer3Start;
    private Integer primer3End;
    private String primerLeftInput;
    private String primerRightInput;
    private Integer primerProductMin = 70;
    private Integer primerProductMax = 1000;
    private Integer primerNumReturn = 10;
    private Double primerMinTm = 57.0;
    private Double primerOptTm = 60.0;
    private Double primerMaxTm = 63.0;
    private Double primerMaxDiffTm = 3.0;
    private Integer primerOnSpliceSite = 0;
    private Integer spliceSiteOverlap5end = 7;
    private Integer spliceSiteOverlap3end = 4;
    private Integer minIntronSize = 1000;
    private Integer maxIntronSize = 1000000;
    private boolean searchSpecificPrimer = true;
    private Integer searchMode = 0;
    private Database primerSpecificityDatabase;
    private String organism;
    private Integer totalPrimerSpecificityMismatch = 1;
    private Integer primer3endSpecificityMismatch = 1;
    private Integer mismatchRegionLength = 5;
    private Integer totalMismatchIgnore = 6;
    private Integer maxTargetSize = 4000;
    private Integer hitSize = 50000;
    private boolean ungappedBlast = true;
    private Integer eValue = 30000;
    private Integer wordSize = 7;
    private Integer maxCandidatePrimer = 500;
    private Integer numTargers = 20;
    private Integer numTargersWithPrimers = 1000;
    private Integer maxTargerPerTemplate = 100;
    private Integer primerMinSize = 15;
    private Integer primerOptSize = 20;
    private Integer primerMaxSize = 25;
    private Double primerMinGC = 20.0;
    private Double primerMaxGC = 80.0;
    private Integer gcClamp = 0;
    private Integer polyx = 5;
    private Integer primerMaxEndStability = 9;
    private Integer primerMaxEndGC = 5;
    private Double primerMaxTemplateMisprimingTH = 40.0;
    private Double primerPairMaxTemplateMisprimingTH = 70.0;
    private Double primerMaxSelfAnyTH = 45.0;
    private Double primerMaxSelfEndTH = 35.0;
    private Double primerPairMaxComplAnyTH = 45.0;
    private Double primerPairMaxComplEndTH = 35.0;
    private Double primerMaxHairpinTH = 24.0;
    private Double primerMaxTemplateMispriming = 12.0;
    private Double primerPairMaxTemplateMispriming = 24.0;
    private Double selfAny = 8.0;
    private Double selfEnd = 3.0;
    private Double getPrimerPairMaxComplAny = 8.0;
    private Double getPrimerPairMaxComplEnd = 3.0;
    private Integer overlap5End = 7;
    private Integer overlap3End = 4;
    private Double monoCations = 50.0;
    private Double divaCations = 1.5;
    private Double conDNTPS = 0.6;
    private Integer saltFormular = 1;
    private Integer tmMethod = 1;
    private Double conAnealOligo = 50.0;
    private String primerMisprimingLibrary = "AUTO";
    private Boolean lowComplexityFilter = true;
    private Integer primerInternalOligoMinSize = 18;
    private Integer primerInternalOligoOptSize = 20;
    private Integer primerInternalOligoMaxSize = 27;
    private Double primerInternalOligoMinTM = 57.0;
    private Double primerInternalOligoOptTM = 60.0;
    private Double primerInternalOligoMaxTM = 63.0;
    private Double primerInternalOligoMinGC = 20.0;
    private Integer primerInternalOligoOptGCPercent = 50;
    private Double primerInternalOligoMaxGC = 80.0;
    private boolean showSviewer = false;
    private String cmd = "request";
    private Integer numDiffs = 1;
    private Integer numOptsDiffs = 0;

    public Integer getHitSize() {
      return hitSize;
    }

    public boolean isUngappedBlast() {
      return ungappedBlast;
    }

    public Integer geteValue() {
      return eValue;
    }

    public Integer getWordSize() {
      return wordSize;
    }

    public Integer getMaxCandidatePrimer() {
      return maxCandidatePrimer;
    }

    public Integer getNumTargers() {
      return numTargers;
    }

    public Integer getNumTargersWithPrimers() {
      return numTargersWithPrimers;
    }

    public Integer getMaxTargerPerTemplate() {
      return maxTargerPerTemplate;
    }

    public Integer getPrimerMinSize() {
      return primerMinSize;
    }

    public Integer getPrimerOptSize() {
      return primerOptSize;
    }

    public Integer getPrimerMaxSize() {
      return primerMaxSize;
    }

    public Double getPrimerMinGC() {
      return primerMinGC;
    }

    public Double getPrimerMaxGC() {
      return primerMaxGC;
    }

    public Integer getGcClamp() {
      return gcClamp;
    }

    public Integer getPolyx() {
      return polyx;
    }

    public Integer getPrimerMaxEndStability() {
      return primerMaxEndStability;
    }

    public Integer getPrimerMaxEndGC() {
      return primerMaxEndGC;
    }

    public Double getPrimerMaxTemplateMisprimingTH() {
      return primerMaxTemplateMisprimingTH;
    }

    public Double getPrimerPairMaxTemplateMisprimingTH() {
      return primerPairMaxTemplateMisprimingTH;
    }

    public Double getPrimerMaxSelfAnyTH() {
      return primerMaxSelfAnyTH;
    }

    public Double getPrimerMaxSelfEndTH() {
      return primerMaxSelfEndTH;
    }

    public Double getPrimerPairMaxComplAnyTH() {
      return primerPairMaxComplAnyTH;
    }

    public Double getPrimerPairMaxComplEndTH() {
      return primerPairMaxComplEndTH;
    }

    public Double getPrimerMaxHairpinTH() {
      return primerMaxHairpinTH;
    }

    public Double getPrimerMaxTemplateMispriming() {
      return primerMaxTemplateMispriming;
    }

    public Double getPrimerPairMaxTemplateMispriming() {
      return primerPairMaxTemplateMispriming;
    }

    public Double getSelfAny() {
      return selfAny;
    }

    public Double getSelfEnd() {
      return selfEnd;
    }

    public Double getGetPrimerPairMaxComplAny() {
      return getPrimerPairMaxComplAny;
    }

    public Double getGetPrimerPairMaxComplEnd() {
      return getPrimerPairMaxComplEnd;
    }

    public Integer getOverlap5End() {
      return overlap5End;
    }

    public Integer getOverlap3End() {
      return overlap3End;
    }

    public Double getMonoCations() {
      return monoCations;
    }

    public Double getDivaCations() {
      return divaCations;
    }

    public Double getConDNTPS() {
      return conDNTPS;
    }

    public Integer getSaltFormular() {
      return saltFormular;
    }

    public Integer getTmMethod() {
      return tmMethod;
    }

    public Double getConAnealOligo() {
      return conAnealOligo;
    }

    public String getPrimerMisprimingLibrary() {
      return primerMisprimingLibrary;
    }

    public Boolean isLowComplexityFilter() {
      return lowComplexityFilter;
    }

    public Integer getPrimerInternalOligoMinSize() {
      return primerInternalOligoMinSize;
    }

    public Integer getPrimerInternalOligoOptSize() {
      return primerInternalOligoOptSize;
    }

    public Integer getPrimerInternalOligoMaxSize() {
      return primerInternalOligoMaxSize;
    }

    public Double getPrimerInternalOligoMinTM() {
      return primerInternalOligoMinTM;
    }

    public Double getPrimerInternalOligoOptTM() {
      return primerInternalOligoOptTM;
    }

    public Double getPrimerInternalOligoMaxTM() {
      return primerInternalOligoMaxTM;
    }

    public Double getPrimerInternalOligoMinGC() {
      return primerInternalOligoMinGC;
    }

    public Integer getPrimerInternalOligoOptGCPercent() {
      return primerInternalOligoOptGCPercent;
    }

    public Double getPrimerInternalOligoMaxGC() {
      return primerInternalOligoMaxGC;
    }

    public String getCmd() {
      return cmd;
    }

    public Integer getNumDiffs() {
      return numDiffs;
    }

    public Integer getNumOptsDiffs() {
      return numOptsDiffs;
    }

    public void setHitSize(Integer hitSize) {

      this.hitSize = hitSize;
    }

    public void setUngappedBlast(boolean ungappedBlast) {
      this.ungappedBlast = ungappedBlast;
    }

    public void seteValue(Integer eValue) {
      this.eValue = eValue;
    }

    public void setWordSize(Integer wordSize) {
      this.wordSize = wordSize;
    }

    public void setMaxCandidatePrimer(Integer maxCandidatePrimer) {
      this.maxCandidatePrimer = maxCandidatePrimer;
    }

    public void setNumTargers(Integer numTargers) {
      this.numTargers = numTargers;
    }

    public void setNumTargersWithPrimers(Integer numTargersWithPrimers) {
      this.numTargersWithPrimers = numTargersWithPrimers;
    }

    public void setMaxTargerPerTemplate(Integer maxTargerPerTemplate) {
      this.maxTargerPerTemplate = maxTargerPerTemplate;
    }

    public void setPrimerMinSize(Integer primerMinSize) {
      this.primerMinSize = primerMinSize;
    }

    public void setPrimerOptSize(Integer primerOptSize) {
      this.primerOptSize = primerOptSize;
    }

    public void setPrimerMaxSize(Integer primerMaxSize) {
      this.primerMaxSize = primerMaxSize;
    }

    public void setPrimerMinGC(Double primerMinGC) {
      this.primerMinGC = primerMinGC;
    }

    public void setPrimerMaxGC(Double primerMaxGC) {
      this.primerMaxGC = primerMaxGC;
    }

    public void setGcClamp(Integer gcClamp) {
      this.gcClamp = gcClamp;
    }

    public void setPolyx(Integer polyx) {
      this.polyx = polyx;
    }

    public void setPrimerMaxEndStability(Integer primerMaxEndStability) {
      this.primerMaxEndStability = primerMaxEndStability;
    }

    public void setPrimerMaxEndGC(Integer primerMaxEndGC) {
      this.primerMaxEndGC = primerMaxEndGC;
    }

    public void setPrimerMaxTemplateMisprimingTH(Double primerMaxTemplateMisprimingTH) {
      this.primerMaxTemplateMisprimingTH = primerMaxTemplateMisprimingTH;
    }

    public void setPrimerPairMaxTemplateMisprimingTH(Double primerPairMaxTemplateMisprimingTH) {
      this.primerPairMaxTemplateMisprimingTH = primerPairMaxTemplateMisprimingTH;
    }

    public void setPrimerMaxSelfAnyTH(Double primerMaxSelfAnyTH) {
      this.primerMaxSelfAnyTH = primerMaxSelfAnyTH;
    }

    public void setPrimerMaxSelfEndTH(Double primerMaxSelfEndTH) {
      this.primerMaxSelfEndTH = primerMaxSelfEndTH;
    }

    public void setPrimerPairMaxComplAnyTH(Double primerPairMaxComplAnyTH) {
      this.primerPairMaxComplAnyTH = primerPairMaxComplAnyTH;
    }

    public void setPrimerPairMaxComplEndTH(Double primerPairMaxComplEndTH) {
      this.primerPairMaxComplEndTH = primerPairMaxComplEndTH;
    }

    public void setPrimerMaxHairpinTH(Double primerMaxHairpinTH) {
      this.primerMaxHairpinTH = primerMaxHairpinTH;
    }

    public void setPrimerMaxTemplateMispriming(Double primerMaxTemplateMispriming) {
      this.primerMaxTemplateMispriming = primerMaxTemplateMispriming;
    }

    public void setPrimerPairMaxTemplateMispriming(Double primerPairMaxTemplateMispriming) {
      this.primerPairMaxTemplateMispriming = primerPairMaxTemplateMispriming;
    }

    public void setSelfAny(Double selfAny) {
      this.selfAny = selfAny;
    }

    public void setSelfEnd(Double selfEnd) {
      this.selfEnd = selfEnd;
    }

    public void setGetPrimerPairMaxComplAny(Double getPrimerPairMaxComplAny) {
      this.getPrimerPairMaxComplAny = getPrimerPairMaxComplAny;
    }

    public void setGetPrimerPairMaxComplEnd(Double getPrimerPairMaxComplEnd) {
      this.getPrimerPairMaxComplEnd = getPrimerPairMaxComplEnd;
    }

    public void setOverlap5End(Integer overlap5End) {
      this.overlap5End = overlap5End;
    }

    public void setOverlap3End(Integer overlap3End) {
      this.overlap3End = overlap3End;
    }

    public void setMonoCations(Double monoCations) {
      this.monoCations = monoCations;
    }

    public void setDivaCations(Double divaCations) {
      this.divaCations = divaCations;
    }

    public void setConDNTPS(Double conDNTPS) {
      this.conDNTPS = conDNTPS;
    }

    public void setSaltFormular(Integer saltFormular) {
      this.saltFormular = saltFormular;
    }

    public void setTmMethod(Integer tmMethod) {
      this.tmMethod = tmMethod;
    }

    public void setConAnealOligo(Double conAnealOligo) {
      this.conAnealOligo = conAnealOligo;
    }

    public void setPrimerMisprimingLibrary(String primerMisprimingLibrary) {
      this.primerMisprimingLibrary = primerMisprimingLibrary;
    }

    public void setLowComplexityFilter(Boolean lowComplexityFilter) {
      this.lowComplexityFilter = lowComplexityFilter;
    }

    public void setPrimerInternalOligoMinSize(Integer primerInternalOligoMinSize) {
      this.primerInternalOligoMinSize = primerInternalOligoMinSize;
    }

    public void setPrimerInternalOligoOptSize(Integer primerInternalOligoOptSize) {
      this.primerInternalOligoOptSize = primerInternalOligoOptSize;
    }

    public void setPrimerInternalOligoMaxSize(Integer primerInternalOligoMaxSize) {
      this.primerInternalOligoMaxSize = primerInternalOligoMaxSize;
    }

    public void setPrimerInternalOligoMinTM(Double primerInternalOligoMinTM) {
      this.primerInternalOligoMinTM = primerInternalOligoMinTM;
    }

    public void setPrimerInternalOligoOptTM(Double primerInternalOligoOptTM) {
      this.primerInternalOligoOptTM = primerInternalOligoOptTM;
    }

    public void setPrimerInternalOligoMaxTM(Double primerInternalOligoMaxTM) {
      this.primerInternalOligoMaxTM = primerInternalOligoMaxTM;
    }

    public void setPrimerInternalOligoMinGC(Double primerInternalOligoMinGC) {
      this.primerInternalOligoMinGC = primerInternalOligoMinGC;
    }

    public void setPrimerInternalOligoOptGCPercent(Integer primerInternalOligoOptGCPercent) {
      this.primerInternalOligoOptGCPercent = primerInternalOligoOptGCPercent;
    }

    public void setPrimerInternalOligoMaxGC(Double primerInternalOligoMaxGC) {
      this.primerInternalOligoMaxGC = primerInternalOligoMaxGC;
    }

    public void setCmd(String cmd) {
      this.cmd = cmd;
    }

    public void setNumDiffs(Integer numDiffs) {
      this.numDiffs = numDiffs;
    }

    public void setNumOptsDiffs(Integer numOptsDiffs) {
      this.numOptsDiffs = numOptsDiffs;
    }

    private Integer productSizeDeviation;
    private boolean removePairsNotInExons;

    public void setSpliceSiteOverlap5end(Integer spliceSiteOverlap5end) {
      this.spliceSiteOverlap5end = spliceSiteOverlap5end;
    }

    public void setSpliceSiteOverlap3end(Integer spliceSiteOverlap3end) {
      this.spliceSiteOverlap3end = spliceSiteOverlap3end;
    }

    public void setMinIntronSize(Integer minIntronSize) {
      this.minIntronSize = minIntronSize;
    }

    public void setMaxIntronSize(Integer maxIntronSize) {
      this.maxIntronSize = maxIntronSize;
    }

    public void setSearchMode(Integer searchMode) {
      this.searchMode = searchMode;
    }

    public void setTotalMismatchIgnore(Integer totalMismatchIgnore) {
      this.totalMismatchIgnore = totalMismatchIgnore;
    }

    public void setMaxTargetSize(Integer maxTargetSize) {
      this.maxTargetSize = maxTargetSize;
    }

    public void setShowSviewer(boolean showSviewer) {
      this.showSviewer = showSviewer;
    }

    public Integer getSpliceSiteOverlap5end() {
      return spliceSiteOverlap5end;
    }

    public Integer getSpliceSiteOverlap3end() {
      return spliceSiteOverlap3end;
    }

    public Integer getMinIntronSize() {
      return minIntronSize;
    }

    public Integer getMaxIntronSize() {
      return maxIntronSize;
    }

    public Integer getSearchMode() {
      return searchMode;
    }

    public Integer getTotalMismatchIgnore() {
      return totalMismatchIgnore;
    }

    public Integer getMaxTargetSize() {
      return maxTargetSize;
    }

    public boolean isShowSviewer() {
      return showSviewer;
    }



    /** Get the forward primer start genomic coordinate.
     *
     * @return start genomic coordinate
     */
    public Integer getPrimer5Start() {
      return primer5Start;
    }

    /** Set the forward primer start genomic coordinate.
     *
     * @param primer5Start - start genomic coordinate
     */
    public void setPrimer5Start(Integer primer5Start) {
      this.primer5Start = primer5Start;
    }

    /** Get the forward primer end genomic coordinate.
     *
     * @return end genomic coordinate
     */
    public Integer getPrimer5End() {
      return primer5End;
    }

    /** Set the forward primer end genomic coordinate.
     *
     * @param primer5End - end genomic coordinate
     */
    public void setPrimer5End(Integer primer5End) {
      this.primer5End = primer5End;
    }

    /** Get the reverse primer start genomic coordinate.
     *
     * @return start genomic coordinate
     */
    public Integer getPrimer3Start() {
      return primer3Start;
    }

    /** Set the reverse primer start genomic coordinate.
     *
     * @param primer3Start - start genomic coordinate
     */
    public void setPrimer3Start(Integer primer3Start) {
      this.primer3Start = primer3Start;
    }

    /** Get the reverse primer end genomic coordinate.
     *
     * @return end genomic coordinate
     */
    public Integer getPrimer3End() {
      return primer3End;
    }

    /** Set the reverse primer end genomic coordinate.
     *
     * @param primer3End - end genomic coordinate
     */
    public void setPrimer3End(Integer primer3End) {
      this.primer3End = primer3End;
    }


    public String getPrimerLeftInput() {
      return primerLeftInput;
    }
    public void setPrimerLeftInput(String primerLeftInput) {
      this.primerLeftInput = primerLeftInput;
    }
    public String getPrimerRightInput() {
      return primerRightInput;
    }
    public void setPrimerRightInput(String primerRightInput) {
      this.primerRightInput = primerRightInput;
    }
    public Integer getPrimerProductMin() {
      return primerProductMin;
    }
    public void setPrimerProductMin(Integer primerProductMin) {
      this.primerProductMin = primerProductMin;
    }
    public Integer getPrimerProductMax() {
      return primerProductMax;
    }
    public void setPrimerProductMax(Integer primerProductMax) {
      this.primerProductMax = primerProductMax;
    }
    public Integer getPrimerNumReturn() {
      return primerNumReturn;
    }
    public void setPrimerNumReturn(Integer primerNumReturn) {
      this.primerNumReturn = primerNumReturn;
    }
    public Double getPrimerMinTm() {
      return primerMinTm;
    }
    public void setPrimerMinTm(Double primerMinTm) {
      this.primerMinTm = primerMinTm;
    }
    public Double getPrimerOptTm() {
      return primerOptTm;
    }
    public void setPrimerOptTm(Double primerOptTm) {
      this.primerOptTm = primerOptTm;
    }
    public Double getPrimerMaxTm() {
      return primerMaxTm;
    }
    public void setPrimerMaxTm(Double primerMaxTm) {
      this.primerMaxTm = primerMaxTm;
    }
    public Double getPrimerMaxDiffTm() {
      return primerMaxDiffTm;
    }
    public void setPrimerMaxDiffTm(Double primerMaxDiffTm) {
      this.primerMaxDiffTm = primerMaxDiffTm;
    }
    public Integer getPrimerOnSpliceSite() {
      return primerOnSpliceSite;
    }
    public void setPrimerOnSpliceSite(Integer primerOnSpliceSite) {
      this.primerOnSpliceSite = primerOnSpliceSite;
    }
    public boolean isSearchSpecificPrimer() {
      return searchSpecificPrimer;
    }
    public void setSearchSpecificPrimer(boolean searchSpecificPrimer) {
      this.searchSpecificPrimer = searchSpecificPrimer;
    }
    public String getOrganism() {
      return organism;
    }
    public void setOrganism(String organism) {
      this.organism = organism;
    }
    public Database getPrimerSpecificityDatabase() {
      return primerSpecificityDatabase;
    }
    public void setPrimerSpecificityDatabase(Database primerSpecificityDatabase) {
      this.primerSpecificityDatabase = primerSpecificityDatabase;
    }
    public Integer getTotalPrimerSpecificityMismatch() {
      return totalPrimerSpecificityMismatch;
    }
    public void setTotalPrimerSpecificityMismatch(Integer totalPrimerSpecificityMismatch) {
      this.totalPrimerSpecificityMismatch = totalPrimerSpecificityMismatch;
    }
    public Integer getPrimer3endSpecificityMismatch() {
      return primer3endSpecificityMismatch;
    }
    public void setPrimer3endSpecificityMismatch(Integer primer3endSpecificityMismatch) {
      this.primer3endSpecificityMismatch = primer3endSpecificityMismatch;
    }
    public Integer getMismatchRegionLength() {
      return mismatchRegionLength;
    }
    public void setMismatchRegionLength(Integer mismatchRegionLength) {
      this.mismatchRegionLength = mismatchRegionLength;
    }
    public Integer getProductSizeDeviation() {
      return productSizeDeviation;
    }
    public void setProductSizeDeviation(Integer productSizeDeviation) {
      this.productSizeDeviation = productSizeDeviation;
    }
    public Integer getStart() {
      return start;
    }
    public void setStart(Integer start) {
      this.start = start;
    }
    public Integer getEnd() {
      return end;
    }
    public void setEnd(Integer end) {
      this.end = end;
    }
    public boolean isRemovePairsNotInExons() {
      return removePairsNotInExons;
    }
    public void setRemovePairsNotInExons(boolean removePairsNotInExons) {
      this.removePairsNotInExons = removePairsNotInExons;
    }

  }
}
