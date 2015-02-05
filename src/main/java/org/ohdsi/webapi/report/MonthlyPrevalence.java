/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.report;

import java.util.ArrayList;

/**
 *
 * @author fdefalco
 */
public class MonthlyPrevalence {
    public ArrayList<Float> prevalence;
    public ArrayList<String> monthKey;
    
    public MonthlyPrevalence() {
        prevalence = new ArrayList<>();
        monthKey = new ArrayList<>();
    }
}
