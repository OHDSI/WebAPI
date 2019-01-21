package org.ohdsi.webapi.prediction.specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import org.ohdsi.analysis.prediction.design.GradientBoostingMachineSettings;

/**
 *
 * @author asena5
 */
public class GradientBoostingMachineSettingsImpl extends SeedSettingsImpl implements GradientBoostingMachineSettings {

    private List<Integer> nTrees = null;
    private Integer nthread = 20;
    private List<Integer> maxDepth = null;
    private List<Integer> minRows = new ArrayList<>(Arrays.asList(20));
    private List<BigDecimal> learnRate = null;

    /**
     * The number of trees to build
     *
     * @return nTrees
   *
     */
    @Override
    public List<Integer> getNTrees() {
        return nTrees;
    }

    /**
     *
     * @param nTrees
     */
    public void setNTrees(List<Object> nTrees) {
        if (nTrees != null) {
            if (this.nTrees == null)
                this.nTrees = new ArrayList<>();
            
            nTrees.forEach((o) -> {
                if (o instanceof BigDecimal) {
                    this.nTrees.add(((BigDecimal) o).intValue());
                } else if (o instanceof Integer) {
                    this.nTrees.add((Integer) o);
                } else {
                    throw new InputMismatchException("Expected ArrayList<Integer> or ArrayList<BigDecimal>");
                }
            });
        } else {
            this.nTrees = null;
        }
    }

    /**
     * The number of computer threads to (how many cores do you have?)
     *
     * @return nthread
   *
     */
    @Override
    public Integer getNThread() {
        return nthread;
    }

    /**
     *
     * @param nthread
     */
    public void setNthread(Integer nthread) {
        this.nthread = nthread;
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
    public void setMaxDepth(Object maxDepth) {
        if (maxDepth != null) {
            if (maxDepth instanceof ArrayList) {
                this.maxDepth = (ArrayList<Integer>) maxDepth;
            } else if (maxDepth instanceof Integer) {
                this.maxDepth = new ArrayList<>(Arrays.asList((Integer) maxDepth));
            } else {
                throw new InputMismatchException("Expected ArrayList<Integer> or Integer");
            }
        } else {
            this.maxDepth = null;
        }
    }

    /**
     * The minimum number of rows required at each end node of the tree
     *
     * @return minRows
   *
     */
    @Override
    public List<Integer> getMinRows() {
        return minRows;
    }

    /**
     *
     * @param minRows
     */
    public void setMinRows(Object minRows) {
        if (minRows != null) {
            if (minRows instanceof ArrayList) {
                this.minRows = (ArrayList<Integer>) minRows;
            } else if (minRows instanceof Integer) {
                this.minRows = new ArrayList<>(Arrays.asList((Integer) minRows));
            } else {
                throw new InputMismatchException("Expected ArrayList<Integer> or Integer");
            }
        } else {
            this.minRows = null;
        }
    }

    /**
     * The boosting learn rate
     *
     * @return learnRate
   *
     */
    @Override
    public List<BigDecimal> getLearnRate() {
        return learnRate;
    }

    /**
     *
     * @param learnRate
     */
    public void setLearnRate(List<BigDecimal> learnRate) {
        this.learnRate = learnRate;
    }
}
