package org.ohdsi.webapi.txPathways;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

import org.ohdsi.webapi.txPathways.Pathway;

public class TxPathways {

  public ArrayList<Pathway> pathways;
	
//  public int drug1Rank;
//  public int drug1ConceptId;
//  public String drug1ConceptName;
//  public int drug2Rank;
//  public int drug2ConceptId;
//  public String drug2ConceptName;
//  public int numEdges;
//  
//  //Drug1 Getters and Setters
//  public int getdrug1Rank() {
//	  return drug1Rank;
//  }
//  public void setDrug1Rank(int drugrank) {
//	  this.drug1Rank = drugrank;
//  }
//  public int getdrug1ConceptId() {
//	  return drug1Rank;
//  }
//  public void setDrug1ConceptId(int conceptId) {
//	  this.drug1Rank = conceptId;
//  }
//  public String getDrug1ConceptName() {
//		return drug1ConceptName;
//  }
//  public void setDrug1ConceptName(String conceptName) {
//		this.drug1ConceptName = conceptName;
//  }
//  
//  //Drug2 Getters and Setters
//  public int getdrug2Rank() {
//	  return drug2Rank;
//  }
//  public void setDrug2Rank(int drugrank) {
//	  this.drug2Rank = drugrank;
//  }
//  public int getdrug2ConceptId() {
//	  return drug2Rank;
//  }
//  public void setDrug2ConceptId(int conceptId) {
//	  this.drug2Rank = conceptId;
//  }
//  public String getDrug2ConceptName() {
//		return drug2ConceptName;
//  }
//  public void setDrug2ConceptName(String conceptName) {
//		this.drug2ConceptName = conceptName;
//  }
//  
//  //NumEdges Getters and Setters
//  public int getNumEdges() {
//	  return numEdges;
//  }
//  public void setNumEdges(int numEdge) {
//	  this.numEdges = numEdge;
//  }
  
  public TxPathways() {
	  pathways = new ArrayList<>();
  }
}
