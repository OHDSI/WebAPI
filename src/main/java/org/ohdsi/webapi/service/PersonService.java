/*
 * Copyright 2015 fdefalco.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.SecurityUtils;
import org.ohdsi.webapi.person.ObservationPeriod;
import org.ohdsi.webapi.person.PersonRecord;
import org.ohdsi.webapi.person.CohortPerson;
import org.ohdsi.webapi.person.PersonProfile;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Path("{sourceKey}/person/")
@Component
public class PersonService extends AbstractDaoService {

  @Value("${person.viewDates}")
  private Boolean viewDatesPermitted;
	
  @Value("#{!'${security.provider}'.equals('DisabledSecurity')}")
  private boolean securityEnabled;

  /**
   * Get the complete medical history for a single person in a single database
   * @summary Get complete patient profile
   * @param sourceKey Data source to extract from
   * @param personId Person whose profile to extract
   * @param cohortId (optional) Cohort used to adjust start and end dates. If the person is a member of the cohort then
   *                 start and end dates will be adjusted so they are relative to the cohort start date.
   * @return All records in a patient profile with start and end days relative to cohort start date or initial date of
   * observation
   */
  @Path("{personId}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PersonProfile getPersonProfile(@PathParam("sourceKey") String sourceKey, @PathParam("personId") String personId,
                                        @DefaultValue("0") @QueryParam("cohort") Long cohortId)
  {
    final PersonProfile profile = new PersonProfile();

    boolean showDates = this.canViewDates();

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    profile.gender = "not found";
    profile.yearOfBirth = 0;

    PreparedStatementRenderer psrPersonInfo = preparePersonInfoSql(personId, source);
    getSourceJdbcTemplate(source).query(psrPersonInfo.getSql(), psrPersonInfo.getSetter(), new RowMapper<Void>() {
      @Override
      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
        profile.yearOfBirth = resultSet.getInt("year_of_birth");
        profile.gender = resultSet.getString("gender");
        return null;
      }
    });
    if (profile.gender.equals("not found")) {
        throw new RuntimeException("Can't find person " + personId);        
    }

    // get observation periods
    PreparedStatementRenderer psrObservationPeriods = prepareObservationPeriodsSql(personId, source);
    getSourceJdbcTemplate(source).query(psrObservationPeriods.getSql(), psrObservationPeriods.getSetter(), new RowMapper<Void>() {
      @Override
      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
        ObservationPeriod op = new ObservationPeriod();
        
        op.startDate = resultSet.getTimestamp("start_date");
        op.endDate = resultSet.getTimestamp("end_date");
        op.type = resultSet.getString("observation_period_type");
        op.id = resultSet.getLong("observation_period_id");
        
        profile.observationPeriods.add(op);
        return null;
      }
    });
    // get simplified records
    PreparedStatementRenderer psrPersonProfile = prepareGetPersonProfile(personId, source);
    getSourceJdbcTemplate(source).query(psrPersonProfile.getSql(), psrPersonProfile.getSetter(), new RowMapper<Void>() {

      @Override
      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
        PersonRecord item = new PersonRecord();
        
        item.conceptId = resultSet.getLong("concept_id");
        item.conceptName = resultSet.getString("concept_name");
        item.domain = resultSet.getString("domain");
        item.startDate = resultSet.getTimestamp("start_date");
        item.endDate = resultSet.getTimestamp("end_date");
        
        profile.records.add(item);
        return null;
      }
    });

    PreparedStatementRenderer psrGetCohorts = prepareGetCohortsSql(personId, source);
    getSourceJdbcTemplate(source).query(psrGetCohorts.getSql(), psrGetCohorts.getSetter(), new RowMapper<Void>() {
      @Override
      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
        CohortPerson item = new CohortPerson();
        
        item.startDate = resultSet.getTimestamp("cohort_start_date");
        item.endDate = resultSet.getTimestamp("cohort_end_date");
        item.cohortDefinitionId = resultSet.getLong("cohort_definition_id");
        
        profile.cohorts.add(item);
        return null;
      }
    });


    LocalDateTime cohortStartDate = null;
    Optional<CohortPerson> cohort = cohortId > 0 ? profile.cohorts.stream().filter(c -> c.cohortDefinitionId.equals(cohortId)).findFirst() :
            Optional.empty();
    cohortStartDate = cohort.map(c -> c.startDate.toLocalDateTime()).orElseGet(() ->
      profile.records.stream().min(Comparator.comparing(c -> c.startDate))
        .map(r -> r.startDate.toLocalDateTime()).orElse(null));

		if (cohortStartDate != null && profile.yearOfBirth > 0) {
			profile.ageAtIndex = cohortStartDate.getYear() - profile.yearOfBirth;
		}
		
    for(PersonRecord record : profile.records){
      record.startDay = Math.toIntExact(ChronoUnit.DAYS.between(cohortStartDate, record.startDate.toLocalDateTime()));
      record.endDay = Objects.nonNull(record.endDate) ? Math.toIntExact(ChronoUnit.DAYS.between(cohortStartDate,
              record.endDate.toLocalDateTime())) : record.startDay;
      if (!showDates) {
        record.startDate = null;
        record.endDate = null;
      }
    }
    for(ObservationPeriod period : profile.observationPeriods){
      period.x1 = Math.toIntExact(ChronoUnit.DAYS.between(cohortStartDate,
              period.startDate.toLocalDateTime()));
      period.x2 = Math.toIntExact(ChronoUnit.DAYS.between(cohortStartDate,
              period.endDate.toLocalDateTime()));
      if (!showDates) {
        period.startDate = null;
        period.endDate = null;
      }
    }
    
    return profile;
  }

  protected PreparedStatementRenderer prepareObservationPeriodsSql(String personId, Source source) {

    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    return new PreparedStatementRenderer(source, "/resources/person/sql/getObservationPeriods.sql", "tableQualifier", resultsTableQualifier, "personId", Long.valueOf(personId));
  }

  protected PreparedStatementRenderer prepareGetCohortsSql(String personId, Source source) {

    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    return new PreparedStatementRenderer(source, "/resources/person/sql/getCohorts.sql", "tableQualifier", resultsTableQualifier, "subjectId", Long.valueOf(personId));
  }

  protected PreparedStatementRenderer preparePersonInfoSql(String personId, Source source) {

    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    return new PreparedStatementRenderer(source, "/resources/person/sql/personInfo.sql", "tableQualifier", tableQualifier, "personId", Long.valueOf(personId));
  }

  protected PreparedStatementRenderer prepareGetPersonProfile(String personId, Source source) {

    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    return new PreparedStatementRenderer(source, "/resources/person/sql/getRecords.sql", "tableQualifier", tqValue, "personId", Long.valueOf(personId));
  }
	
	private Boolean canViewDates() {
		if (this.viewDatesPermitted && this.securityEnabled) {
			return SecurityUtils.getSubject().isPermitted(Security.PROFILE_VIEW_DATES_PERMISSION);
		} else {
			return this.viewDatesPermitted;
		}
	}
}
