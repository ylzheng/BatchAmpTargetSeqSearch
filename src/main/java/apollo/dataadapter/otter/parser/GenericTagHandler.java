package apollo.dataadapter.otter.parser;

/**
 * SequenceSets contain everything with a marker for an author
 * - we map them to apollo GenericAnnotationSets
**/
public class GenericTagHandler extends TagHandler{
  String name;
  
  public GenericTagHandler(String newValue){
    name = newValue;
  }//end GenericTagHandler


  public String getFullName(){
    return name;
  }//end getFullName

  public String getLeafName(){
    return name;
  }//end getLeafName
}//end TagHandler
