package org.ohdsi.webapi.service;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.ohdsi.webapi.shiny.ApplicationBrief;
import org.ohdsi.webapi.shiny.ConflictPositConnectException;
import org.ohdsi.webapi.shiny.PackagingStrategies;
import org.ohdsi.webapi.shiny.PackagingStrategy;
import org.ohdsi.webapi.shiny.PositConnectClient;
import org.ohdsi.webapi.shiny.PositConnectClientException;
import org.ohdsi.webapi.shiny.ShinyPackagingService;
import org.ohdsi.webapi.shiny.ShinyPublishedEntity;
import org.ohdsi.webapi.shiny.ShinyPublishedRepository;
import org.ohdsi.webapi.shiny.TemporaryFile;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.annotations.SourceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "shiny.enabled", havingValue = "true")
public class ShinyService {
    private static final Logger log = LoggerFactory.getLogger(ShinyService.class);
    private final Map<CommonAnalysisType, ShinyPackagingService> servicesMap;
    @Autowired
    private ShinyPublishedRepository shinyPublishedRepository;
    @Autowired
    private PermissionManager permissionManager;
    @Autowired
    private PositConnectClient connectClient;

    @Inject
    public ShinyService(List<ShinyPackagingService> services) {
        servicesMap = services.stream().collect(Collectors.toMap(ShinyPackagingService::getType, Function.identity()));
    }

    public void publishApp(String type, int id, String sourceKey) {
        TemporaryFile data = packageShinyApp(type, id, sourceKey, PackagingStrategies.targz());
        ShinyPublishedEntity publication = getPublication(id, sourceKey);
        ShinyPackagingService service = findShinyService(CommonAnalysisType.valueOf(type.toUpperCase()));
        UUID contentId = Optional.ofNullable(publication.getContentId())
                .orElseGet(() -> createOrFindItem(service.getBrief(id, sourceKey)));
        String bundleId = connectClient.uploadBundle(contentId, data);
        String taskId = connectClient.deployBundle(contentId, bundleId);
        log.debug("Bundle [{}] is deployed to Shiny server, task id: [{}]", id, taskId);
    }

    private UUID createOrFindItem(ApplicationBrief brief) {
        try {
            return connectClient.createContentItem(brief);
        } catch (ConflictPositConnectException e) {
            log.warn("Content item [{}] already exist, will update", brief.getName());
            return connectClient.listContentItems().stream()
                    .filter(i -> Objects.equals(i.name, brief.getName()))
                    .findFirst()
                    .map(i -> i.guid)
                    .orElseThrow(NotFoundException::new);
        }
    }

    private ShinyPublishedEntity getPublication(int id, String sourceKey) {
        return shinyPublishedRepository.findByAnalysisIdAndSourceKey(Integer.toUnsignedLong(id), sourceKey).orElseGet(() -> {
            ShinyPublishedEntity entity = new ShinyPublishedEntity();
            entity.setAnalysisId(Integer.toUnsignedLong(id));
            entity.setSourceKey(sourceKey);
            entity.setCreatedBy(permissionManager.getCurrentUser());
            entity.setCreatedDate(Date.from(Instant.now()));
            return entity;
        });
    }

    public TemporaryFile packageShinyApp(String type, int id, String sourceKey, PackagingStrategy packaging) {
        CommonAnalysisType analysisType = CommonAnalysisType.valueOf(type.toUpperCase());
        ShinyPackagingService service = findShinyService(analysisType);
        return service.packageApp(id, sourceKey, packaging);
    }

    private ShinyPackagingService findShinyService(CommonAnalysisType type) {
        return Optional.ofNullable(servicesMap.get(type))
                .orElseThrow(() -> new NotFoundException(MessageFormat.format("Shiny application download is not supported for [{0}] analyses.", type)));
    }
}
