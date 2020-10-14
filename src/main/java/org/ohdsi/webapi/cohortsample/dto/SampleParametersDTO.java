package org.ohdsi.webapi.cohortsample.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class SampleParametersDTO {
	private static final int SIZE_MAX = 500;
	private static final int AGE_MAX = 500;

	/** Sample size. */
	private int size;
	/** Sample name. */
	private String name;

	/** Gender criteria. */
	private GenderDTO gender;

	/** Age criteria. */
	private AgeDTO age;

	/**
	 * Validate this DTO.
	 * @throws BadRequestException if the DTO is not valid.
	 */
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
		if (age != null && !age.validate()) {
			age = null;
		}
		if (gender != null && !gender.validate()) {
			gender = null;
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
		public final static int GENDER_MALE_CONCEPT_ID = 8507;
		public final static int GENDER_FEMALE_CONCEPT_ID = 8532;

		private Integer conceptId;

		private List<Integer> conceptIds;

		private boolean otherNonBinary = false;

		/**
		 * Validate this DTO.
		 * @return true if this DTO contains any information, false otherwise.
		 */
		public boolean validate() {
			if (conceptIds == null) {
				conceptIds = new ArrayList<>();
			} else if (conceptIds.contains(null)) {
				conceptIds.removeIf(Objects::isNull);
			}
			if (conceptId != null) {
				conceptIds.add(conceptId);
				conceptId = null;
			}

			if (!isOtherNonBinary() && conceptIds.isEmpty()) {
				return false;
			}

			if (isOtherNonBinary()) {
				conceptIds.removeIf(i -> i != GENDER_MALE_CONCEPT_ID && i != GENDER_FEMALE_CONCEPT_ID);
			}
			return true;
		}

		public Integer getConceptId() {
			return conceptId;
		}

		public void setConceptId(Integer conceptId) {
			this.conceptId = conceptId;
		}

		public List<Integer> getConceptIds() {
			return conceptIds;
		}

		public void setConceptIds(List<Integer> conceptIds) {
			this.conceptIds = conceptIds;
		}

		public boolean isOtherNonBinary() {
			return otherNonBinary;
		}

		public void setOtherNonBinary(boolean otherNonBinary) {
			this.otherNonBinary = otherNonBinary;
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class AgeDTO {
		private Integer min;
		private Integer max;
		private Integer value;
		private AgeMode mode;

		/**
		 * Validate this DTO.
		 * @return true if this DTO contains any information, false otherwise.
		 * @throws BadRequestException if the DTO is not valid.
		 */
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
						throw new BadRequestException("Cannot use single age comparison mode " + mode.getSerialName() + " without age property.");
					}
					if (min != null || max != null) {
						throw new BadRequestException("Cannot use age range property with comparison mode " + mode.getSerialName() + ".");
					}
					break;
				case BETWEEN:
				case NOT_BETWEEN:
					if (min == null || max == null) {
						throw new BadRequestException("Cannot use age range comparison mode " + mode.getSerialName() + " without ageMin and ageMax properties.");
					}
					if (value != null) {
						throw new BadRequestException("Cannot use single age property with comparison mode " + mode.getSerialName() + ".");
					}
					if (min < 0) {
						throw new BadRequestException("Minimum age may not be less than 0");
					}
					if (max >= AGE_MAX) {
						throw new BadRequestException("Maximum age must be smaller than " + AGE_MAX);
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
