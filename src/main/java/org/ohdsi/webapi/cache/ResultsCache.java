/*
 * Copyright 2017 fdefalco.
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
package org.ohdsi.webapi.cache;

import java.util.HashMap;
import org.ohdsi.webapi.cdmresults.CDMResultsCache;

/**
 *
 * @author fdefalco
 */
public class ResultsCache {
    private static HashMap<String,CDMResultsCache> sourceCaches;
    
    public HashMap<String,CDMResultsCache> getCaches() {
        if (sourceCaches == null) {
            sourceCaches = new HashMap<>();
        }
        
        return sourceCaches;
    }
    
    public CDMResultsCache getCache(String sourceKey) {
        return this.getCaches().get(sourceKey);
    }
}
