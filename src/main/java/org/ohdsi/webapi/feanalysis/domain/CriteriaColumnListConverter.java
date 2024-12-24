package org.ohdsi.webapi.feanalysis.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.ohdsi.circe.cohortdefinition.builders.CriteriaColumn;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CriteriaColumnListConverter implements AttributeConverter<List<CriteriaColumn>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<CriteriaColumn> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.stream()
                .map(CriteriaColumn::name) // Convert enum to its string name
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public List<CriteriaColumn> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(dbData.split(DELIMITER))
                .map(CriteriaColumn::valueOf) // Convert string name back to enum
                .collect(Collectors.toList());
    }
}