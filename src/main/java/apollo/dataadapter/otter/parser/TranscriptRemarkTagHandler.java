package apollo.dataadapter.otter.parser;
import apollo.datamodel.*;

public class TranscriptRemarkTagHandler extends TagHandler{
  public void handleEndElement(
    OtterContentHandler theContentHandler,
    String namespaceURI,
    String localName,
    String qualifiedName
  ){
    Transcript transcript = (Transcript)theContentHandler.getStackObject();
    Comment comment = 
      new Comment(
        transcript.getId(),
        getCharacters(),
        "no author",
        0 //should be a long representing a timestamp.
      );
    transcript.addComment(comment);

    super.handleEndElement( theContentHandler, namespaceURI, localName, qualifiedName);
  }

  public String getFullName(){
    return "otter:sequence_set:locus:transcript:remark";
  }//end getFullName
  
  public String getLeafName() {
    return "remark";
  }//end getLeafName
}//end TagHandler
