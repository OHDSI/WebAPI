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
package org.ohdsi.webapi.evidence;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author fdefalco
 */
public class LinkoutData {
  @JsonProperty("AN")
  public String an;

  @JsonProperty("BODY")
  public String body;

  @JsonProperty("TARGET")
  public String target;

  @JsonProperty("SOURCEURL")
  public String sourceURL;

  @JsonProperty("SELECTOR")
  public String selector;

  @JsonProperty("SPL")
  public String spl;
  
  @JsonProperty("TEXT")
  public String text;
}
