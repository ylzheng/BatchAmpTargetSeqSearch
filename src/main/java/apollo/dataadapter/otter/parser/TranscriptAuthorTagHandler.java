package apollo.dataadapter.otter.parser;
import apollo.datamodel.*;

public class TranscriptAuthorTagHandler extends TagHandler{

  public void handleEndElement(
    OtterContentHandler theContentHandler,
    String namespaceURI,
    String localName,
    String qualifiedName
  ){
    Transcript trans = (Transcript)theContentHandler.getStackObject();
    trans.addProperty(TagHandler.AUTHOR, getCharacters());
    super.handleEndElement( theContentHandler, namespaceURI, localName, qualifiedName);
  }

  public String getFullName(){
    return "otter:sequence_set:locus:transcript:author";
  }
  
  public String getLeafName() {
    return "author";
  }
}//end TagHandler
