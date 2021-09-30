package org.ohdsi.webapi.prediction.specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import org.ohdsi.analysis.prediction.design.MLPSettings;

/**
 *
 * @author asena5
 */
public class MLPSettingsImpl extends SeedSettingsImpl implements MLPSettings {

    private List<Integer> size = new ArrayList<>(Arrays.asList(4));
    private List<BigDecimal> alpha = new ArrayList<>(Arrays.asList(BigDecimal.valueOf(0.000010f)));

    /**
     * The number of hidden nodes
     *
     * @return size
   *
     */
    @Override
    public List<Integer> getSize() {
        return size;
    }

    /**
     *
     * @param size
     */
    public void setSize(Object size) {
        if (size != null) {
            if (size instanceof ArrayList) {
                this.size = (ArrayList<Integer>) size;
            } else if (size instanceof Integer) {
                this.size = new ArrayList<>(Arrays.asList((Integer) size));
            } else {
                throw new InputMismatchException("Expected ArrayList<Integer> or Integer");
            }
        } else {
            this.size = null;
        }        
    }

    /**
     * The L2 regularisation
     *
     * @return alpha
   *
     */
    @Override
    public List<BigDecimal> getAlpha() {
        return alpha;
    }

    /**
     *
     * @param alpha
     */
    public void setAlpha(Object alpha) {
        if (alpha != null) {
            if (alpha instanceof ArrayList) {
                this.alpha = (ArrayList<BigDecimal>) alpha;
            } else if (alpha instanceof Float) {
                this.alpha = new ArrayList<>(Arrays.asList(new BigDecimal((Float) alpha)));
            } else {
                throw new InputMismatchException("Expected ArrayList<BigDecimal> or Float");
            }
        } else {
            this.alpha = null;
        }        
    }
}
