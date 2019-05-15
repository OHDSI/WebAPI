package org.ohdsi.webapi.cyclops.specification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import org.ohdsi.analysis.cyclops.design.*;
import org.ohdsi.webapi.RLangClassImpl;

/**
 *
 * @author asena5
 */
public class PriorImpl extends RLangClassImpl implements Prior {

    private PriorTypeEnum priorType = PriorTypeEnum.LAPLACE;
    private List<BigDecimal> variance = null;
    private List<Integer> exclude = null;
    private String graph = null;
    private List<String> neighborhood = null;
    private Boolean useCrossValidation = false;
    private Boolean forceIntercept = false;
    private String priorAttrClass = "cyclopsPrior";

    /**
     * Specifies prior distribution. We specify all priors in terms of their
     * variance parameters. Similar fitting tools for regularized regression
     * often parameterize the Laplace distribution in terms of a rate
     * \&quot;lambda\&quot; per observation. See \&quot;glmnet\&quot;, for
     * example. variance &#x3D; 2 * / (nobs * lambda)^2 or lambda &#x3D; sqrt(2
     * / variance) / nobs
     *
     * @return priorType
     *
     */
    @Override
    public PriorTypeEnum getPriorType() {
        return priorType;
    }

    /**
     *
     * @param priorType
     */
    @JsonIgnore
    public void setPriorType(PriorTypeEnum priorType) {
        this.priorType = priorType;
    }

    /**
     * prior distribution variance
     *
     * @return variance
     *
     */
    @Override
    public List<BigDecimal> getVariance() {
        return variance;
    }

    /**
     *
     * @param variance
     */
    public void setVariance(Object variance) {
        if (variance != null) {
            if (variance instanceof ArrayList) {
                this.variance = (ArrayList<BigDecimal>) variance;
            } else if (variance instanceof Integer) {
                if (this.variance == null) {
                    this.variance = new ArrayList<>();
                }
                this.variance.add(new BigDecimal((Integer) variance));
            } else {
                throw new InputMismatchException("Expected ArrayList<BigDecimal> or Integer");
            }
        } else {
            this.variance = null;
        }
    }

    /**
     * A vector of numbers or covariateId names to exclude from prior
     *
     * @return exclude
     *
     */
    @Override
    public List<Integer> getExclude() {
        return exclude;
    }

    /**
     *
     * @param exclude
     */
    public void setExclude(Object exclude) {
        if (exclude != null) {
            if (exclude instanceof ArrayList) {
                this.exclude = (ArrayList<Integer>) exclude;
            } else if (exclude instanceof Integer) {
                this.exclude = new ArrayList<>(Arrays.asList((Integer) exclude));
            } else {
                throw new InputMismatchException("Expected ArrayList<String> or Integer");
            }
        } else {
            this.exclude = null;
        }
    }

    /**
     * Child-to-parent mapping for a hierarchical prior
     *
     * @return graph
     *
     */
    @Override
    public String getGraph() {
        return graph;
    }

    /**
     *
     * @param graph
     */
    public void setGraph(String graph) {
        this.graph = graph;
    }

    /**
     * A list of first-order neighborhoods for a partially fused prior
     *
     * @return neighborhood
     *
     */
    @Override
    public List<String> getNeighborhood() {
        return neighborhood;
    }

    /**
     *
     * @param neighborhood
     */
    public void setNeighborhood(Object neighborhood) {
        if (neighborhood != null) {
            if (neighborhood instanceof ArrayList) {
                this.neighborhood = (ArrayList<String>) neighborhood;
            } else if (neighborhood instanceof String) {
                this.neighborhood = new ArrayList<>(Arrays.asList((String) neighborhood));
            } else {
                throw new InputMismatchException("Expected ArrayList<String> or String");
            }
        } else {
            this.neighborhood = null;
        }
    }

    /**
     * Perform cross-validation to determine prior variance.
     *
     * @return useCrossValidation
     *
     */
    @Override
    public Boolean getUseCrossValidation() {
        return useCrossValidation;
    }

    /**
     *
     * @param useCrossValidation
     */
    public void setUseCrossValidation(Boolean useCrossValidation) {
        this.useCrossValidation = useCrossValidation;
    }

    /**
     * Force intercept coefficient into prior
     *
     * @return forceIntercept
     *
     */
    @Override
    public Boolean getForceIntercept() {
        return forceIntercept;
    }

    /**
     *
     * @param forceIntercept
     */
    public void setForceIntercept(Boolean forceIntercept) {
        this.forceIntercept = forceIntercept;
    }

    /**
     * Get priorAttrClass
     *
     * @return priorAttrClass
     *
     */
    @Override
    public String getAttrClass() {
        return priorAttrClass;
    }

    /**
     *
     * @param attrClass
     */
    @Override
    public void setAttrClass(String attrClass) {
        this.priorAttrClass = attrClass;
    }
}
