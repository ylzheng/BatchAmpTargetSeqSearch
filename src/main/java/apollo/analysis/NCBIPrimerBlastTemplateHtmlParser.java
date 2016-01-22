package apollo.analysis;

import apollo.datamodel.template.PrimerBlastResult;

import java.io.*;

public class NCBIPrimerBlastTemplateHtmlParser {

  private static final String TYPE = "primer_blast";

  public class PrimerBlastHtmlParserException extends Exception
  {
    private static final long serialVersionUID = 1L;

    public PrimerBlastHtmlParserException(String msg)
    {
      super(msg);
    }
  }

  public NCBIPrimerBlastTemplateHtmlParser() throws IOException
  {
  }
  
  public String parse(InputStream is, PrimerBlastResult result, String outputRetrivedInforFile) throws IOException, PrimerBlastHtmlParserException
  {
    FileWriter outputStream = null;
    try{
      outputStream = new FileWriter(new File(outputRetrivedInforFile));
    } catch (IOException e) {
      e.printStackTrace();
    }
    assert outputStream != null;

    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line;
    while ((line = br.readLine()) != null) {
      //System.out.println("line: " + line);
      outputStream.write(line + "\n");
      // check for errors
      if (line.contains("class=\"error\"")) {
        //String[] tokens = line.split("<|>");
        /**In order to  pass the */
        System.out.println("this pair of trouble primers! ");
        result.setfTemplateStart(Integer.parseInt("-1"));
        result.setfTemplateStop(Integer.parseInt("-1"));
        result.setrTemplateStart(Integer.parseInt("-1"));
        result.setrTemplateStop(Integer.parseInt("-2"));
        break;
        //throw new PrimerBlastHtmlParserException(tokens[6].replaceAll("&quot;", "'"));
      }

      if (line.contains("No target templates")) {
        System.out.println("this pair of trouble primers can not find target templates! ");
        result.setfTemplateStart(Integer.parseInt("-1"));
        result.setfTemplateStop(Integer.parseInt("-1"));
        result.setrTemplateStart(Integer.parseInt("-1"));
        result.setrTemplateStop(Integer.parseInt("-2"));
        break;
      }

      /** find out the genome ID in NCBI */
      if (line.contains("target = \"new_entrez\"") && result.getGenomeID() == null) {
        System.out.println("To get ID");
        String[] tokens = line.split("\\s+");
        /*int idStart = tokens[1].indexOf("id=") + 3;
        int idEnd = tokens[1].length() - 1;
        String tmp = tokens[1].substring(idStart, idEnd);*/
        String tokenwithGenomeID = findTokenWithGenomeID(tokens);
        String genomeID = tokenwithGenomeID.substring(13,tokenwithGenomeID.length()-4);
        result.setGenomeID(genomeID);
      }

      // parse forward data
      if (line.contains("Forward primer  1")) {
        line = br.readLine();
        outputStream.write(line + "\n");
        if (line.contains("Template")) {
          String[] tokens = line.split("\\s+");
          result.setfTemplateStart(Integer.parseInt(tokens[1]));
          result.setfTemplateStop(Integer.parseInt(tokens[3]));
        } else {
          //throw new IOException("Can not reteive forward template data!");
          System.out.println("Can not reteive forward template data!");
        }
      } else {
        // did not reach the first record and skip to the next line
        continue;
      }

      // parse reverse data
      line = br.readLine();
      outputStream.write(line + "\n");
      line = br.readLine();
      outputStream.write(line + "\n");
      if (line.contains("Reverse primer  1")) {
        line = br.readLine();
        outputStream.write(line + "\n");
        if (line.contains("Template")) {
          String[] tokens = line.split("\\s+");
          result.setrTemplateStart(Integer.parseInt(tokens[1]));
          result.setrTemplateStop(Integer.parseInt(tokens[3]));
          break; // get first primer pair and we can exit parsing.
        } else {
          System.out.println("Can not reteive reverse template data!");
          //throw new IOException("Can not reteive reverse template data!");
        }
      }

      if (result.getfTemplateStart() == null){
        /**In order to  pass the*/
        System.out.println("this pair of trouble primers! ");
        result.setfTemplateStart(Integer.parseInt("-1"));
        result.setfTemplateStop(Integer.parseInt("-1"));
        result.setrTemplateStart(Integer.parseInt("-1"));
        result.setrTemplateStop(Integer.parseInt("-2"));
        break;
      }
    }
    outputStream.close();
    return TYPE;
  }

  public String findTokenWithGenomeID (String[] tokens){
    for(String eachElement : tokens){
      if (eachElement.contains("new_entrez")){
        return eachElement;
      }
    }
    return "noIDFound";
  }
}
