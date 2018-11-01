package org.ohdsi.webapi.estimation.specification;

import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.TrimByPsToEquipoiseArgs;
import org.ohdsi.webapi.RLangClassImpl;
import java.util.ArrayList;
import java.util.List;

public class TrimByPsToEquipoiseArgsImpl extends RLangClassImpl implements TrimByPsToEquipoiseArgs {
  private List<Float> bounds = null;

  public TrimByPsToEquipoiseArgsImpl addBoundsItem(Float boundsItem) {
    if (this.bounds == null) {
      this.bounds = new ArrayList<>();
    }
    this.bounds.add(boundsItem);
    return this;
  }

  /**
   * The upper and lower bound on the preference score for keeping persons 
   * @return bounds
   **/
  @Override
  public List<Float> getBounds() {
    return bounds;
  }

  public void setBounds(List<Float> bounds) {
    this.bounds = bounds;
  }
}
