/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortcomparison;

import java.io.Serializable;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

/**
 *
 * @author asena5
 */
public class ConceptSetInfo implements Serializable {
	public int id;
	public String name;
	public ConceptSetExpression expression;
	public String sql;
}
