package apollo.dataadapter.synteny;

import apollo.datamodel.CurationSet;
import apollo.datamodel.LinkSet;
import apollo.datamodel.SeqFeatureI;
import apollo.datamodel.SpeciesComparison;
import apollo.dataadapter.ApolloDataAdapterI;
import apollo.dataadapter.ApolloAdapterException;

public interface LinkerI { 
  public LinkSet createLinksFromSpeciesComp(SpeciesComparison twoSpecies);
  public CurationSet getCurationSetForLink(ApolloDataAdapterI adap,SeqFeatureI link)
    throws ApolloAdapterException;  
}
