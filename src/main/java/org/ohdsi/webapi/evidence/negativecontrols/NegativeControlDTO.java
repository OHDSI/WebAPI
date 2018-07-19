package org.ohdsi.webapi.evidence.negativecontrols;

import java.io.Serializable;

/**
 *
 * @author asena5
 */
public class NegativeControlDTO implements Serializable {

    public int conceptSetId;
    public int sourceId;
    public String conceptSetName;
    public int negativeControl;
    public int conceptId;
    public String conceptName;
    public String domainId;
    public Long sortOrder;
    public Long descendantPmidCount;
    public Long exactPmidCount ;
    public Long parentPmidCount ;
    public Long ancestorPmidCount;
    public int indCi;
    public int tooBroad;
    public int drugInduced;
    public int pregnancy;
    public Long descendantSplicerCount;
    public Long exactSplicerCount;
    public Long parentSplicerCount;
    public Long ancestorSplicerCount;
    public Long descendantFaersCount;
    public Long exactFaersCount;
    public Long parentFaersCount;
    public Long ancestorFaersCount;
    public int userExcluded;
    public int userIncluded;
    public int optimizedOut;
    public int notPrevalent;
}
