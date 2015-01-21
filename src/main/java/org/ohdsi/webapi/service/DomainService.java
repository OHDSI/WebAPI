/**
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
 */
package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.vocabulary.Domain;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Path("/domain/")
public class DomainService extends AbstractDAOService {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Domain> getDomains() {
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getDomains.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[] { "CDM_schema" }, new String[] { getCdmSchema() });
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());
        
        return getJdbcTemplate().query(sql_statement, new RowMapper<Domain>() {
            
            @Override
            public Domain mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
                final Domain domain = new Domain();
                domain.domainId = resultSet.getString("DOMAIN_ID");
                domain.domainName = resultSet.getString("DOMAIN_NAME");
                domain.domainConceptId = resultSet.getLong("DOMAIN_CONCEPT_ID");
                return domain;
            }
        });
    }
}
