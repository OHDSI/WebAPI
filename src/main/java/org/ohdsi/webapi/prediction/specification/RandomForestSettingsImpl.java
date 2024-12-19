package org.ohdsi.webapi.prediction.specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import org.ohdsi.analysis.prediction.design.RandomForestSettings;

/**
 *
 * @author asena5
 */
public class RandomForestSettingsImpl extends SeedSettingsImpl implements RandomForestSettings {

    private List<Integer> mtries = new ArrayList<>(Arrays.asList(-1));
    private List<Integer> ntrees = new ArrayList<>(Arrays.asList(500));
    private List<Integer> maxDepth = null;
    private List<Boolean> varImp = new ArrayList<>(Arrays.asList(true));

    /**
     * The number of features to include in each tree (-1 defaults to square
     * root of total features)
     *
     * @return mtries
   *
     */
    @Override
    public List<Integer> getMtries() {
        return mtries;
    }

    /**
     *
     * @param mtries
     */
    public void setMtries(Object mtries) {
        if (mtries != null) {
            if (mtries instanceof ArrayList) {
                this.mtries = (ArrayList<Integer>) mtries;
            } else if (mtries instanceof Integer integer) {
                this.mtries = new ArrayList<>(Arrays.asList(integer));
            } else {
                throw new InputMismatchException("Expected ArrayList<Integer> or Integer");
            }
        } else {
            this.mtries = null;
        }        
    }

    /**
     * The number of trees to build
     *
     * @return ntrees
   *
     */
    @Override
    public List<Integer> getNtrees() {
        return ntrees;
    }

    /**
     *
     * @param ntrees
     */
    public void setNtrees(Object ntrees) {
        if (ntrees != null) {
            if (ntrees instanceof ArrayList) {
                this.ntrees = (ArrayList<Integer>) ntrees;
            } else if (ntrees instanceof Integer integer) {
                this.ntrees = new ArrayList<>(Arrays.asList(integer));
            } else {
                throw new InputMismatchException("Expected ArrayList<Integer> or Integer");
            }
        } else {
            this.ntrees = null;
        }        
    }

    /**
     * Maximum number of interactions - a large value will lead to slow model
     * training
     *
     * @return maxDepth
   *
     */
    @Override
    public List<Integer> getMaxDepth() {
        return maxDepth;
    }

    /**
     *
     * @param maxDepth
     */
    public void setMaxDepth(List<Object> maxDepth) {
        if (maxDepth != null) {
            if (this.maxDepth == null)
                this.maxDepth = new ArrayList<>();
            
            maxDepth.forEach((o) -> {
                if (o instanceof BigDecimal decimal) {
                    this.maxDepth.add(decimal.intValue());
                } else if (o instanceof Integer integer) {
                    this.maxDepth.add(integer);
                } else {
                    throw new InputMismatchException("Expected ArrayList<Integer> or ArrayList<BigDecimal>");
                }
            });
        } else {
            this.maxDepth = null;
        }
    }

    /**
     * Perform an initial variable selection prior to fitting the model to
     * select the useful variables
     *
     * @return varImp
   *
     */
    @Override
    public List<Boolean> getVarImp() {
        return varImp;
    }

    /**
     *
     * @param varImp
     */
    public void setVarImp(Object varImp) {
        if (varImp != null) {
            if (varImp instanceof ArrayList) {
                this.varImp = (ArrayList<Boolean>) varImp;
            } else if (varImp instanceof Boolean boolean1) {
                this.varImp = new ArrayList<>(Arrays.asList(boolean1));
            } else {
                throw new InputMismatchException("Expected ArrayList<Boolean> or Boolean");
            }
        } else {
            this.varImp = null;
        }        
    }
}
