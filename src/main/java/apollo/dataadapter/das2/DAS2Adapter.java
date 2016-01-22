package apollo.dataadapter.das2;

import java.util.*;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.net.URL;
import javax.swing.JOptionPane;

import apollo.datamodel.*;
import apollo.config.Config;
import apollo.dataadapter.AbstractApolloAdapter;
import apollo.dataadapter.ApolloDataAdapterI;
import apollo.dataadapter.DataInputType;
import apollo.dataadapter.NotImplementedException;
import apollo.dataadapter.ApolloAdapterException;

import apollo.dataadapter.das.*;
import apollo.dataadapter.das.simple.*;
import apollo.dataadapter.*;
import apollo.main.LoadUtil;

import org.apache.log4j.*;

import org.bdgp.io.*;
import org.bdgp.util.*;
import java.util.Properties;

/**
 * <p>I implement a <code>AbstractDataAdapter</code>, allowing Apollo to 
 * get information from DAS datasources, using 'simple'
 * implementions of <code>DASServerI</code>, <code>DASDsn</code> etc. </p>
**/
public class DAS2Adapter
  extends AbstractApolloAdapter 
{

  // -----------------------------------------------------------------------
  // Class/static variables
  // -----------------------------------------------------------------------

  protected final static Logger logger = LogManager.getLogger(DAS2Adapter.class);

  private static long groupIdInt = 0;

  // -----------------------------------------------------------------------
  // Instance variables
  // -----------------------------------------------------------------------

  private DAS2AdapterGUI gui;  // set by getUI
  String server;
  DASDsn dsn;
  DASSegment segment;
  private SequenceI genomeSeq = null;
  int low = 1;
  int high = 100000;
  private Properties stateInformation = new StateInformation();

    //  DASServerI dasServer;
  DAS2Request dasServer;

  String filename;  // FOR NOW, data is being read from a file

  /** This indicates no GUI so certain non-apollo, non-gui apps can use this
   *  adapter. (But who sets this?) */
  boolean NO_GUI = false;

  public DAS2Adapter() {}

    //  public DASServerI getDASServer(){
  public DAS2Request getDASServer(){
    return dasServer;
  }

    //  public void setDASServer(DASServerI newValue){
  public void setDASServer(DAS2Request newValue){
    dasServer = newValue;
    logger.debug("DAS2Adapter.setDasServer: URL = " + dasServer.getURL());
  }

  IOOperation [] supportedOperations = 
    {
      ApolloDataAdapterI.OP_READ_DATA,
      ApolloDataAdapterI.OP_READ_SEQUENCE,
      ApolloDataAdapterI.OP_WRITE_DATA
    };

  public void init() {}

  public DASDsn getDSN(){
    return dsn;
  }

  public String getName() {
    return "DAS/2 server";
  }

  public String getType() {
    return "CGI server";
  }

  public DataInputType getInputType() {
    return DataInputType.URL;
  }

  //  public String getInput() {
  //    return segment.getId(); // is this right??
  //  }

  // For reading from a file
  public void setInput(String filename) {
    logger.debug("setInput: filename = " + filename);
    this.filename = filename;
  }
  public String getInput() {
    return filename;
  }

  /** Used when Apollo was called from the command line rather than the GUI */
  public void setDataInput(DataInput dataInput) {
    super.setDataInput(dataInput);
    setInput(dataInput.getInputString());
  }

  public IOOperation [] getSupportedOperations() {
    return supportedOperations;
  }

  public DataAdapterUI getUI(IOOperation op) {
    if (gui == null)
      gui = new DAS2AdapterGUI(op);
    gui.setIOOperation(op);
    return gui;
  }

  private void setDSN(DASDsn dsn) {
    logger.debug("DAS2Adapter.setDSN: dsn = " + dsn + "; capabilities = " + ((SimpleDASDsn)dsn).getCapabilities());
    this.dsn = dsn;
  }

  private void setSegment(DASSegment segment){
    this.segment = segment;
    logger.trace("DAS2Adapter.setSegment: segment = " + segment);
  }

  private DASSegment getSegment(){
    return segment;
  }

  private void setLow(int low) {
    this.low = low;
  }

  private void setHigh(int high) {
    this.high = high;
  }

  private int getLow(){
    return low;
  }

  private int getHigh(){
    return high;
  }

  /**
   * This string is used when the user navigates around the genome - it should
   * contain both the new segment and high/low information. Armed with this, we can set the
   * DASSegment in this data adapter. The new data is set into the adapter with
   * two calls: setStateInformation (copying in the "old" state of the adapter
   * and then setRegion (copying in the new choices of region into the
   * adapter).
  **/
  public void setRegion(String region) throws ApolloAdapterException {
    SimpleDASSegment segment = (SimpleDASSegment)getSegment();

    logger.debug("DAS2Adapter.setRegion: region = " + region);
    if(region == null){
      return;
    }//end if
    
    StringTokenizer tokenizer = new StringTokenizer(region);
    String chrString = null;
    String chromosome = null;
    String start = null;
    String end = null;
    
    if(tokenizer.hasMoreTokens()){
      chrString = tokenizer.nextToken();
    }//end if
    
    if(tokenizer.hasMoreTokens()){
      chromosome = tokenizer.nextToken();
    }//end if
    
    if(tokenizer.hasMoreTokens()){
      start = tokenizer.nextToken();
    }//end if
    
    if(tokenizer.hasMoreTokens()){
      end = tokenizer.nextToken();
    }//end if
    
    if(chromosome == null || start == null || end == null){
      return;
    }//end if
    
    if(segment != null){
      segment.setId(chromosome);
      segment.setStart(start);
      segment.setStop(end);
    }else{
      setSegment(
        new SimpleDASSegment(
          null,//descr text
          chromosome,//id
          start,//start
          end,//stop
          null,//subparts
          null,//orientation
          null//length
        )
      );
    }//end if
    
    setLow(Integer.valueOf(start).intValue());
    setHigh(Integer.valueOf(end).intValue());
    
    getStateInformation().setProperty(StateInformation.REGION, region);
    getStateInformation().setProperty(StateInformation.SEGMENT_START, start);
    getStateInformation().setProperty(StateInformation.SEGMENT_STOP, end);
    // Need to save the capabilities too!  Are they going to have to go in the
    // property string as well?!
  }//end setRegion

  /**
   * The adapter is characterised by dsn (e.g. http://url/.../entry_point)
   * and segment (e.g. chr=1, start=... stop=....)
   * With this information we can make a das-call. So we write
   * this information out in entirety.
  **/
  public Properties getStateInformation() {
    return stateInformation;
  }//end getStateInformation

  /**
   * <p>With the following keys in the adapter properties, we can re-create a 
   * our internal state and start a new das-call.</p>
   * 
   * <ul>
   * <li> DAS_server_url </li>
   * <li> DSN_sourceId </li>
   * <li> DSN_source </li>
   * <li> DSN_sourceVersion </li>
   * <li> DSN_mapMaster </li>
   * <li> Segment_segment </li>
   * <li> Segment_id </li>
   * <li> Segment_start </li>
   * <li> Segment_stop </li>
   * <li> Segment_orientation </li>
   * <li> Segment_subparts </li>
   * <li> Segment_length </li>
   * </ul>
   *
  **/
  public void setStateInformation(Properties newProperties) {
    String proxySet;
    String proxyHost;
    String proxyPort;
    Properties props = getStateInformation();
    String dsnSourceId;
    String segmentId;
    String lowString;
    String highString;
    
    props.putAll(newProperties);

    if(props.getProperty(StateInformation.SERVER_URL) != null){
	//      setDASServer(new SimpleDASServer(props.getProperty(StateInformation.SERVER_URL)));
      setDASServer(new DAS2Request(props.getProperty(StateInformation.SERVER_URL)));
    }
    
    dsnSourceId = props.getProperty(StateInformation.DSN_SOURCE_ID);
    logger.trace("setStateInformation: dsnSourceId = " + dsnSourceId);

    if (dsnSourceId != null && dsnSourceId.trim().length()>0) {
	// !! Can we see if we already have this DSN as one of the DSNs attached to the DAS server,
	// or did we not set that yet?
	// Making a new SimpleDASDsn means we lose the capabilities.
	DAS2Request server = getDASServer();
	DASDsn dsn = server.getDSN(dsnSourceId);
	if (dsn == null) {
          logger.debug("DAS2Adapter: setting DSN to a new simpleDASDsn based on properties");
          setDSN(
                 new SimpleDASDsn(
                                  dsnSourceId,
                                  props.getProperty(StateInformation.DSN_SOURCE_VERSION),
                                  props.getProperty(StateInformation.DSN_SOURCE),
                                  props.getProperty(StateInformation.DSN_MAP_MASTER),
                                  props.getProperty(StateInformation.DSN_DESCRIPTION)
                                  )
                 );
	}
	else
          setDSN(dsn);  // found it
    }
    
    segmentId = props.getProperty(StateInformation.SEGMENT_SEGMENT);
    if(segmentId != null && segmentId.trim().length()>0){
      //      logger.debug("setStateInformation: segmentId = " + segmentId);
      //      logger.debug("setStateInformation: START = " + props.getProperty(StateInformation.SEGMENT_START));
      setSegment(
       new SimpleDASSegment(
          segmentId,
          props.getProperty(StateInformation.SEGMENT_ID),
          props.getProperty(StateInformation.SEGMENT_START),
          props.getProperty(StateInformation.SEGMENT_STOP),
          props.getProperty(StateInformation.SEGMENT_ORIENTATION),
          props.getProperty(StateInformation.SEGMENT_SUBPARTS),
          props.getProperty(StateInformation.SEGMENT_LENGTH)
       )
      );
    }
    else
      logger.debug("DAS2Adapter.setStateInformation: segment id is null!");
    
    lowString = props.getProperty(StateInformation.SEGMENT_START);
    if(lowString != null && lowString.trim().length() > 0){
      logger.trace("setStateInformation: setLow to " + lowString);
      setLow(Integer.valueOf(lowString).intValue());
    }
    
    highString = props.getProperty(StateInformation.SEGMENT_STOP);
    if(highString != null && highString.trim().length() > 0){
      setHigh(Integer.valueOf(highString).intValue());
    }
    
    proxySet = props.getProperty(StateInformation.HTTP_PROXY_SET);
    if(proxySet != null && "true".equals(proxySet)){
      proxyHost = props.getProperty(StateInformation.HTTP_PROXY_HOST);
      proxyPort = props.getProperty(StateInformation.HTTP_PROXY_PORT);
      System.setProperty("proxySet", "true");
      System.setProperty("proxyHost", proxyHost);
      System.setProperty("proxyPort", proxyPort);
    }
    
    //If we've been passed a region in the props file, then overwrite the segment with 
    //the implied high/low/id, as well as the high/low instance variables on this adapter.
    if (props.getProperty(StateInformation.REGION) != null) {
      try{
        setRegion(props.getProperty(StateInformation.REGION));
      }catch(ApolloAdapterException exception){
        throw new NonFatalDataAdapterException(exception.getMessage());
      }
    }

    //
    //Finally hang onto the new properties file we've created as this adapter's new
    //state information
    stateInformation = props;
  }//end setStateInformation

//   public CurationSet getCurationSet() throws ApolloAdapterException {

//     CurationSet curationSet = new CurationSet();

//     validateStateInformation();
    
//     curationSet
//       .setAnnots(
//         new StrandedFeatureSet(
//           new FeatureSet(),
//           new FeatureSet()
//         )
//       );

//     curationSet.setRefSequence (new Sequence (getSegment().getId(), ""));
//     curationSet.setResults(getAnalysisRegion(curationSet));
    
//     // This goes after when we know the user request is valid which i believe is here(?)
//     //super.clearOldData(); 
//     //apollo.dataadapter.debug.DisplayTool.showFeatureSet(curationSet.getResults());

//     curationSet.setLow(low);
//     curationSet.setHigh(high);

//     try {
//       genomeSeq =
//         getSequence(
//           new DbXref(
//             getDSN().getSourceId(),
//             getSegment().getId(),
//             getSegment().getId()
//           )
//         );

//       curationSet.setChromosome(segment.getId());
//       curationSet.setName (segment.getId());
//       curationSet.setRefSequence(genomeSeq);
//     } catch (Exception e) {
//       throw new ApolloAdapterException(
//         "Load failed. Are you sure " + segment
//         + "is a real sequence?", e
//       );
//     }

//     //
//     //This notifies all listeners that data loading is done. There is, at time
//     //of writing, at least one listener which replaces the cursor on the ApolloFrame
//     //from a wait cursor back to a default cursor.
//     //super.notifyLoadingDone();

//     return curationSet;
//   }//end getCurationSet

  public CurationSet getCurationSet() throws ApolloAdapterException {
    CurationSet curation = null;
    BufferedInputStream bis = null;

    try {
      fireProgressEvent(new ProgressEvent(this, new Double(5.0),
                                          "Finding data..."));
      // Throws DataAdapter exception if faulty input (like bad filename)
      InputStream xml_stream;
      // This is for reading from a DAS2XML file
      xml_stream = getDAS2XMLStream();
      bis = new BufferedInputStream(xml_stream);

      /* Now that we know input is not faulty (exception not thrown if
         we got here) we can clear out old data. If we don't clear we
         get exceptions on style changes as apollo tiers get in an
         inconsistent state between old and new data. */
      if (!NO_GUI)
        super.clearOldData();

      // All "progress" percentages are made up--we can't actually tell
      // how far along we are, but we want to give the user an idea that
      // progress is being made.
      fireProgressEvent(new ProgressEvent(this, new Double(10.0),
                                          "Reading XML..."));
    } catch (Exception e) {
      logger.error("Error trying to open XML stream: " + e.getMessage(), e);
      return null;
    }

    try {
      curation = new CurationSet();
      //      curation.setName(getInput());
      DAS2FeatureSaxParser parser = new DAS2FeatureSaxParser();
      parser.parse(bis, curation);
      bis.close();
      // !! Need to set curation region (based on segment?)
      setCurationRegion(curation);
      return curation;
    } catch (Exception e) {
      logger.error("Error trying to parse XML: " + e.getMessage(), e);
      return null;
    }

  }//end getCurationSet

  /** If we've requested a file, open a stream to it.
   *  Otherwise, open a stream to the URL. */
  private InputStream getDAS2XMLStream() 
    throws ApolloAdapterException {
    // See if we can get the data from an URL
    DASDsn theDSN = getDSN();
    logger.debug("DAS2Adapter.getDAS2XMLStream: theDSN = " + theDSN);
    if (theDSN == null) {
      // Data source not specified--open a stream to a file of DAS2XML data
      return das2XmlFileInputStream(getInput());
    }

    // Construct a query string like
    // http://das.biopackages.net/das/genome/yeast/S228C/feature?overlaps=chrXIII/22000:24000"
    String query = makeFeatureQuery();
    logger.debug("DAS2Adapter.getDAS2XMLStream: feature query = " + query);

    try {
      URL url = new URL(query);
      InputStream stream = url.openStream();
      Thread.sleep(50); // milliseconds
      if (stream.available() <= 1) {
        logger.error("Couldn't fetch requested region: " + query);
        return null;
      }
      return stream;
    } catch (Exception e) {
      // Give it more time to respond?
      logger.error("Couldn't open stream to " + query);
      throw new ApolloAdapterException("Couldn't open stream to " + query);
    }
  }

  private String getRegionString(DASSegment segment) {
    return segment.getSegment() + "/" + low + ":" + high;
  }

  /** Construct a name for the Apollo window based on a feature query URL like
   *  http://das.biopackages.net/das/genome/yeast/S228C/feature?overlaps=chrI/1111:2222 */
  private String makeRegionName(String featureQueryURL) {
    return featureQueryURL; // for now
  }

  /** The feature query should be constructed by using the CAPABILITY for
   *  this source from the SOURCE request, e.g.
   *  <CAPABILITY type="features" query_id="volvox/1/features"> */
  private String makeFeatureQuery() {
    //The selected segment was set on us
    //when we were created. We create a DASSegment from this name
    //and the selected high/low range.
    String region = getRegionString(getSegment());

    SimpleDASDsn dsn = (SimpleDASDsn)getDSN();
    String featureRequest = dsn.getCapabilityURI("features");
    if (featureRequest == null) {
	String warning = "Warning: couldn't get 'features' capability for " + dsn.getSourceId() + "; capabilities hash = " + dsn.getCapabilities() + "\nUsing hardcoded query 'feature'.";
	logger.warn(warning);
	JOptionPane.showMessageDialog(null,warning,"Warning",JOptionPane.WARNING_MESSAGE);
	featureRequest = "feature";
    }
    // dsn.getMapMaster() is the base URL for the versioned sources,
    // something like http://das.biopackages.net/das/genome/
    // (getSourceId returns something like http://das.biopackages.net/das/genome/yeast/S228C--not what we want)
    // featureRequest (from "features" capability) is something like yeast/S228C/feature
    // Want a query like http://das.biopackages.net/das/genome/yeast/S228C/feature?overlaps=chrI/2:2222
    String baseURL = dsn.getMapMaster();
    if (!baseURL.endsWith("/"))
	baseURL = baseURL + "/";
    return baseURL + featureRequest + "?overlaps="+region;
  }

  /** Open the requested file as a stream; check that file really does appear to contain
   *  DAS2XML before returning stream. */
  private InputStream das2XmlFileInputStream(String filename) throws ApolloAdapterException {
    if (filename == null)
      throw new ApolloAdapterException("No filename or DAS/2 source specified");

    InputStream stream = null;
    String path = apollo.util.IOUtil.findFile(filename, false);
    try {
      fireProgressEvent(new ProgressEvent(this, new Double(6.0),
                                          "Opening file..."));
      logger.debug("Trying to open DAS2XML file " + filename);
      stream = new FileInputStream (path);
    } catch (Exception e) { // FileNotFoundException
      stream = null;
      throw new ApolloAdapterException("could not open DAS2XML file " + filename + " for reading.");
    }
    // Check whether this is, in fact, DAS2XML by looking for a line that says das2
    BufferedReader in;
    try {
      in = new BufferedReader(new FileReader(path));
    } catch (Exception e) { // FileNotFoundException
      stream = null;
      throw new ApolloAdapterException("Error: could not open DAS2XML file " + 
                                       path + " for reading.");
    }
    fireProgressEvent(new ProgressEvent(this, new Double(7.0),
                                        "Validating file..."));
    // Look for a line identifying this as das2
    for (int i=0; i < 12; i++) {  // 12 lines should be enough, right?
      String line;
      try {
        line = in.readLine();
      } catch (Exception e) {
        throw new ApolloAdapterException("Error: DAS2XML file " + filename + 
                                         " is empty.");
      }
      // There should be a DOCTYPE line with a das2feature.dtd:
      // <!DOCTYPE DAS2FEATURE SYSTEM "http://www.biodas.org/dtd/das2feature.dtd"> 
      // but Andrew's examples don't include that.
      if (line.toLowerCase().indexOf("das2") >= 0)
        return stream;
      if (line.toLowerCase().indexOf("das/genome/2") >= 0)
        return stream;
      if (line.toLowerCase().indexOf("das/2.") >= 0)
        return stream;
    }
    throw new ApolloAdapterException("Error: file " + filename + 
                                     " does not appear to contain DAs2XML.");
  }

  /** Not currently used */
  private List parseFeatureSet(){
    StrandedFeatureSetI parentStandedFeatureSet;
    DASSegment inputSegment;

    //Holder for the sourceId of the data source (e.g. 'elegans' on wormbase)
    DASDsn theDSN = getDSN();
    logger.debug("DAS2Adapter.parseFeatureSet: theDSN = " + theDSN);

    //The selected segment was set on us
    //when we were created. We create a DASSegment from this name
    //and the selected high/low range. Then we can call getFeatures()
    //to return all DASFeatures from the server.
    inputSegment = getSegment();

    return
      getDASServer().getFeatures(
        theDSN,
        new DASSegment[]{inputSegment}
      );
  }//end parseFeatures

  private void initializeMethodNameFeatureTypeFeatureSet(
    FeatureSetI targetFeatureSet,
    DASFeature theSourceFeature
  ){
    targetFeatureSet.setId (theSourceFeature.getTypeId());
    // targetFeatureSet.setHolder(true);

    targetFeatureSet.setStrand(getStrandForOrientation(theSourceFeature.getOrientation()));
    targetFeatureSet.setProgramName(theSourceFeature.getMethodLabel());
    targetFeatureSet.setDatabase(theSourceFeature.getTypeId());
    targetFeatureSet.setFeatureType(getMethodAndFeatureType(theSourceFeature));

    targetFeatureSet.setName (theSourceFeature.getTypeLabel());
    // targetFeatureSet.setHolder (true);

  }//end initializeMethodNameFeatureTypeFeatureSet

  private void initializeGroupFeatureSet(
    FeatureSetI groupFeatureSet,
    DASFeature theFeature,
    boolean setIsHolder
  ){
    String methodAndFeatureType = getMethodAndFeatureType(theFeature);
    // groupFeatureSet.setHolder(setIsHolder);
    groupFeatureSet.setStrand(getStrandForOrientation(theFeature.getOrientation()));
    groupFeatureSet.setFeatureType(getMethodAndFeatureType(theFeature));

    if(
      theFeature.getGroupId() != null
    ){
      groupFeatureSet.setId(theFeature.getGroupId());
      groupFeatureSet.setName(theFeature.getGroupId());
    }else{
      groupFeatureSet.setId(theFeature.getId());
      groupFeatureSet.setName(theFeature.getId());
    }//end if

    if(theFeature.getScore() != null){
      groupFeatureSet.setScore(Double.parseDouble(theFeature.getScore()));
    }//end if
  }//end initializeGroupFeatureSet

  private void addFeaturePairToGroupFeatureSet(
    FeatureSetI groupFeatureSet,
    DASFeature theFeature
  ){
    SeqFeatureI sf_1 = new SeqFeature();
    SeqFeatureI sf_2 = new SeqFeature();
    FeaturePair featurePair = new FeaturePair(sf_1, sf_2);

    String featureId = theFeature.getId();
    String methodLabelFeatureType;
    String orientation = theFeature.getOrientation();
    String score = theFeature.getScore();
    String start = theFeature.getStart();
    String end = theFeature.getEnd();
    String targetId = theFeature.getTargetId();
    String targetStart = theFeature.getTargetStart();
    String targetStop = theFeature.getTargetStop();

    methodLabelFeatureType = getMethodAndFeatureType(theFeature);

    sf_1.setId (featureId);
    sf_2.setId (featureId);

    sf_1.setFeatureType (methodLabelFeatureType);
    sf_2.setFeatureType (methodLabelFeatureType);

    if(score != null){
      sf_1.setScore(Double.parseDouble(score));
      sf_2.setScore(Double.parseDouble(score));
    }//end if

    if (targetId != null) {
      sf_1.setName (targetId);
      sf_2.setName (targetId);
      sf_2.setStrand (1);
      sf_2.setLow (Integer.parseInt(targetStart));
      sf_2.setHigh (Integer.parseInt(targetStop));
      sf_2.setRefSequence (groupFeatureSet.getHitSequence());
    } else {
      sf_1.setName (featureId);
      sf_2.setName (featureId);
    }//end if

    sf_1.setStrand(getStrandForOrientation(orientation));

    if(start != null){
      sf_1.setLow (Integer.parseInt(start));
    }//end if

    if(end != null){
      sf_1.setHigh (Integer.parseInt(end));
    }//end if

    sf_1.setRefSequence (groupFeatureSet.getHitSequence());

    featurePair = new FeaturePair(sf_1, sf_2);

    if(groupFeatureSet.getId() != null){
      featurePair.setName(groupFeatureSet.getId() + " span " + groupFeatureSet.size());
    }else{
      featurePair.setName(" span " + groupFeatureSet.size());
    }//end if

    groupFeatureSet.addFeature(featurePair);
  }//end addFeaturePairToGroupFeatureSet

  /**
   * <p>
   * Make a call to the chosen DSN, passing in the chosen segment/range into a 
   * das "features" command. Parse the result into a nested heirarchy of FeatureSets.
   * </p>
   * <p>Level 1 - StrandedFeatureSet. Common parent to all lower level sets</p>
   * <p>Level 2 - FeatureSets keyed by Method Name and Feature Type</p>
   * <p>Level 3 - FeatureSets for the parent Method and Type, keyed by GroupId, containing max score within group,
   * and having handles to aligned sequences </p>
   * <p>Level 4 - FeaturePairs for the parent Group, containing start, end, 
   * target start, target end.</p>
  **/
  public StrandedFeatureSetI getAnalysisRegion(CurationSet curation)
  throws ApolloAdapterException
  {
    List allDASFeatures;
    DASFeature theFeature;
    StrandedFeatureSetI parentStrandedFeatureSet
	= new StrandedFeatureSet(new FeatureSet(), new FeatureSet());
    HashMap methodNameFeatureTypes = new HashMap(); //features grouped by methodName and featureType
    HashMap groupsForAMethodNameAndFeatureType; //features grouped by GroupId within method name and feature type
    String methodNameFeatureTypeAndOrientation;
    FeatureSetI methodNameFeatureTypeFeatureSet = null;
    String groupIdAndOrientation;
    FeatureSetI groupFeatureSet = null;
    double numberOfFeatures = 0;
    double featureCount = 0;
    double currentPercentage;
    double percentFeaturesLastDisplayed = 0;
    String groupId;
    boolean initializeAsHolder = false;

    fireProgressEvent(
      new ProgressEvent(
        this,
        new Double(0.0),
        "Getting features from DASServer"
      )
    );

    allDASFeatures = parseFeatureSet();

    numberOfFeatures = allDASFeatures.size();

    fireProgressEvent(
      new ProgressEvent(
        this,
        new Double(50.0),
        "Building browser image"
      )
    );

    parentStrandedFeatureSet =
      new StrandedFeatureSet(
        new FeatureSet(),
        new FeatureSet()
      );

    parentStrandedFeatureSet.setName ("Analyses");

    Iterator features = allDASFeatures.iterator();

    while(features.hasNext()){

      theFeature = (DASFeature)features.next();

      methodNameFeatureTypeAndOrientation =
        getMethodAndFeatureType(theFeature)+
        theFeature.getOrientation();

      //
      //If we haven't encountered this group of features before,
      //create the group as a FeatureSet.
      if(methodNameFeatureTypes.get(methodNameFeatureTypeAndOrientation) == null){

        methodNameFeatureTypeFeatureSet = new FeatureSet();

        initializeMethodNameFeatureTypeFeatureSet(
          methodNameFeatureTypeFeatureSet,
          theFeature
        );

        //
        //If the methodName/feature type is new, then there are no groups
        //allocated to this methodName/featureType - we will just create
        //a new Hashmap for it.
        groupsForAMethodNameAndFeatureType = new HashMap();

        //
        //Add the group to our temporary list of groups. We store BOTH
        //the FeatureSet AND the hashmap of groupid's, by the key
        //methodNameFeatureTypeAndOrientation.
        methodNameFeatureTypes.put(
          methodNameFeatureTypeAndOrientation,
          new Object[]{
            methodNameFeatureTypeFeatureSet,
            groupsForAMethodNameAndFeatureType
          }
        );

        //
        //Add the group to our permanent list of FeatureSets
        parentStrandedFeatureSet.addFeature(methodNameFeatureTypeFeatureSet);

      }else{

        Object[] setAndHashMap = (Object[])methodNameFeatureTypes.get(methodNameFeatureTypeAndOrientation);
        methodNameFeatureTypeFeatureSet = (FeatureSet)setAndHashMap[0];
        groupsForAMethodNameAndFeatureType = (HashMap)setAndHashMap[1];

      }//end if

      //
      //If there is _no_ grouping information on the feature, automatically
      //place the feature in a new group by itself. Otherwise, group it by
      //its group name and orientation.
      groupId = theFeature.getGroupId();

      if(groupId != null && groupId.trim().length() >0){
        groupIdAndOrientation = theFeature.getGroupId()+theFeature.getOrientation();
        initializeAsHolder = false;
      }else{
        groupIdInt++;
        groupIdAndOrientation = theFeature.getId()+" "+groupIdInt;
        initializeAsHolder = true;
      }//end if

      //
      //Now work out if we already have a group with this group id to place
      //the feature into. If not, create a new group.
      if(groupsForAMethodNameAndFeatureType.get(groupIdAndOrientation) == null){

        groupFeatureSet = new FeatureSet();

        initializeGroupFeatureSet(
          groupFeatureSet,
          theFeature,
          initializeAsHolder
        );

        groupsForAMethodNameAndFeatureType.put(groupIdAndOrientation, groupFeatureSet);

        //
        //After you've created the group feature set, add it to the methodname/featuretype
        //featureset.
        methodNameFeatureTypeFeatureSet.addFeature(groupFeatureSet);

      }else{
        groupFeatureSet = (FeatureSet)groupsForAMethodNameAndFeatureType.get(groupIdAndOrientation);
      }//end if

      //In any case, add in the feature pair (the 'real feature' contained in the
      //DASFeature) to the groupFeatureSet we retrieved/created.
      addFeaturePairToGroupFeatureSet(groupFeatureSet, theFeature);
    }//end while

    parentStrandedFeatureSet.setRefSequence(curation.getRefSequence());
    //apollo.dataadapter.debug.DisplayTool.showFeatureSet(parentStrandedFeatureSet);


    return parentStrandedFeatureSet;
  }//getAnalysisRegion

  // returns an empty AnnotatedFeatureSet, because I don't know if GFF files can even
  // contain annotations, and if they can, I don't know how to fetch them
  public FeatureSetI getAnnotatedRegion() throws ApolloAdapterException {
    return new StrandedFeatureSet(new FeatureSet(),
                                  new FeatureSet());
  }

  public SequenceI getSequence(String id) throws ApolloAdapterException {
    throw new NotImplementedException();
  }

  /**
   * <p>The dbxref passed into this argument is only checked to make sure
   * that the the IdValue it contains is the same as the Id for the DASSegment
   * that this adapter is poised on. The range etc passed into the lazy sequence
   * are determined by the Segment that this adapter already has stored on it. </p>
   *
   * <p> Note carefully: the sequence should NOT be fetched from the DASServer and 
   * DASDSN set on this adapter: that Server/DSN may not be a reference server. Instead, 
   * we must to go to the server and DSN referenced by the 'MapMaster' - this
   * points to the URL of the reference server, which will have the raw sequence. </p>
   *
   * 3/2006: Not currently used
  **/
  public SequenceI getSequence(DbXref dbxref) throws ApolloAdapterException {
    logger.error("getSequence(" + dbxref + "): don't yet know how to get sequence");
    return null;

//     if (getSegment() == null)
//       return null;

//     String segmentId = getSegment().getId();
//     DASLazySequence theSequence;
//     String start = getSegment().getStart();
//     String stop = getSegment().getStop();
//     DASDsn theSetDSN = null;
//     DASServerI referenceServer;
//     String mapMaster = null;

//     theSetDSN = 
//       new SimpleDASDsn(
//         getDSN().getSourceId(), 
//         getDSN().getSourceVersion(),
//         getDSN().getSource(),
//         getDSN().getMapMaster(),
//         getDSN().getDescription()
//       );
    
//     mapMaster = theSetDSN.getMapMaster();
    
//     //
//     //we only want the reference data source (for all dsns) - so we have to
//     //cut the actual dsn off the end of the mapmaster url by looking for the
//     //last term separated by a slash.
//     int indexOfLastSlash = mapMaster.lastIndexOf('/');
//     String referenceDataSourceURL = null;
//     String referenceDataSourceDSN;

//     //
//     //If the URL ends with a slash, we have to clip it off first, THEN
//     //clip off the dsn from the mapmaster.
//     if(mapMaster.endsWith("/")){
//       //
//       //clip of the trailing slash.
//       referenceDataSourceURL = mapMaster.substring(0,indexOfLastSlash);
//       //
//       //now the url wont end with a slash, and we can clip off the dsn
//       indexOfLastSlash = referenceDataSourceURL.lastIndexOf('/');
//       referenceDataSourceDSN = referenceDataSourceURL.substring(indexOfLastSlash+1);
//       referenceDataSourceURL = referenceDataSourceURL.substring(0,indexOfLastSlash);
//     }else{
//       referenceDataSourceDSN = mapMaster.substring(indexOfLastSlash+1);
//       referenceDataSourceURL = mapMaster.substring(0,indexOfLastSlash);
//     }//end if

//     //
//     //Re-point the DSN on us at the reference URL
//     referenceServer = new SimpleDASServer(referenceDataSourceURL);
//     theSetDSN.setSourceId(referenceDataSourceDSN);

//     if (dbxref.getIdValue().equals(segmentId)) {

//       theSequence =
//         new DASLazySequence(
//           segmentId,
//           Config.getController(),
//           new apollo.datamodel.Range(
//             segmentId,
//             Integer.valueOf(start).intValue(),
//             Integer.valueOf(stop).intValue()
//           ),
//           referenceServer,
//           theSetDSN,
//           getSegment()
//         );

//       theSequence.getCacher().setMaxSize(1000000);
//       return theSequence;

//     } else {
//       throw new NotImplementedException();
//     }//end if
  }

  public SequenceI getSequence(DbXref dbxref, int start, int end)
  throws ApolloAdapterException {
    throw new NotImplementedException();
  }

  public Vector    getSequences(DbXref[] dbxref)
  throws ApolloAdapterException {
    throw new NotImplementedException();
  }

  public Vector    getSequences(DbXref[] dbxref, int[] start, int[] end)
  throws ApolloAdapterException {
    throw new NotImplementedException();
  }
  public String getRawAnalysisResults(String id)
  throws ApolloAdapterException {
    throw new NotImplementedException();
  }

  /**
   * Direct conversion of a DAS feature orientation +,-,0 into +1, -1, 0.
  **/
  private int getStrandForOrientation(String orientation){
    if(orientation.equals("+")){
      return 1;
    }else if(orientation.equals("-")){
      return -1;
    }else if(orientation.equals("0")){
      return 0;
    }else{
      throw new apollo.dataadapter.NonFatalDataAdapterException("Received orientation: "+orientation+"-- I only accept +,-,0");
    }//end if
  }//end getStrandForOrienation

  /**
   * Concatenates a DAS Feature's method label (if not null) and feature type
   * into a string, separated by a colon.
  **/
  private String getMethodAndFeatureType(DASFeature theFeature){
    if(
      theFeature.getMethodLabel() != null
      &&
      theFeature.getMethodLabel().trim().length() > 0
    ){
      return theFeature.getMethodLabel()+":"+theFeature.getTypeId();
    }else{
      return theFeature.getTypeId();
    }//end if
  }
  
  public void clearStateInformation() {
    stateInformation = new StateInformation();
  }

  public void validateStateInformation(){
    String region = getStateInformation().getProperty(StateInformation.REGION);
    String lowText = getStateInformation().getProperty(StateInformation.SEGMENT_START);
    String highText = getStateInformation().getProperty(StateInformation.SEGMENT_STOP);
    
    if (getDSN() == null)
      throw new apollo.dataadapter.NonFatalDataAdapterException("getDSN returned null");
    if (getSegment() == null)
      throw new apollo.dataadapter.NonFatalDataAdapterException("getSegment returned null.  region = " + region);
    
    if(lowText == null || highText == null){
      throw new NonFatalDataAdapterException("Low/High range must be specified");
    }//end if
  }

  /** Curation set needs top-level info--chromosome, start, end.
   *  FOR NOW, get these from the first and last results. */
  private void setCurationRegion(CurationSet curation) {
    // Chromosome == segment
    if (getSegment() != null)
      curation.setChromosome(getSegment().getId());

    // Set name of region.  If we read data from a file, just use the filename.
    if (getDSN() == null)
      curation.setName(getInput());
    else
	curation.setName(makeRegionName(makeFeatureQuery()));

    StrandedFeatureSetI results = curation.getResults();
    SeqFeatureI firstresult = results.getFeatureAt(0);
    if (firstresult == null) {
      logger.warn("DAS2Adapter.setCurationRegion: no results!");
      return;  // we are hosed
    }
    curation.setLow(firstresult.getLow());
    SeqFeatureI lastresult = results.getFeatureAt(results.getFeatures().size()-1);
    // Add padding to right side or rightmost annotation gets cut off
    int padding = (lastresult.getHigh() - lastresult.getLow())/20;
    curation.setHigh(lastresult.getHigh() + padding);
    logger.debug("Set region to " + curation.getChromosome() + ":" + curation.getLow() + "-" + curation.getHigh() + " + " + padding + " based on extent of first and last results");
    curation.setStrand(1);
  }

  /** Main method for writing DAS2XML file */
  public void commitChanges(CurationSet curation) {
    String filename = apollo.util.IOUtil.findFile(getInput(), true);
    if (filename == null)
      filename = getInput();
    if (filename == null)
      return;

    // If file already exists, ask user to confirm that they want to overwrite
    // This question is configged in style (AskConfirmOverwrite), defaulting to true
    if (Config.getConfirmOverwrite()) {
      File handle = new File(filename);
      if (handle.exists()) {
        if (!LoadUtil.areYouSure(filename + " already exists--overwrite?")) {
          // If user said no, prompt them again to choose a filename
          apollo.main.DataLoader loader = new apollo.main.DataLoader();
          loader.saveFileDialog(curation);
          return;
        }
      }
    }

    setInput(filename);
    String msg = "Saving data to file " + filename;
    fireProgressEvent(new ProgressEvent(this, new Double(10.0), msg));
    if (DAS2Writer.writeXML(curation,
                            filename,
                            "Apollo version: " + apollo.main.Version.getVersion())) {
      logger.info("Saved DAS2XML to " + filename);
      
      //      // Transactions should be associated with any saved file to avoid asynchronization!
      //      ChadoXmlWrite.saveTransactions(curation, filename);
    }
    else {
      String message = "Failed to save DAS2XML to " + filename;
      logger.error(message);
      JOptionPane.showMessageDialog(null,message,"Warning",JOptionPane.WARNING_MESSAGE);
    }
  }


  /** For testing directly from the command line--give a DAS2XML file as the argument. */
  public static void main(String[] args) {
    DAS2FeatureSaxParser featureParser = new DAS2FeatureSaxParser();
    try {
      String test_file_name = args[0];
      File test_file = new File(test_file_name);
      FileInputStream fistr = new FileInputStream(test_file);
      BufferedInputStream bis = new BufferedInputStream(fistr);
      logger.info("Opened input stream to " + test_file_name);

      CurationSet curation = new CurationSet();
      curation.setName(test_file_name);
      featureParser.parse(bis, curation);
      bis.close();

      DAS2Adapter adapter = new DAS2Adapter();
      adapter.setInput(test_file_name);
      adapter.setCurationRegion(curation);
      //      StrandedFeatureSetI results = curation.getResults();
      //      logger.debug("result count: " + ((results == null) ? "(0)" : results.size()+""));
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

}
