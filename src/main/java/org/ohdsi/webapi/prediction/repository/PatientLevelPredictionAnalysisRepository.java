/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.prediction.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.prediction.PatientLevelPredictionAnalysis;

/**
 *
 * @author asena5
 */
public interface PatientLevelPredictionAnalysisRepository extends EntityGraphJpaRepository<PatientLevelPredictionAnalysis, Integer> {
}