/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.study.report;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public interface ReportRepository extends CrudRepository<Report, Integer> {
  
  @Query("select sr from StudyReport AS sr JOIN FETCH sr.study as s")          
  List<Report> list();

}
