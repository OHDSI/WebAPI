package org.ohdsi.webapi.util;

import org.apache.commons.csv.CSVRecord;

@FunctionalInterface
public interface CSVRecordMapper<T> {
  T mapRecord(CSVRecord record);
}
