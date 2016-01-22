package apollo.datamodel.template;

/**
 * Created by pxuan on 12/19/15.
 */
public class PrimerBlastResult {
  Integer fTemplateStart;
  Integer fTemplateStop;
  Integer rTemplateStart;
  Integer rTemplateStop;
  String genomeID;

  public Integer getfTemplateStart() {
    return fTemplateStart;
  }

  public Integer getfTemplateStop() {
    return fTemplateStop;
  }

  public Integer getrTemplateStart() {
    return rTemplateStart;
  }

  public Integer getrTemplateStop() {
    return rTemplateStop;
  }

  public String getGenomeID() {return genomeID;}

  public PrimerBlastResult() {

  }

  public PrimerBlastResult(Integer fTemplateStart, Integer fTemplateStop, Integer rTemplateStart, Integer rTemplateStop,
                           String genomeID) {
    this.fTemplateStart = fTemplateStart;
    this.fTemplateStop = fTemplateStop;
    this.rTemplateStart = rTemplateStart;
    this.rTemplateStop = rTemplateStop;
    this.genomeID = genomeID;
  }

  public void setfTemplateStart(Integer fTemplateStart) {
    this.fTemplateStart = fTemplateStart;
  }

  public void setfTemplateStop(Integer fTemplateStop) {
    this.fTemplateStop = fTemplateStop;
  }

  public void setrTemplateStart(Integer rTemplateStart) {
    this.rTemplateStart = rTemplateStart;
  }

  public void setrTemplateStop(Integer rTemplateStop) {
    this.rTemplateStop = rTemplateStop;
  }

  public void setGenomeID(String genomeID) {this.genomeID = genomeID;}
}
