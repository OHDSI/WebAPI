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
package org.ohdsi.webapi.service;

import com.opencsv.CSVWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.vocabulary.Concept;
import org.ohdsi.webapi.vocabulary.ConceptSetExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author fdefalco
 */
@Path("/conceptset/")
@Component
public class ConceptSetService extends AbstractDaoService {

    @Autowired
    private VocabularyService vocabService;

    @Autowired
    private SourceService sourceService;

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ConceptSet getConceptSet(@PathParam("id") final int id) {
        return getConceptSetRepository().findById(id);
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Iterable<ConceptSet> getConceptSets() {
        return getConceptSetRepository().findAll();
    }

    @GET
    @Path("{id}/items")
    @Produces(MediaType.APPLICATION_JSON)
    public Iterable<ConceptSetItem> getConceptSetItems(@PathParam("id") final int id) {
        return getConceptSetItemRepository().findAllByConceptSetId(id);
    }

    @GET
    @Path("{id}/expression")
    @Produces(MediaType.APPLICATION_JSON)
    public ConceptSetExpression getConceptSetExpression(@PathParam("id") final int id) {
        HashMap<Long, ConceptSetItem> map = new HashMap<>();

        // collect the concept set items so we can lookup their properties later
        for (ConceptSetItem csi : getConceptSetItems(id)) {
            map.put(csi.getConceptId(), csi);
        }

        // create our expression to return
        ConceptSetExpression expression = new ConceptSetExpression();
        expression.items = new ConceptSetExpression.ConceptSetItem[map.size()];

        // lookup the concepts we need information for
        long[] identifiers = new long[map.size()];
        int identifierIndex = 0;
        for (Long identifier : map.keySet()) {
            identifiers[identifierIndex] = identifier;
            identifierIndex++;
        }

        // assume we want to resolve using the priority vocabulary provider
        SourceInfo vocabSourceInfo = sourceService.getPriorityVocabularySourceInfo();
        Collection<Concept> concepts = vocabService.executeIdentifierLookup(vocabSourceInfo.sourceKey, identifiers);

        // put the concept information into the expression along with the concept set item information 
        int conceptIndex = 0;
        for (Concept concept : concepts) {
            expression.items[conceptIndex] = new ConceptSetExpression.ConceptSetItem();
            expression.items[conceptIndex].concept = concept;

            ConceptSetItem csi = map.get(concept.conceptId);
            expression.items[conceptIndex].includeDescendants = (csi.getIncludeDescendants() == 1);
            expression.items[conceptIndex].includeMapped = (csi.getIncludeMapped() == 1);
            expression.items[conceptIndex].isExcluded = (csi.getIsExcluded() == 1);
            conceptIndex++;
        }

        return expression;
    }

    @GET
    @Path("{id}/{name}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ConceptSet> getConceptSetExists(@PathParam("id") final int id, @PathParam("name") String name) {
        return getConceptSetRepository().conceptSetExists(id, name);
    }

    @POST
    @Path("{id}/items")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public boolean saveConceptSetItems(@PathParam("id") final int id, ConceptSetItem[] items) {
        getConceptSetItemRepository().deleteByConceptSetId(id);

        for (ConceptSetItem csi : items) {
            csi.setConceptSetId(id);
            getConceptSetItemRepository().save(csi);
        }

        return true;
    }

    @GET
    @Path("/exportlist")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportConceptSetList(@QueryParam("conceptsets") final String conceptSetList) throws Exception {
        ArrayList<Integer> conceptSetIds = new ArrayList<Integer>();
        try {
            String[] conceptSetItems = conceptSetList.split("\\+");
            for(String csi : conceptSetItems) {
                conceptSetIds.add(Integer.valueOf(csi));
            }
            if (conceptSetIds.size() <= 0) {
                throw new IllegalArgumentException("You must supply a querystring value for conceptsets that is of the form: ?conceptset=<concept_set_id_1>+<concept_set_id_2>+<concept_set_id_n>");
            }
        } catch (Exception e) {
            throw e;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SourceInfo sourceInfo = sourceService.getPriorityVocabularySourceInfo();
        ArrayList<ConceptSetExport> cs = new ArrayList<ConceptSetExport>();
        Response response = null;
        try {
            // Load all of the concept sets requested
            for (int i = 0; i < conceptSetIds.size(); i++) {
                // Get the concept set information
                cs.add(getConceptSetForExport(conceptSetIds.get(i), sourceInfo));
            }
           // Write Concept Set Expression to a CSV
            baos = writeConceptSetToCSVAndZip(cs);

            response = Response
                    .ok(baos)
                    .type(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"conceptSetExport.zip\"")
                    .build();

        } catch (Exception ex) {
            throw ex;
        }
        return response;
    }

    @GET
    @Path("{id}/export")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportConceptSetToCSV(@PathParam("id") final String id) throws Exception {
        return this.exportConceptSetList(id);
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConceptSet saveConceptSet(ConceptSet conceptSet) {
        conceptSet = this.getConceptSetRepository().save(conceptSet);
        return conceptSet;
    }
    
    private ConceptSetExport getConceptSetForExport(int conceptSetId, SourceInfo vocabSource) {
        ConceptSetExport cs = new ConceptSetExport();

        // Set the concept set id
        cs.ConceptSetId = conceptSetId;
        // Get the concept set information
        cs.ConceptSetName = this.getConceptSet(conceptSetId).getName();
        // Get the concept set expression
        cs.csExpression = this.getConceptSetExpression(conceptSetId);

        // Resolve the concept set
        cs.conceptIds = vocabService.resolveConceptSetExpression(vocabSource.sourceKey, cs.csExpression);

        // Create an array of concept Ids that will be used in the subsequent calls
        long[] conceptIds = new long[cs.conceptIds.size()];
        Iterator<Long> iter = cs.conceptIds.iterator();
        for (int j = 0; iter.hasNext(); j++) {
            conceptIds[j] = iter.next();
        }
        //Java 8 this will be more efficent:
        //long[] conceptIds = cs.conceptIds.stream().mapToLong(i->i).toArray();

        // Lookup the identifiers
        cs.identifierConcepts = vocabService.executeIdentifierLookup(vocabSource.sourceKey, conceptIds);
        // Lookup the mapped items
        cs.mappedConcepts = vocabService.executeMappedLookup(vocabSource.sourceKey, conceptIds);

        return cs;
    }

    private ByteArrayOutputStream writeConceptSetToCSVAndZip(ArrayList<ConceptSetExport> conceptSetExportList) throws Exception {
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
            allLines = new ArrayList<String[]>();
            headings = "Concept Set ID#Name#Concept ID#Concept Code#Concept Name#Concept Class ID#Domain#Vocabulary".split("#");
            allLines.add(headings);
            for (ConceptSetExport cs : conceptSetExportList) {
                for (Concept c : cs.identifierConcepts) {
                    ArrayList<String> csItem = new ArrayList<String>();
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
            allLines = new ArrayList<String[]>();
            headings = "Concept Set ID#Name#Concept ID#Concept Code#Concept Name#Concept Class ID#Domain#Vocabulary".split("#");
            allLines.add(headings);
            for (ConceptSetExport cs : conceptSetExportList) {
                for (Concept c : cs.mappedConcepts) {
                    ArrayList<String> csItem = new ArrayList<String>();
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
            throw ex;
        }

    }
}
