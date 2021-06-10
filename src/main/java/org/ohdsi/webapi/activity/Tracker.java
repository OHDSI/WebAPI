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
package org.ohdsi.webapi.activity;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.ohdsi.webapi.activity.Activity.ActivityType;

/**
 *
 * @author fdefalco
 */
public class Tracker {
  private static ConcurrentLinkedQueue<Activity> activityLog;
  
  public static void trackActivity(ActivityType type, String caption) {
    if (activityLog == null) {
      activityLog = new ConcurrentLinkedQueue<>();
    }
    
    Activity activity = new Activity();
    activity.caption = caption;
    activity.timestamp = new Date();
    activity.type = type;
    
    activityLog.add(activity);
  }
  
  public static Object[] getActivity() {
    if (activityLog == null) {
      activityLog = new ConcurrentLinkedQueue<>();
    }
    
    return activityLog.toArray();
  }
}
