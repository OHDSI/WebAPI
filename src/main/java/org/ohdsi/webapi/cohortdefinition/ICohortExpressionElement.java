/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortdefinition;

/**
 *
 * @author cknoll1
 */
public interface ICohortExpressionElement {
  String accept(ICohortExpressionElementVisitor visitor);
}
