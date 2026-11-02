/*
 * Copyright 2024 cknoll1.
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
package org.ohdsi.webapi.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.mjeanroy.dbunit.core.dataset.DataSetFactory;
import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.security.authc.UserOrigin;
import org.ohdsi.webapi.security.authz.mapping.ExternalRoleMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.Assert.*;

/**
 * Integration tests for External Role Mapping subsystem - CSV Import Operations.
 * 
 * Tests verify that CSV import operations (add, delete, overwrite) properly update
 * the sec_external_role_map table. Tests chain operations sequentially to verify
 * state propagation and interaction between operations.
 * 
 * Test flow:
 * 1. Load CSV_SETUP with initial 5 mappings
 * 2. Phase 1 - ADD: Load add_mappings.csv, call addMappings(), verify 3 added, state=8
 * 3. Phase 2 - DELETE: Load delete_mappings.csv, call deleteMappings(), verify 3 removed, state=5
 * 4. Phase 3 - OVERWRITE: Load overwrite_ldap.csv, call overwriteMappings(), verify state=5 (LDAP replaced)
 * 5. Cleanup: truncateTable (remove FK refs), then DELETE fixture
 * 
 * @author cknoll1
 */
public class ExternalRoleMappingCsvTest extends AbstractDatabaseTest {

  @Autowired
  private ExternalRoleMapService externalRoleMapService;

  private static final String SETUP_DATA = "/externalRoleMapping/externalRoleMappingCsv_SETUP.json";
  private static final String EXPECTED_DATA = "/externalRoleMapping/externalRoleMappingCsv_EXPECTED.json";

