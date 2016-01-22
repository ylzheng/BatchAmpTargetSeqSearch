package apollo.dataadapter.otter.parser;
import apollo.datamodel.*;

public class ExonEndTagHandler extends TagHandler{

  public void handleEndElement(
    OtterContentHandler theContentHandler,
    String namespaceURI,
    String localName,
    String qualifiedName
  ){

    Exon exon = (Exon)theContentHandler.getStackObject();
    exon.setHigh(Integer.valueOf(getCharacters()).intValue());

    super.handleEndElement( theContentHandler, namespaceURI, localName, qualifiedName);
  }

  public String getFullName(){
    return "otter:sequence_set:locus:transcript:exon:end";
  }//end getFullName
  
  public String getLeafName() {
    return "end";
  }//end getLeafName
}//end TagHandler
