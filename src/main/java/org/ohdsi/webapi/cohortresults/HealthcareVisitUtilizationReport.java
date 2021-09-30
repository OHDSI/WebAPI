/*
 * Copyright 2018 cknoll1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.cohortresults;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author cknoll1
 */
public class HealthcareVisitUtilizationReport {
	public Summary summary;
	public List<ReportItem> data;
	public List<Concept> visitConcepts;
	public List<Concept> visitTypeConcepts;
	
		public static class Summary {
		public long personsCount;
		public BigDecimal personsPct;
		public long visitsCount;
		public BigDecimal visitsPer1000;
		public BigDecimal visitsPer1000WithVisits;
		public BigDecimal visitsPer1000PerYear;
		public long lengthOfStayTotal;
		public BigDecimal lengthOfStayAvg;
		public BigDecimal allowed;
		public BigDecimal allowedPmPm;
		public BigDecimal charged;
		public BigDecimal chargedPmPm;		
		public BigDecimal paid;
		public BigDecimal paidPmPm;
		public BigDecimal allowedChargedRatio;
		public BigDecimal paidAllowedRatio;
		
		

		public Summary() {

		}
	}

	public static class ReportItem extends Summary {

		public String periodType;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		public Date periodStart;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		public Date periodEnd;
		
		public ReportItem() {
		}
	}
}