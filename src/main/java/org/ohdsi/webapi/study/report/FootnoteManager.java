/*
 * Copyright 2017 cknoll1.
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
package org.ohdsi.webapi.study.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author cknoll1
 */
public class FootnoteManager {
	private final String[] noteGlphys = new String[] {"*","†","‡", "§","#","¶"};
	private int glyphIndex = 0;
	private HashMap<String,String> glyphLookup = new HashMap<>();
	private List<String> noteOrder = new ArrayList<>();

	public static class FootnoteEntry {
		public String glyph;
		public String footnote;
	}
	public FootnoteManager() {
	}
	
	private String getNextSymbol(){
		String nextSymbol = StringUtils.repeat(noteGlphys[glyphIndex % noteGlphys.length], (glyphIndex / noteGlphys.length) + 1);
		glyphIndex++;
		return nextSymbol;
	}
	
	public String getFootnoteSymbol(String footnote) {
		if (!glyphLookup.containsKey(footnote)) {
			glyphLookup.put(footnote, getNextSymbol());
			noteOrder.add(footnote);
		}
		return glyphLookup.get(footnote);
	}
	public List<FootnoteEntry> getFootnotes() {
		
		return noteOrder.stream().map(n -> {
			FootnoteEntry entry = new FootnoteEntry();
			entry.footnote = n;
			entry.glyph = glyphLookup.get(n);
			return entry;
		}).collect(Collectors.toList());
	}
}
