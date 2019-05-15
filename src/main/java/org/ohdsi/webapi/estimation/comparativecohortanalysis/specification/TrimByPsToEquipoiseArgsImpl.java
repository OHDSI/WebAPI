package org.ohdsi.webapi.estimation.comparativecohortanalysis.specification;

import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.TrimByPsToEquipoiseArgs;
import org.ohdsi.webapi.RLangClassImpl;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author asena5
 */
public class TrimByPsToEquipoiseArgsImpl extends RLangClassImpl implements TrimByPsToEquipoiseArgs {
  private List<Float> bounds = null;

    /**
     *
     * @param boundsItem
     * @return
     */
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

    /**
     *
     * @param bounds
     */
    public void setBounds(List<Float> bounds) {
    this.bounds = bounds;
  }
}
