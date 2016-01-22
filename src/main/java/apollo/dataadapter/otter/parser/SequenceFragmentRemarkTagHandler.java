package apollo.dataadapter.otter.parser;
import apollo.datamodel.*;

public class SequenceFragmentRemarkTagHandler extends TagHandler{
  public void handleEndElement(
    OtterContentHandler theContentHandler,
    String namespaceURI,
    String localName,
    String qualifiedName
  ){
    AssemblyFeature assFeat = (AssemblyFeature)theContentHandler.getStackObject();
    assFeat.addRemark(getCharacters());
    super.handleEndElement( theContentHandler, namespaceURI, localName, qualifiedName);
  }

  public String getFullName(){
    return "otter:sequence_set:sequence_fragment:remark";
  }//end getFullName
  
  public String getLeafName() {
    return "remark";
  }//end getLeafName
}//end TagHandler
