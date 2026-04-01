package org.ohdsi.webapi.feanalysis.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.ohdsi.circe.cohortdefinition.builders.CriteriaColumn;

@Converter(autoApply = false)
public class CriteriaColumnListConverter implements AttributeConverter<List<CriteriaColumn>, String> {

    @Override
    public String convertToDatabaseColumn(List<CriteriaColumn> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null; // match old behavior (likely null, not empty string)
        }

        return attribute.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @Override
    public List<CriteriaColumn> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .map(CriteriaColumn::valueOf)
                .collect(Collectors.toList());
    }
}