  /**
   * Comprehensive CSV import test: chained ADD → DELETE → OVERWRITE operations.
   * 
   * Scenario:
   * - Start with 5 mappings (2 LDAP, 1 WINDOWS, 2 OIDC)
   * - ADD 3 new LDAP mappings via LDAP origin → 8 total (tests add operation code path)
   * - DELETE the 3 added LDAP mappings → back to 5 (tests delete operation code path)
   * - OVERWRITE LDAP origin with new set (2 mappings) → 5 total (tests overwrite; LDAP changed, others unchanged)
   */
  @Test
  public void testCsvOperationsSequentially() throws Exception {
    loadPrepData(new String[] { SETUP_DATA }, DatabaseOperation.INSERT);

    try {
      // ======================================================================
      // Phase 1: ADD new mappings via LDAP origin (tests add operation code path)
      // ======================================================================
      MultipartFile addCsv = loadCsvAsMultipart("add_mappings.csv");
      ExternalRoleMapService.ImportResult addResult = externalRoleMapService.addMappings(addCsv, UserOrigin.LDAP);
      
      // Verify add result: all 3 rows from CSV should be added as LDAP mappings
      assertEquals("Should add 3 LDAP mappings from CSV", 3, addResult.summary().addedCount());
      assertEquals("Should have 3 rows in add result", 3, addResult.rows().size());
      for (ExternalRoleMapService.RowResult row : addResult.rows()) {
        assertEquals("Each row should have ADDED status", ExternalRoleMapService.RowResultStatus.ADDED,
            row.status());
      }

      // Verify database state: 8 total mappings (5 original + 3 added as LDAP)
      IDatabaseConnection dbCon = getConnection();
      try {
        ITable actualAfterAdd = dbCon.createQueryTable("mappings",
            "SELECT origin, external_claim, role_id FROM " + ohdsiSchema
                + ".sec_external_role_map ORDER BY origin, external_claim, role_id");

        IDataSet expectedDataSet = DataSetFactory.createDataSet(new String[] { EXPECTED_DATA });
        ITable expectedAfterAdd = expectedDataSet.getTable("mappings.afterAdd");

        Assertion.assertEquals(expectedAfterAdd, actualAfterAdd);
      } finally {
        dbCon.close();
      }

      // ======================================================================
      // Phase 2: DELETE the LDAP mappings we just added (tests delete operation code path)
      // ======================================================================
      MultipartFile deleteCsv = loadCsvAsMultipart("delete_mappings.csv");
      ExternalRoleMapService.ImportResult deleteResult = externalRoleMapService.deleteMappings(deleteCsv, UserOrigin.LDAP);

      // Verify delete result: all 3 rows should be removed
      assertEquals("Should remove 3 LDAP mappings from CSV", 3, deleteResult.summary().removedCount());
      assertEquals("Should have 3 rows in delete result", 3, deleteResult.rows().size());
      for (ExternalRoleMapService.RowResult row : deleteResult.rows()) {
        assertEquals("Each row should have REMOVED status", ExternalRoleMapService.RowResultStatus.REMOVED,
            row.status());
      }

      // Verify database state: 5 mappings (back to baseline after adding and removing)
      dbCon = getConnection();
      try {
        ITable actualAfterDelete = dbCon.createQueryTable("mappings",
            "SELECT origin, external_claim, role_id FROM " + ohdsiSchema
                + ".sec_external_role_map ORDER BY origin, external_claim, role_id");

        IDataSet expectedDataSet = DataSetFactory.createDataSet(new String[] { EXPECTED_DATA });
        ITable expectedAfterDelete = expectedDataSet.getTable("mappings.afterDelete");

        Assertion.assertEquals(expectedAfterDelete, actualAfterDelete);
      } finally {
        dbCon.close();
      }

      // ======================================================================
      // Phase 3: OVERWRITE LDAP mappings (replaces entire LDAP origin)
      // ======================================================================
      MultipartFile overwriteCsv = loadCsvAsMultipart("overwrite_ldap.csv");
      ExternalRoleMapService.ImportResult overwriteResult = externalRoleMapService.overwriteMappings(overwriteCsv,
          UserOrigin.LDAP);

      // Verify overwrite result: 1 added (CN=DataSteward), 1 removed (CN=Admins), 1 skipped (CN=Analysts)
      assertEquals("Should add 1 new LDAP mapping", 1, overwriteResult.summary().addedCount());
      assertEquals("Should remove 1 old LDAP mapping", 1, overwriteResult.summary().removedCount());
      assertEquals("Should skip 1 existing LDAP mapping", 1, overwriteResult.summary().skippedCount());
      assertEquals("Should have 3 LDAP rows in result", 3, overwriteResult.rows().size());

      // Verify database state: 5 total mappings (same count, different LDAP content)
      dbCon = getConnection();
      try {
        ITable actualAfterOverwrite = dbCon.createQueryTable("mappings",
            "SELECT origin, external_claim, role_id FROM " + ohdsiSchema
                + ".sec_external_role_map ORDER BY origin, external_claim, role_id");

        IDataSet expectedDataSet = DataSetFactory.createDataSet(new String[] { EXPECTED_DATA });
        ITable expectedAfterOverwrite = expectedDataSet.getTable("mappings.afterOverwrite");

        Assertion.assertEquals(expectedAfterOverwrite, actualAfterOverwrite);
      } finally {
        dbCon.close();
      }

    } finally {
      // Cleanup: truncate mappings table FIRST, then delete fixture
      truncateTable(ohdsiSchema + ".sec_external_role_map");
      loadPrepData(new String[] { SETUP_DATA }, DatabaseOperation.DELETE);
    }
  }

