package org.ohdsi.webapi.cohortsample.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.ws.rs.BadRequestException;
import java.util.stream.Stream;

public class SampleParametersDTO {
    private static final int SIZE_MAX = 500;
    private static final int AGE_MAX = 500;

    private int size;
    private String name;
    private GenderDTO gender;

    private AgeDTO age;

    public void validate() {
        if (name == null) {
            throw new BadRequestException("Sample must have a name");
        }
        if (size <= 0) {
            throw new BadRequestException("sample parameter size must fall in the range (1, " + SIZE_MAX + ")");
        }
        if (size > SIZE_MAX) {
            throw new BadRequestException("sample parameter size must fall in the range (1, " + SIZE_MAX + ")");
        }
        if (age != null) {
            if (!age.validate()) {
                age = null;
            }
        }
    }

    public AgeDTO getAge() {
        return age;
    }

    public void setAge(AgeDTO age) {
        this.age = age;
    }

    public GenderDTO getGender() {
        return gender;
    }

    public void setGender(GenderDTO gender) {
        this.gender = gender;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum AgeMode {
        LESS_THAN("lessThan"),
        LESS_THAN_OR_EQUAL("lessThanOrEqual"),
        GREATER_THAN("greaterThan"),
        GREATER_THAN_OR_EQUAL("greaterThanOrEqual"),
        EQUAL_TO("equalTo"),
        BETWEEN("between"),
        NOT_BETWEEN("notBetween");

        private final String serialName;

        AgeMode(String serialName) {
            this.serialName = serialName;
        }

        @JsonValue
        public String getSerialName() {
            return serialName;
        }

        public static AgeMode fromSerialName(String name) {
            return Stream.of(SampleParametersDTO.AgeMode.values())
                    .filter(mode -> mode.getSerialName().equals(name))
                    .findFirst()
                    .orElse(null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GenderDTO {
        private Integer conceptId;

        public Integer getConceptId() {
            return conceptId;
        }

        public void setConceptId(Integer conceptId) {
            this.conceptId = conceptId;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AgeDTO {
        private Integer min;
        private Integer max;
        private Integer value;
        private AgeMode mode;

        public boolean validate() {
            if (mode == null) {
                if (min != null || max != null || value != null) {
                    throw new BadRequestException("Cannot specify age without a mode to use age with.");
                } else {
                    return false;
                }
            }
            switch (mode) {
                case LESS_THAN:
                case LESS_THAN_OR_EQUAL:
                case GREATER_THAN:
                case GREATER_THAN_OR_EQUAL:
                case EQUAL_TO:
                    if (value == null) {
                        throw new BadRequestException("Cannot use single age comparison mode " + mode + " without age property.");
                    }
                    if (min != null || max != null) {
                        throw new BadRequestException("Cannot use age range property with comparison mode " + mode + ".");
                    }
                    break;
                case BETWEEN:
                case NOT_BETWEEN:
                    if (min == null || max == null) {
                        throw new BadRequestException("Cannot use age range comparison mode " + mode + " without ageMin and ageMax properties.");
                    }
                    if (value != null) {
                        throw new BadRequestException("Cannot use single age property with comparison mode " + mode + ".");
                    }
                    if (min < 0) {
                        throw new BadRequestException("Minimum age may not be less than 0");
                    }
                    if (max >= AGE_MAX) {
                        throw new BadRequestException("Minimum age must be smaller than " + AGE_MAX);
                    }
                    if (min > max) {
                        throw new BadRequestException("Maximum age " + max + " may not be less than minimum age " + min);
                    }
                    break;
            }
            return true;
        }

        public Integer getMin() {
            return min;
        }

        public void setMin(Integer min) {
            this.min = min;
        }

        public Integer getMax() {
            return max;
        }

        public void setMax(Integer max) {
            this.max = max;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public AgeMode getMode() {
            return mode;
        }

        public void setMode(AgeMode mode) {
            this.mode = mode;
        }
    }
}
