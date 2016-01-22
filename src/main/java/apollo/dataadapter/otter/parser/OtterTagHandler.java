package apollo.dataadapter.otter.parser;

/**
 * General do-nothing tag for the root of the document
**/
public class OtterTagHandler extends TagHandler{
  public String getFullName(){
    return "otter";
  }//end getTag

  public String getLeafName(){
    return "otter";
  }//end getTag
}//end TagHandler
