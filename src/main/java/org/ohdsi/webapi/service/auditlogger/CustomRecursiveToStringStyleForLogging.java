package org.ohdsi.webapi.service.auditlogger;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;

public class CustomRecursiveToStringStyleForLogging extends RecursiveToStringStyle {

    public CustomRecursiveToStringStyleForLogging() {
        super();
        super.setUseClassName(false);
        super.setUseIdentityHashCode(false);
        super.setContentStart("{");
        super.setContentEnd("}");
        super.setArrayStart("[");
        super.setArrayEnd("]");
        super.setFieldSeparator(",");
        super.setFieldNameValueSeparator(":");
        super.setNullText("null");
        super.setSummaryObjectStartText("\"<");
        super.setSummaryObjectEndText(">\"");
        super.setSizeStartText("\"<size=");
        super.setSizeEndText(">\"");
    }
}
