package apollo.gui.detailviewers.sequencealigner.filters;

public interface Filter<T> {

  public abstract boolean keep(T f);
  
  public abstract String valueToString();
}
