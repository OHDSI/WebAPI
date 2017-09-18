/*
 * Copyright 2016 Observational Health Data Sciences and Informatics [OHDSI.org].
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
package org.ohdsi.webapi.conceptset;

import com.opencsv.CSVWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class ExportUtil {
  
  public static ByteArrayOutputStream writeConceptSetExportToCSVAndZip(ArrayList<ConceptSetExport> conceptSetExportList) throws RuntimeException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);
    StringWriter sw;
    CSVWriter csvWriter;
    String[] headings;
    try {
      // Write Concept Set Expression to a CSV
      sw = new StringWriter();
      csvWriter = new CSVWriter(sw);
      ArrayList<String[]> allLines = new ArrayList<String[]>();
      headings = "Concept Set ID#Name#Concept ID#Concept Code#Concept Name#Domain#Vocabulary#Standard Concept#Exclude#Descendants#Mapped".split("#");
      allLines.add(headings);
      for (ConceptSetExport cs : conceptSetExportList) {
        for (ConceptSetExpression.ConceptSetItem item : cs.csExpression.items) {
          ArrayList<String> csItem = new ArrayList<String>();
          csItem.add(String.valueOf(cs.ConceptSetId));
          csItem.add(cs.ConceptSetName);
          csItem.add(String.valueOf(item.concept.conceptId));
          csItem.add(String.valueOf(item.concept.conceptCode));
          csItem.add(item.concept.conceptName);
          csItem.add(item.concept.domainId);
          csItem.add(item.concept.vocabularyId);
          csItem.add(item.concept.standardConcept);
          csItem.add(String.valueOf(item.isExcluded));
          csItem.add(String.valueOf(item.includeDescendants));
          csItem.add(String.valueOf(item.includeMapped));
          allLines.add(csItem.toArray(new String[0]));
        }
      }
      csvWriter.writeAll(allLines);
      csvWriter.flush();

      ZipEntry resultsEntry = new ZipEntry("conceptSetExpression.csv");
      zos.putNextEntry(resultsEntry);
      zos.write(sw.getBuffer().toString().getBytes());

      // Write included concepts to a CSV
      sw = new StringWriter();
      csvWriter = new CSVWriter(sw);
      allLines = new ArrayList<>();
      headings = "Concept Set ID#Name#Concept ID#Concept Code#Concept Name#Concept Class ID#Domain#Vocabulary".split("#");
      allLines.add(headings);
      for (ConceptSetExport cs : conceptSetExportList) {
        for (Concept c : cs.identifierConcepts) {
          ArrayList<String> csItem = new ArrayList<>();
          csItem.add(String.valueOf(cs.ConceptSetId));
          csItem.add(cs.ConceptSetName);
          csItem.add(String.valueOf(c.conceptId));
          csItem.add(String.valueOf(c.conceptCode));
          csItem.add(c.conceptName);
          csItem.add(c.conceptClassId);
          csItem.add(c.domainId);
          csItem.add(c.vocabularyId);
          allLines.add(csItem.toArray(new String[0]));
        }
      }
      csvWriter.writeAll(allLines);
      csvWriter.flush();

      resultsEntry = new ZipEntry("includedConcepts.csv");
      zos.putNextEntry(resultsEntry);
      zos.write(sw.getBuffer().toString().getBytes());

      // Write mapped concepts to a CSV
      sw = new StringWriter();
      csvWriter = new CSVWriter(sw);
      allLines = new ArrayList<>();
      headings = "Concept Set ID#Name#Concept ID#Concept Code#Concept Name#Concept Class ID#Domain#Vocabulary".split("#");
      allLines.add(headings);
      for (ConceptSetExport cs : conceptSetExportList) {
        for (Concept c : cs.mappedConcepts) {
          ArrayList<String> csItem = new ArrayList<>();
          csItem.add(String.valueOf(cs.ConceptSetId));
          csItem.add(cs.ConceptSetName);
          csItem.add(String.valueOf(c.conceptId));
          csItem.add(String.valueOf(c.conceptCode));
          csItem.add(c.conceptName);
          csItem.add(c.conceptClassId);
          csItem.add(c.domainId);
          csItem.add(c.vocabularyId);
          allLines.add(csItem.toArray(new String[0]));
        }
      }
      csvWriter.writeAll(allLines);
      csvWriter.flush();

      resultsEntry = new ZipEntry("mappedConcepts.csv");
      zos.putNextEntry(resultsEntry);
      zos.write(sw.getBuffer().toString().getBytes());

      zos.closeEntry();
      zos.close();
      baos.flush();
      baos.close();
      return baos;
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

  }
}
