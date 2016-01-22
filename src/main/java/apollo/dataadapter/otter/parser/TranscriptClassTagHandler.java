package apollo.dataadapter.otter.parser;
import apollo.datamodel.*;

public class TranscriptClassTagHandler extends TagHandler{
  public void handleEndElement(
    OtterContentHandler theContentHandler,
    String namespaceURI,
    String localName,
    String qualifiedName
  ){
    Transcript transcript = (Transcript)theContentHandler.getStackObject();
    transcript.setTopLevelType(getCharacters());
    super.handleEndElement( theContentHandler, namespaceURI, localName, qualifiedName);
  }

  public String getFullName(){
    return "otter:sequence_set:locus:transcript:transcript_class";
  }//end getFullName
  
  public String getLeafName() {
    return "transcript_class";
  }//end getLeafName
}//end TagHandler
