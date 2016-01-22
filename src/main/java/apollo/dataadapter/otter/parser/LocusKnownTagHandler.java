package apollo.dataadapter.otter.parser;
import apollo.datamodel.*;

public class LocusKnownTagHandler extends TagHandler{
  public void handleEndElement(
    OtterContentHandler theContentHandler,
    String namespaceURI,
    String localName,
    String qualifiedName
  ){
    AnnotatedFeatureI gene 
      = (AnnotatedFeatureI)theContentHandler.getStackObject();
    gene.addProperty(TagHandler.KNOWN,getCharacters());

    super.handleEndElement(theContentHandler, 
                           namespaceURI,
                           localName, 
                           qualifiedName);
  }

  public String getFullName(){
    return "otter:sequence_set:locus:known";
  }
  
  public String getLeafName() {
    return "known";
  }
}//end TagHandler
