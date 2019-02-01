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

import java.math.BigDecimal;

/**
 *
 * @author cknoll1
 */
public class HealthcareDrugUtlizationStatistic {
	public long personsCount;
	public BigDecimal personsPct;
	public long exposureCount;
	public BigDecimal exposuresPer1000;
	public BigDecimal exposurePer1000WithExposures;
	public BigDecimal exposurePer1000PerYear;
	public long daysSupplyTotal;
	public BigDecimal daysSupplyAvg;
	public BigDecimal daysSupplyPer1000PerYear;
	public long quantityTotal;
	public BigDecimal quantityAvg;
	public BigDecimal quantityPer1000PerYear;
	public BigDecimal allowed;
	public BigDecimal allowedPmPm;
	public BigDecimal charged;
	public BigDecimal chargedPmPm;		
	public BigDecimal paid;
	public BigDecimal paidPmPm;
	public BigDecimal allowedChargedRatio;
	public BigDecimal paidAllowedRatio;
	
}
