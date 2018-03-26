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
public class HealthcareExposureReport {
	public ExposureSummary summary;
	public List<ExposureReportItem> data;

	public static class ExposureSummary {
		public long personsCount;
		public BigDecimal exposureTotal;
		public BigDecimal exposureAvg;

		public ExposureSummary() {

		}
	}

	public static class ExposureReportItem {

		public String periodType;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		public Date periodStart;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		public Date periodEnd;
		public long personsCount;
		public BigDecimal personsPct;
		public BigDecimal exposureTotal;
		public BigDecimal exposurePct;
		public BigDecimal exposureAvg;
		
		public ExposureReportItem() {
		}
	}
}
