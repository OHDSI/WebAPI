package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.security.dto.RoleDTO;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.security.model.EntityType;
import org.ohdsi.webapi.security.model.UserSimpleAuthorizationInfo;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.util.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.ohdsi.webapi.util.ConversionUtils.convertMetadata;

public abstract class BaseCommonEntityToDTOConverter<S extends CommonEntity, T extends CommonEntityDTO>
        extends BaseConversionServiceAwareConverter<S, T> {
    private final Logger logger = LoggerFactory.getLogger(BaseCommonEntityToDTOConverter.class);

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private EntityPermissionSchemaResolver schemaResolver;

    @Autowired
    private PermissionManager permissionManager;

    @Value("#{!'${security.provider}'.equals('DisabledSecurity')}")
    private boolean securityEnabled;

    protected abstract void doConvert(S source, T target);

    @Override
    public T convert(S s) {
        T target = createResultObject(s);
        convertMetadata(conversionService, s, target);
        fillWriteAccess(s, target);
        doConvert(s, target);
        return target;
    }

    private void fillWriteAccess(S s, T t) {
        if (securityEnabled && s.getCreatedBy() != null) {
            try {
                String login = this.permissionManager.getSubjectName();
                UserSimpleAuthorizationInfo authorizationInfo = this.permissionManager.getAuthorizationInfo(login);
                if (!Objects.equals(authorizationInfo.getUserId(), s.getCreatedBy().getId())) {
                    EntityType entityType = schemaResolver.getEntityType(s.getClass());

                    List<RoleDTO> roles = permissionService.getRolesHavingPermissions(entityType, s.getId());

                    Collection<String> userRoles = authorizationInfo.getRoles();
                    boolean hasRole = roles.stream()
                            .anyMatch(r -> userRoles.stream()
                                    .anyMatch(re -> re.equals(r.getName())));

                    t.setHasWriteAccess(hasRole);
                }
            } catch (Exception e) {
                logger.error("Error getting user roles and permissions", e);
                throw new RuntimeException(e);
            }
        }
    }
}
