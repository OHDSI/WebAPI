/*
 * Copyright 2015 fdefalco.
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
package org.ohdsi.webapi.person;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.sql.Timestamp;

/**
 *
 * @author fdefalco
 */
public class PersonProfile {

  @JsonIgnore
  public HashMap<Long, EraSet> eraSets;
  
  @JsonProperty("durationInDays")
  public Long getDurationInDays() {
    return (endDate.getTime() - startDate.getTime()) / (1000*60*60*25);
  }

  public ArrayList<PersonRecord> records;

  public PersonProfile() {
    eraSets = new HashMap<>();
    records = new ArrayList<>();
  }

  public Timestamp startDate;
  public Timestamp endDate;

  public void addRecord(PersonRecord item) {
    if (startDate == null) {
      startDate = item.startDate;
    }
    
    if (endDate == null) {
      endDate = item.endDate;
    }
    
    if (item.startDate.before(startDate)) {
      startDate = item.startDate;
    }
    
    if (item.endDate.after(endDate)) {
      endDate = item.endDate;
    }
    
    records.add(item);
    EraSet set;

    if (!eraSets.containsKey(item.conceptId)) {
      EraSet temp = new EraSet();
      temp.conceptId = item.conceptId;
      temp.conceptName = item.conceptName;
      temp.eraType = item.recordType;
      eraSets.put(item.conceptId, temp);
    }

    set = eraSets.get(item.conceptId);
    Era era = new Era();
    era.startDate = item.startDate;
    era.endDate = item.endDate;
    set.eras.add(era);
  }

  @JsonProperty("recordCount") 
  public Integer getRecordCount() {
    return this.records.size();
  }
  
  @JsonProperty("eraSets")
  public Collection<EraSet> getEraSets() {
    return eraSets.values();
  }

  @JsonProperty("timewave")
  public Timewave generateTimewave() {
    Timewave tw = new Timewave();
    HashMap<Long, TimewaveBucket> bd = new HashMap<>();

    for (PersonRecord record : this.records) {
      for (Timestamp ti = record.startDate; ti.before(record.endDate); ti.setTime(ti.getTime() + (1000 * 60 * 60 * 24))) {
        TimewaveBucket bucket;

        if (!bd.containsKey(ti.getTime())) {
          bucket = new TimewaveBucket();
          bucket.timeIndex = ti;
          bd.put(ti.getTime(), bucket);
        } else {
          bucket = bd.get(ti.getTime());
        }

        if (null != record.recordType) switch (record.recordType) {
          case "drug":
            bucket.drugs++;
            break;
          case "condition":
            bucket.conditions++;
            break;
          case "observation":
            bucket.observations++;
            break;            
        }
        
        tw.maxEvents = Math.max(bucket.getRecords(), tw.maxEvents);
      }
    }

    tw.buckets = bd.values();
    return tw;
  }

}
