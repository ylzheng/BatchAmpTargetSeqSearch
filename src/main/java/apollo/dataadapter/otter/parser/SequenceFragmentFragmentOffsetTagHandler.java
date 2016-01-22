package apollo.dataadapter.otter.parser;
import apollo.datamodel.*;

/**
 * I add on the assembly start of the current sequence fragment.
**/
public class SequenceFragmentFragmentOffsetTagHandler extends TagHandler{

  public void handleEndElement(
    OtterContentHandler theContentHandler,
    String namespaceURI,
    String localName,
    String qualifiedName
  ){
    AssemblyFeature annotation = (AssemblyFeature)theContentHandler.getStackObject();
    annotation.setFragmentOffset(Integer.valueOf(getCharacters()).intValue());

    super.handleEndElement( theContentHandler, namespaceURI, localName, qualifiedName);
  }

  public String getFullName(){
    return "otter:sequence_set:sequence_fragment:fragment_offset";
  }//end getTag
  
  public String getLeafName(){
    return "fragment_offset";
  }//end getTag
}//end TagHandler
