package apollo.dataadapter.otter.parser;
import apollo.datamodel.*;

public class ExonStartTagHandler extends TagHandler{
  public void handleEndElement(
    OtterContentHandler theContentHandler,
    String namespaceURI,
    String localName,
    String qualifiedName
  ){
    Exon exon = (Exon)theContentHandler.getStackObject();
    exon.setLow(Integer.valueOf(getCharacters()).intValue());

    super.handleEndElement( theContentHandler, namespaceURI, localName, qualifiedName);
  }

  public String getFullName(){
    return "otter:sequence_set:locus:transcript:exon:start";
  }//end getFullName
  
  public String getLeafName() {
    return "start";
  }//end getLeafName
}//end TagHandler
