/*
<<<<<<<< HEAD:src/main/java/org/ohdsi/webapi/cache/CacheInfo.java
 * Copyright 2019 cknoll1.
========
 * Copyright 2022 cknoll1.
>>>>>>>> refs/remotes/OHDSI_WebAPI/master-2.14:src/main/java/org/ohdsi/webapi/vocabulary/ConceptRecommendedNotInstalledException.java
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
<<<<<<<< HEAD:src/main/java/org/ohdsi/webapi/cache/CacheInfo.java
package org.ohdsi.webapi.cache;
========
package org.ohdsi.webapi.vocabulary;
>>>>>>>> refs/remotes/OHDSI_WebAPI/master-2.14:src/main/java/org/ohdsi/webapi/vocabulary/ConceptRecommendedNotInstalledException.java

/**
 *
 * @author cknoll1
 */
<<<<<<<< HEAD:src/main/java/org/ohdsi/webapi/cache/CacheInfo.java
public class CacheInfo {
	public String cacheName;
	public Long entries;
========
public class ConceptRecommendedNotInstalledException extends RuntimeException{
  
>>>>>>>> refs/remotes/OHDSI_WebAPI/master-2.14:src/main/java/org/ohdsi/webapi/vocabulary/ConceptRecommendedNotInstalledException.java
}
