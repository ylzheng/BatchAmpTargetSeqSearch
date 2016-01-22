package apollo.dataadapter.otter.parser;
import apollo.datamodel.*;

public class TranscriptNameTagHandler extends TagHandler{
  public void handleEndElement(
    OtterContentHandler theContentHandler,
    String namespaceURI,
    String localName,
    String qualifiedName
  ){
    Transcript transcript = (Transcript)theContentHandler.getStackObject();
    String characters = getCharacters();
    transcript.setDescription(characters);
    transcript.setId(characters);
    transcript.setName(characters);

    super.handleEndElement( theContentHandler, namespaceURI, localName, qualifiedName);
  }

  public String getFullName(){
    return "otter:sequence_set:locus:transcript:name";
  }//end getFullName
  
  public String getLeafName() {
    return "name";
  }//end getLeafName
}//end TagHandler