  /**
   * Test CSV validation errors: invalid header, too many columns, too few columns, 
   * empty fields, description too long, role not found, duplicate claims.
   * 
   * All scenarios should throw InvalidFormatException or ValidationException before
   * attempting to insert data.
   */
  @Test
  public void testCsvValidationErrors() throws Exception {
    loadPrepData(new String[] { SETUP_DATA }, DatabaseOperation.INSERT);

    try {
      // Scenario 1: Invalid header names
      // Expected: InvalidFormatException with message about expected header
      try {
        MultipartFile invalidHeaderCsv = loadCsvAsMultipart("invalid_header.csv");
        externalRoleMapService.addMappings(invalidHeaderCsv, UserOrigin.LDAP);
        fail("Should have thrown InvalidFormatException for invalid header");
      } catch (ExternalRoleMapService.InvalidFormatException e) {
        assertTrue("Error message should mention header", e.getMessage().contains("header"));
        assertTrue("Error message should mention expected format", 
            e.getMessage().contains("claim") || e.getMessage().contains("role_name"));
      }

      // Scenario 2: Too many columns in header
      // Expected: InvalidFormatException with message about column count
      try {
        MultipartFile tooManyColumnsCsv = loadCsvAsMultipart("too_many_columns.csv");
        externalRoleMapService.addMappings(tooManyColumnsCsv, UserOrigin.LDAP);
        fail("Should have thrown InvalidFormatException for too many columns");
      } catch (ExternalRoleMapService.InvalidFormatException e) {
        assertTrue("Error message should mention header or columns", 
            e.getMessage().contains("header") || e.getMessage().contains("column"));
      }

      // Scenario 3: Too few columns in data row
      // Expected: InvalidFormatException with message about column count
      try {
        MultipartFile tooFewColumnsCsv = loadCsvAsMultipart("too_few_columns.csv");
        externalRoleMapService.addMappings(tooFewColumnsCsv, UserOrigin.LDAP);
        fail("Should have thrown InvalidFormatException for too few columns");
      } catch (ExternalRoleMapService.InvalidFormatException e) {
        assertTrue("Error message should mention column count", 
            e.getMessage().contains("column") || e.getMessage().contains("Expected 3"));
      }

      // Scenario 4: Empty claim field
      // Expected: InvalidFormatException with message about empty claim
      try {
        MultipartFile emptyClaimCsv = loadCsvAsMultipart("empty_claim.csv");
        externalRoleMapService.addMappings(emptyClaimCsv, UserOrigin.LDAP);
        fail("Should have thrown InvalidFormatException for empty claim");
      } catch (ExternalRoleMapService.InvalidFormatException e) {
        assertTrue("Error message should mention claim or empty", 
            e.getMessage().contains("claim") || e.getMessage().contains("empty"));
      }

      // Scenario 5: Empty role_name field
      // Expected: InvalidFormatException with message about empty role_name
      try {
        MultipartFile emptyRoleNameCsv = loadCsvAsMultipart("empty_role_name.csv");
        externalRoleMapService.addMappings(emptyRoleNameCsv, UserOrigin.LDAP);
        fail("Should have thrown InvalidFormatException for empty role_name");
      } catch (ExternalRoleMapService.InvalidFormatException e) {
        assertTrue("Error message should mention role_name or empty", 
            e.getMessage().contains("role_name") || e.getMessage().contains("empty"));
      }

      // Scenario 6: Description exceeds 500 characters
      // Expected: InvalidFormatException with message about description length
      try {
        MultipartFile descriptionTooLongCsv = loadCsvAsMultipart("description_too_long.csv");
        externalRoleMapService.addMappings(descriptionTooLongCsv, UserOrigin.LDAP);
        fail("Should have thrown InvalidFormatException for description too long");
      } catch (ExternalRoleMapService.InvalidFormatException e) {
        assertTrue("Error message should mention description or 500 characters", 
            e.getMessage().contains("description") || e.getMessage().contains("500"));
      }

      // Scenario 7: Role name does not exist in database
      // Expected: ValidationException with message about role not found
      try {
        MultipartFile roleNotFoundCsv = loadCsvAsMultipart("role_not_found.csv");
        externalRoleMapService.addMappings(roleNotFoundCsv, UserOrigin.LDAP);
        fail("Should have thrown ValidationException for non-existent role");
      } catch (ExternalRoleMapService.ValidationException e) {
        assertTrue("Error message should mention role not found", 
            e.getMessage().contains("not found") || e.getMessage().contains("Role"));
      }

      // Scenario 8: Duplicate claim in CSV
      // Expected: ValidationException with message about duplicate claim
      try {
        MultipartFile duplicateClaimCsv = loadCsvAsMultipart("duplicate_claim.csv");
        externalRoleMapService.addMappings(duplicateClaimCsv, UserOrigin.LDAP);
        fail("Should have thrown ValidationException for duplicate claim");
      } catch (ExternalRoleMapService.ValidationException e) {
        assertTrue("Error message should mention duplicate", e.getMessage().contains("Duplicate"));
      }

    } finally {
      // Cleanup: truncate mappings table FIRST, then delete fixture
      truncateTable(ohdsiSchema + ".sec_external_role_map");
      loadPrepData(new String[] { SETUP_DATA }, DatabaseOperation.DELETE);
    }
  }

  /**
   * Load a CSV file from classpath and wrap it as MockMultipartFile for service consumption.
   * 
   * @param filename CSV filename in /externalRoleMapping/ resource directory
   * @return MockMultipartFile with CSV content
   * @throws IOException if file cannot be read
   */
  private MultipartFile loadCsvAsMultipart(String filename) throws Exception {
    String resourcePath = "/externalRoleMapping/" + filename;
    byte[] fileBytes = Files.readAllBytes(
        Paths.get(getClass().getResource(resourcePath).toURI()));
    return new MockMultipartFile("csvfile", filename, "text/csv", fileBytes);
  }
}
