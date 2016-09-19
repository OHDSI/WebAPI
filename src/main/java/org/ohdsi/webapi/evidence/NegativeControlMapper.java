/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.evidence;

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
                record.setSourceId(rs.getInt("SOURCE_ID"));
                record.setConceptSetId(rs.getInt("CONCEPT_SET_ID"));
                record.setConceptSetName(rs.getString("CONCEPT_SET_NAME"));
                record.setConceptId(rs.getInt("CONCEPT_ID"));
                record.setConceptName(rs.getString("CONCEPT_NAME"));
                record.setDomainId(rs.getString("DOMAIN_ID"));
                record.setMedlineCt(rs.getDouble("MEDLINE_CT"));
                record.setMedlineCase(rs.getDouble("MEDLINE_CASE"));
                record.setMedlineOther(rs.getDouble("MEDLINE_OTHER"));
                record.setSemmeddbCtT(rs.getDouble("SEMMEDDB_CT_T"));
                record.setSemmeddbCaseT(rs.getDouble("SEMMEDDB_CASE_T"));
                record.setSemmeddbOtherT(rs.getDouble("SEMMEDDB_OTHER_T"));
                record.setSemmeddbCtF(rs.getDouble("SEMMEDDB_CT_F"));
                record.setSemmeddbCaseF(rs.getDouble("SEMMEDDB_CASE_F"));
                record.setSemmeddbOtherF(rs.getDouble("SEMMEDDB_OTHER_F"));
                record.setEu_spc(rs.getDouble("EU_SPC"));
                record.setSplADR(rs.getDouble("SPL_ADR"));
                record.setAers(rs.getDouble("AERS"));
                record.setAersPRR(rs.getDouble("AERS_PRR"));
                // Scaled values
                record.setMedlineCtScaled(rs.getDouble("MEDLINE_CT_SCALED"));
                record.setMedlineCaseScaled(rs.getDouble("MEDLINE_CASE_SCALED"));
                record.setMedlineOtherScaled(rs.getDouble("MEDLINE_OTHER_SCALED"));
                record.setSemmeddbCtTScaled(rs.getDouble("SEMMEDDB_CT_T_SCALED"));
                record.setSemmeddbCaseTScaled(rs.getDouble("SEMMEDDB_CASE_T_SCALED"));
                record.setSemmeddbOtherTScaled(rs.getDouble("SEMMEDDB_OTHER_T_SCALED"));
                record.setSemmeddbCtFScaled(rs.getDouble("SEMMEDDB_CT_F_SCALED"));
                record.setSemmeddbCaseFScaled(rs.getDouble("SEMMEDDB_CASE_F_SCALED"));
                record.setSemmeddbOtherFScaled(rs.getDouble("SEMMEDDB_OTHER_F_SCALED"));
                record.setEuSPCScaled(rs.getDouble("EU_SPC_SCALED"));
                record.setSplADRScaled(rs.getDouble("SPL_ADR_SCALED"));
                record.setAersScaled(rs.getDouble("AERS_SCALED"));
                record.setAersPRRScaled(rs.getDouble("AERS_PRR_SCALED"));
                // Betas
                record.setMedlineCtBeta(rs.getDouble("MEDLINE_CT_BETA"));
                record.setMedlineCaseBeta(rs.getDouble("MEDLINE_CASE_BETA"));
                record.setMedlineOtherBeta(rs.getDouble("MEDLINE_OTHER_BETA"));
                record.setSemmeddbCtTBeta(rs.getDouble("SEMMEDDB_CT_T_BETA"));
                record.setSemmeddbCaseTBeta(rs.getDouble("SEMMEDDB_CASE_T_BETA"));
                record.setSemmeddbOtherTBeta(rs.getDouble("SEMMEDDB_OTHER_T_BETA"));
                record.setSemmeddbCtFBeta(rs.getDouble("SEMMEDDB_CT_F_BETA"));
                record.setSemmeddbCaseFBeta(rs.getDouble("SEMMEDDB_CASE_F_BETA"));
                record.setSemmeddbOtherFBeta(rs.getDouble("SEMMEDDB_OTHER_F_BETA"));
                record.setEuSPCBeta(rs.getDouble("EU_SPC_BETA"));
                record.setSplADRBeta(rs.getDouble("SPL_ADR_BETA"));
                record.setAersBeta(rs.getDouble("AERS_BETA"));
                record.setAersPRRBeta(rs.getDouble("AERS_PRR_BETA"));
                // Prediction values
                record.setRawPrediction(rs.getDouble("RAW_Prediction"));
                record.setPrediction(rs.getDouble("Prediction"));
                
		return record;
	}
    
}
