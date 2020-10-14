/*
 * Copyright 2020 cknoll1.
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
package org.ohdsi.webapi.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author cknoll1
 */
public final class SqlUtils {

	/**
		* Creates a DATEFROMPARTS() expression based on an input in form YYYY-MM-DD.
		* @param date a string in YYYY-MM-DD format
		* @return a String containing the DATEFROMPARTS expression
		* @throws IllegalArgumentException
		* @author cknoll1
	*/
  public static String dateStringToSql(String date) {
		if (!date.matches("^\\d{4}-\\d{2}-\\d{2}"))
			throw new IllegalArgumentException(String.format("%s is not in valid YYYY-MM-DD format.", date));
    String[] dateParts = StringUtils.split(date, '-');
    return String.format("DATEFROMPARTS(%s, %s, %s)", Integer.valueOf(dateParts[0]), Integer.valueOf(dateParts[1]), Integer.valueOf(dateParts[2]));
  }  
}
