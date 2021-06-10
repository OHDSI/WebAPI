package org.ohdsi.webapi.sqlrender;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 *
 * @author Lee Evans
 */
public class TranslatedStatement {
    
    @JsonProperty("targetSQL")
    private String targetSQL;

    public String getTargetSQL() {

        return targetSQL;
    }

    public void setTargetSQL(String targetSQL) {

        this.targetSQL = targetSQL;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslatedStatement that = (TranslatedStatement) o;
        return Objects.equals(targetSQL, that.targetSQL);
    }

    @Override
    public int hashCode() {

        return Objects.hash(targetSQL);
    }
}
