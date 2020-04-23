package org.ohdsi.webapi.check;

import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.NumericRange;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class Comparisons {
    public Comparisons() {
    }

    public static Boolean isDateValid(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }

    public static Boolean startIsGreaterThanEnd(NumericRange r) {
        return Objects.nonNull(r.value) && Objects.nonNull(r.extent)
                && r.value.intValue() > r.extent.intValue();
    }

    public static Boolean startIsGreaterThanEnd(DateRange r) {
        try {
            return Objects.nonNull(r.value) && Objects.nonNull(r.extent)
                    && LocalDate.parse(r.value).isAfter(LocalDate.parse(r.extent));
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }

    public static Boolean isStartNegative(NumericRange r) {
        return Objects.nonNull(r.value) && r.value.intValue() < 0;
    }
}
