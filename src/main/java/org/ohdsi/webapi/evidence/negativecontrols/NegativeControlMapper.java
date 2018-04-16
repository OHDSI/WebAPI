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
public class NegativeControlMapper implements RowMapper<NegativeControlRecord> {
	
        @Override
	public NegativeControlRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		NegativeControlRecord record = new NegativeControlRecord();
		record.setEvidenceJobId(rs.getLong("JOB_ID"));
		record.setSourceId(rs.getInt("SOURCE_ID"));
		record.setConceptSetId(rs.getInt("CONCEPT_SET_ID"));
		record.setConceptSetName(rs.getString("CONCEPT_SET_NAME"));
		record.setNegativeControl(rs.getInt("NEGATIVE_CONTROL"));
		record.setConceptId(rs.getInt("CONCEPT_ID"));
		record.setConceptName(rs.getString("CONCEPT_NAME"));
		record.setDomainId(rs.getString("DOMAIN_ID"));
		record.setSortOrder(rs.getLong("SORT_ORDER"));
		record.setDescendantPmidCount(rs.getLong("DESCENDANT_PMID_CNT"));
		record.setExactPmidCount(rs.getLong("EXACT_PMID_CNT"));
		record.setParentPmidCount(rs.getLong("PARENT_PMID_CNT"));
		record.setAncestorPmidCount(rs.getLong("ANCESTOR_PMID_CNT"));
		record.setIndCi(rs.getInt("IND_CI"));
		record.setTooBroad(rs.getInt("TOO_BROAD"));
		record.setDrugInduced(rs.getInt("DRUG_INDUCED"));
		record.setPregnancy(rs.getInt("PREGNANCY"));
		record.setDescendantSplicerCount(rs.getLong("DESCENDANT_SPLICER_CNT"));
		record.setExactSplicerCount(rs.getLong("EXACT_SPLICER_CNT"));
		record.setParentSplicerCount(rs.getLong("PARENT_SPLICER_CNT"));
		record.setAncestorSplicerCount(rs.getLong("ANCESTOR_SPLICER_CNT"));
		record.setDescendantFaersCount(rs.getLong("DESCENDANT_FAERS_CNT"));
		record.setExactFaersCount(rs.getLong("EXACT_FAERS_CNT"));
		record.setParentFaersCount(rs.getLong("PARENT_FAERS_CNT"));
		record.setAncestorFaersCount(rs.getLong("ANCESTOR_FAERS_CNT"));
		record.setUserExcluded(rs.getInt("USER_EXCLUDED"));
		record.setUserIncluded(rs.getInt("USER_INCLUDED"));
		record.setOptimizedOut(rs.getInt("OPTIMIZED_OUT"));
		record.setNotPrevalent(rs.getInt("NOT_PREVALENT"));
                
		return record;
	}
    
}
