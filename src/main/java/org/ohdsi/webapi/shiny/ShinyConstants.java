package org.ohdsi.webapi.shiny;

public enum ShinyConstants {
    VALUE_NOT_AVAILABLE("N/A"),
    DATE_TIME_FORMAT("yyyy-MM-dd HH:mm:ss"),
    PROPERTY_NAME_REPO_LINK("repo_link"),
    PROPERTY_NAME_COHORT_LINK("cohort_link"),
    PROPERTY_NAME_COHORT_NAME("cohort_name"),
    PROPERTY_NAME_ATLAS_URL("atlas_url"),
    PROPERTY_NAME_ATLAS_LINK("atlas_link"),
    PROPERTY_NAME_DATASOURCE_KEY("datasource"),
    PROPERTY_NAME_DATASOURCE_NAME("datasource_name"),
    PROPERTY_NAME_ASSET_ID("asset_id"),
    PROPERTY_NAME_ASSET_NAME("asset_name"),
    PROPERTY_NAME_ANALYSIS_NAME("analysis_name"),
    PROPERTY_NAME_AUTHOR("author"),
    PROPERTY_NAME_AUTHOR_NOTES("author_notes"),
    PROPERTY_NAME_GENERATED_DATE("generated_date"),
    PROPERTY_NAME_RECORD_COUNT("record_count"),
    PROPERTY_NAME_REFERENCED_COHORTS("referenced_cohorts"),
    PROPERTY_NAME_VERSION_ID("version_id"),
    PROPERTY_NAME_GENERATION_ID("generation_id"),
    PROPERTY_NAME_PERSON_COUNT("person_count");

    private final String value;

    ShinyConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
