/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.evidence.negativecontrols;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author asena5
 */
public class NegativeControlMapper implements RowMapper<NegativeControlDTO> {
	
        @Override
	public NegativeControlDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		NegativeControlDTO record = new NegativeControlDTO();
                record.conceptSetId = rs.getInt("CONCEPT_SET_ID");
		record.negativeControl = rs.getInt("NEGATIVE_CONTROL");
		record.conceptId = rs.getInt("CONCEPT_ID");
		record.conceptName = rs.getString("CONCEPT_NAME");
		record.sortOrder = rs.getLong("SORT_ORDER");
		record.descendantPmidCount = rs.getLong("DESCENDANT_PMID_CNT");
		record.exactPmidCount = rs.getLong("EXACT_PMID_CNT");
		record.parentPmidCount = rs.getLong("PARENT_PMID_CNT");
		record.ancestorPmidCount = rs.getLong("ANCESTOR_PMID_CNT");
		record.indCi = rs.getInt("IND_CI");
		record.tooBroad = rs.getInt("TOO_BROAD");
		record.drugInduced = rs.getInt("DRUG_INDUCED");
		record.pregnancy = rs.getInt("PREGNANCY");
		record.descendantSplicerCount = rs.getLong("DESCENDANT_SPLICER_CNT");
		record.exactSplicerCount = rs.getLong("EXACT_SPLICER_CNT");
		record.parentSplicerCount = rs.getLong("PARENT_SPLICER_CNT");
		record.ancestorSplicerCount = rs.getLong("ANCESTOR_SPLICER_CNT");
		record.descendantFaersCount = rs.getLong("DESCENDANT_FAERS_CNT");
		record.exactFaersCount = rs.getLong("EXACT_FAERS_CNT");
		record.parentFaersCount = rs.getLong("PARENT_FAERS_CNT");
		record.ancestorFaersCount = rs.getLong("ANCESTOR_FAERS_CNT");
		record.userExcluded = rs.getInt("USER_EXCLUDED");
		record.userIncluded = rs.getInt("USER_INCLUDED");
		record.optimizedOut = rs.getInt("OPTIMIZED_OUT");
		record.notPrevalent = rs.getInt("NOT_PREVALENT");
                
		return record;
	}
    
}
