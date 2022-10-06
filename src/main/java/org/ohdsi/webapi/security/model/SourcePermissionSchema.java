package org.ohdsi.webapi.security.model;

import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.source.Source;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.ohdsi.webapi.shiro.management.Security.SOURCE_ACCESS_PERMISSION;

@Component
public class SourcePermissionSchema extends EntityPermissionSchema {

    private static Map<String, String> readPermissions = new HashMap<String, String>() {{
        put("cohortdefinition:*:report:%s:get", "Get Inclusion Rule Report for Source with SourceKey = %s");
        put("cohortdefinition:*:generate:%s:get", "Generate Cohort on Source with SourceKey = %s");
        put("cohortdefinition:*:cancel:%s:get", "Cancel Cohort Generation on Source with SourceKey = %s");
        put("vocabulary:%s:*:get", "Get vocabulary info on Source with SourceKey = %s");
        put("vocabulary:%s:included-concepts:count:post", "Get vocab concept counts on Source with SourceKey = %s");
        put("vocabulary:%s:resolveConceptSetExpression:post", "Resolve concept set expression on Source with SourceKey = %s");
        put("vocabulary:%s:lookup:identifiers:post", "Lookup identifiers on Source with SourceKey = %s");
        put("vocabulary:%s:lookup:identifiers:ancestors:post", "Lookup identifiers ancestors on Source with SourceKey = %s");
        put("vocabulary:%s:lookup:mapped:post", "Lookup mapped identifiers on Source with SourceKey = %s");
        put("vocabulary:%s:lookup:recommended:post", "Lookup recommendations on Source with SourceKey = %s");
        put("vocabulary:%s:compare:post", "Compare concept sets on Source with SourceKey = %s");
        put("vocabulary:%s:optimize:post", "Optimize concept sets on Source with SourceKey = %s");
        put("vocabulary:%s:concept:*:get", "Get concept on Source with SourceKey = %s");
        put("vocabulary:%s:concept:*:related:get", "Get related concepts on Source with SourceKey = %s");
        put("vocabulary:%s:search:post", "Search vocab on Source with SourceKey = %s");
        put("vocabulary:%s:search:*:get", "Search vocab on Source with SourceKey = %s");
        put("cdmresults:%s:*:get", "Get Achilles reports on Source with SourceKey = %s");
        put("cdmresults:%s:conceptRecordCount:post", "Get Achilles concept counts on Source with SourceKey = %s");
        put("cdmresults:%s:*:*:get", "Get Achilles reports details on Source with SourceKey = %s");
        put("cohortresults:%s:*:*:get", "Get cohort results on Source with SourceKey = %s");
        put("cohortresults:%s:*:*:*:get", "Get cohort results details on Source with SourceKey = %s");
        put("cohortresults:%s:*:healthcareutilization:*:*:get", "Get cohort results baseline on period for Source with SourceKey = %s");
        put("cohortresults:%s:*:healthcareutilization:*:*:*:get", "Get cohort results baseline on occurrence for Source with SourceKey = %s");
        put("ir:*:execute:%s:get", "Generate IR on Source with SourceKey = %s");
        put("ir:*:execute:%s:delete", "Cancel IR generation on Source with SourceKey = %s");
        put("ir:*:info:%s:get", "Get IR execution info on Source with SourceKey = %s");
        put("ir:*:report:%s:get", "Get IR generation report with SourceKey = %s");
        put("ir:%s:info:*:delete", "Delete IR generation report with ID=%s");
        put("%s:person:*:get", "Get person's profile on Source with SourceKey = %s");
        put("vocabulary:%s:lookup:sourcecodes:post", "Lookup source codes in Source with SourceKey = %s");
        put("cohort-characterization:*:generation:%s:post", "Generate Cohort Characterization on Source with SourceKey = %s");
        put("cohort-characterization:*:generation:%s:delete", "Cancel Generation of Cohort Characterization on Source with SourceKey = %s");
        put("pathway-analysis:*:generation:%s:post", "Generate Pathway Analysis on Source with SourceKey = %s");
        put("pathway-analysis:*:generation:%s:delete", "Cancel Generation of Pathway Analysis on Source with SourceKey = %s");
        put("vocabulary:%s:concept:*:ancestorAndDescendant:get", "Get ancestor and descendants on Source with SourceKey = %s");

        put(SOURCE_ACCESS_PERMISSION, "Access to Source with SourceKey = %s");
    }};

    private static Map<String, String> writePermissions = new HashMap<String, String>() {{
        put("source:%s:put", "Edit Source with sourceKey=%s");
        put("source:%s:get", "Read Source with sourceKey=%s");
        put("source:%s:delete", "Delete Source with sourceKey=%s");
    }};

    public SourcePermissionSchema() {

        super(EntityType.SOURCE, readPermissions, writePermissions);
    }

    @Override
    public void onInsert(CommonEntity commonEntity) {

        addSourceUserRole(commonEntity);
        addPermissionsToCurrentUserFromTemplate(commonEntity, getWritePermissions());
    }

    @Override
    public void onDelete(CommonEntity commonEntity) {

        super.onDelete(commonEntity);
        dropSourceUserRole(commonEntity);
    }

    public void addSourceUserRole(CommonEntity commonEntity) {

        Source source = (Source) commonEntity;
        final String roleName = getSourceRoleName(source.getSourceKey());
        final RoleEntity role;
        if (permissionManager.roleExists(roleName)) {
            role = permissionManager.getSystemRoleByName(roleName);
        } else {
            role = permissionManager.addRole(roleName, true);
        }
        permissionManager.addPermissionsFromTemplate(role, getReadPermissions(), source.getSourceKey());
    }

    private void dropSourceUserRole(CommonEntity commonEntity) {

        Source source = (Source) commonEntity;
        final String roleName = getSourceRoleName(source.getSourceKey());
        if (permissionManager.roleExists(roleName)) {
            RoleEntity role = permissionManager.getSystemRoleByName(roleName);
            permissionManager.removePermissionsFromTemplate(getReadPermissions(), source.getSourceKey());
            permissionManager.removeRole(role.getId());
        }
    }

    private String getSourceRoleName(String sourceKey) {

        return String.format("Source user (%s)", sourceKey);
    }
}
