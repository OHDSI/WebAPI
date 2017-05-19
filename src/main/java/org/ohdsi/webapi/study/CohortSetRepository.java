/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.study;

import org.springframework.data.repository.CrudRepository;


/**
 *
 * @author Anthony Sena <https://github.com/ohdsi>
 */
public interface CohortSetRepository extends CrudRepository<CohortSet, Integer> {
  
}
