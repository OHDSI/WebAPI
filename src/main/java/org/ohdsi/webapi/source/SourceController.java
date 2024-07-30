package org.ohdsi.webapi.source;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.logging.event.AddDataSourceEvent;
import com.odysseusinc.logging.event.ChangeDataSourceEvent;
import com.odysseusinc.logging.event.DeleteDataSourceEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.ohdsi.webapi.exception.SourceDuplicateKeyException;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.shiro.management.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Path("/source/")
@Component
@Transactional
public class SourceController extends AbstractDaoService {

  public static final String SECURE_MODE_ERROR = "This feature requires the administrator to enable security for the application";

  @Autowired
  private ApplicationEventPublisher publisher;

  @Autowired
  private VocabularyService vocabularyService;

  @Autowired
  private SourceService sourceService;

  @Autowired
  private SourceRepository sourceRepository;

  @Autowired
  private SourceDaimonRepository sourceDaimonRepository;

  @Autowired
  private GenericConversionService conversionService;

  @Autowired
  private Security securityManager;

  @Value("#{!'${security.provider}'.equals('DisabledSecurity')}")
  private boolean securityEnabled;
  
	/**
	 * Gets the list of all Sources in WebAPI database. Sources with a non-null
	 * deleted_date are not returned (ie: these are soft deleted)
	 *
	 * @summary Get Sources
	 * @return A list of all CDM sources with the ID, name, SQL dialect, and key
	 * for each source. The {sourceKey} is used in other WebAPI endpoints to
	 * identify CDMs.
	 */
  @Path("sources")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SourceInfo> getSources() {

    return sourceService.getSources().stream().map(SourceInfo::new).collect(Collectors.toList());
  }

  /**
   * Refresh cached CDM database metadata
   * 
	 * @summary Refresh Sources
   * @return A list of all CDM sources with the ID, name, SQL dialect, and key 
   * for each source (same as the 'sources' endpoint) after refreshing the cached sourced data.
   */
  @Path("refresh")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SourceInfo> refreshSources() {
    sourceService.invalidateCache();
    vocabularyService.clearVocabularyInfoCache();
    sourceService.ensureSourceEncrypted();
    return getSources();
  }
/**
 * Get the priority vocabulary source.
 * 
 * WebAPI designates one CDM vocabulary as the priority vocabulary to be used for vocabulary searches in Atlas.
 * 
 * @summary Get Priority Vocabulary Source
 * @return The CDM metadata for the priority vocabulary.
 */
  @Path("priorityVocabulary")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SourceInfo getPriorityVocabularySourceInfo() {
    return sourceService.getPriorityVocabularySourceInfo();
  }
  
/**
 * Get source by key
 * @summary Get Source By Key
 * @param sourceKey 
 * @return  Metadata for a single Source that matches the <code>sourceKey</code>.
 */
  @Path("{key}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SourceInfo getSource(@PathParam("key") final String sourceKey) {
    return sourceRepository.findBySourceKey(sourceKey).getSourceInfo();
  }

	/**
	 * Get Source Details
	 * 
	 * Source Details contains connection-specific information like JDBC url and authentication information.
	 
	 * @summary Get Source Details
	 * @param sourceId
	 * @return
	 */
	@Path("details/{sourceId}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SourceDetails getSourceDetails(@PathParam("sourceId") Integer sourceId) {
    if (!securityEnabled) {
      throw new NotAuthorizedException(SECURE_MODE_ERROR);
    }
    Source source = sourceRepository.findBySourceId(sourceId);
    return new SourceDetails(source);
  }

	/**
	 * Create a Source
	 * 
	 * @summary Create Source
	 * @param file the keyfile
	 * @param fileDetail the keyfile details
	 * @param request contains the source information (name, key, etc) 
	 * @return a new SourceInfo for the created source
	 * @throws Exception
	 */
	@Path("")
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public SourceInfo createSource(@FormDataParam("keyfile") InputStream file, @FormDataParam("keyfile") FormDataContentDisposition fileDetail, @FormDataParam("source") SourceRequest request) throws Exception {
    if (!securityEnabled) {
      throw new NotAuthorizedException(SECURE_MODE_ERROR);
    }
    Source sourceByKey = sourceRepository.findBySourceKey(request.getKey());
    if (Objects.nonNull(sourceByKey)) {
      throw new SourceDuplicateKeyException("The source key has been already used.");
    }
    Source source = conversionService.convert(request, Source.class);
    if(source.getDaimons() != null) {
      // First source should get priority = 1
      Iterable<Source> sources = sourceRepository.findAll();
      source.getDaimons()
              .stream()
              .filter(sd -> sd.getPriority() <= 0)
              .filter(sd -> {
                 boolean accept = true;
                 // Check if source daimon of given type with priority > 0 already exists in other sources
                 for(Source innerSource: sources) {
                     accept = !innerSource.getDaimons()
                        .stream()
                        .anyMatch(innerDaimon -> innerDaimon.getPriority() > 0
                                && innerDaimon.getDaimonType().equals(sd.getDaimonType()));
                    if(!accept) {
                        break;
                    }
                 }
                 return accept;
              })
              .forEach(sd -> sd.setPriority(1));
    }
    Source original = new Source();
    original.setSourceDialect(source.getSourceDialect());
    setKeyfileData(source, original, file);
    source.setCreatedBy(getCurrentUser());
    source.setCreatedDate(new Date());
    try {
      Source saved = sourceRepository.saveAndFlush(source);
      sourceService.invalidateCache();
      SourceInfo sourceInfo = new SourceInfo(saved);
      publisher.publishEvent(new AddDataSourceEvent(this, source.getSourceId(), source.getSourceName()));
      return sourceInfo;
    } catch (PersistenceException ex) {
      throw new SourceDuplicateKeyException("You cannot use this Source Key, please use different one");
    }
  }
  
	/**
	 * Updates a Source with the provided details from multiple files
	 * 
	 * @summary Update Source
	 * @param file the keyfile
	 * @param fileDetail the keyfile details
	 * @param request contains the source information (name, key, etc) 
	 * @return the updated SourceInfo for the source
	 * @throws Exception
	 */
	@Path("{sourceId}")
  @PUT
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public SourceInfo updateSource(@PathParam("sourceId") Integer sourceId, @FormDataParam("keyfile") InputStream file, @FormDataParam("keyfile") FormDataContentDisposition fileDetail, @FormDataParam("source") SourceRequest request) throws IOException {
    if (!securityEnabled) {
      throw new NotAuthorizedException(SECURE_MODE_ERROR);
    }
    Source updated = conversionService.convert(request, Source.class);
    Source source = sourceRepository.findBySourceId(sourceId);
    if (source != null) {
      updated.setSourceId(sourceId);
      updated.setSourceKey(source.getSourceKey());
      if (StringUtils.isBlank(updated.getUsername()) ||
              Objects.equals(updated.getUsername().trim(), Source.MASQUERADED_USERNAME)) {
        updated.setUsername(source.getUsername());
      }
      if (StringUtils.isBlank(updated.getPassword()) ||
              Objects.equals(updated.getPassword().trim(), Source.MASQUERADED_PASSWORD)) {
        updated.setPassword(source.getPassword());
      }
      setKeyfileData(updated, source, file);
      transformIfRequired(updated);
      if (request.isCheckConnection() == null) {
        updated.setCheckConnection(source.isCheckConnection());
      }
      updated.setModifiedBy(getCurrentUser());
      updated.setModifiedDate(new Date());

      reuseDeletedDaimons(updated, source);

      List<SourceDaimon> removed = source.getDaimons().stream().filter(d -> !updated.getDaimons().contains(d))
              .collect(Collectors.toList());
      // Delete MUST be called after fetching user or source data to prevent autoflush (see DefaultPersistEventListener.onPersist)
      sourceDaimonRepository.delete(removed);
      Source result = sourceRepository.save(updated);
      publisher.publishEvent(new ChangeDataSourceEvent(this, updated.getSourceId(), updated.getSourceName()));
      sourceService.invalidateCache();
      return new SourceInfo(result);
    } else {
      throw new NotFoundException();
    }
  }

  private void reuseDeletedDaimons(Source updated, Source source) {
    List<SourceDaimon> daimons = updated.getDaimons().stream().filter(d -> source.getDaimons().contains(d))
            .collect(Collectors.toList());
    List<SourceDaimon> newDaimons = updated.getDaimons().stream().filter(d -> !source.getDaimons().contains(d))
            .collect(Collectors.toList());

    List<SourceDaimon> allDaimons = sourceDaimonRepository.findBySource(source);

    for (SourceDaimon newSourceDaimon: newDaimons) {
      Optional<SourceDaimon> reusedDaimonOpt = allDaimons.stream()
              .filter(d -> d.equals(newSourceDaimon))
              .findFirst();
      if (reusedDaimonOpt.isPresent()) {
        SourceDaimon reusedDaimon = reusedDaimonOpt.get();
        reusedDaimon.setPriority(newSourceDaimon.getPriority());
        reusedDaimon.setTableQualifier(newSourceDaimon.getTableQualifier());
        daimons.add(reusedDaimon);
      } else {
        daimons.add(newSourceDaimon);
      }
    }
    updated.setDaimons(daimons);
  }

  private void transformIfRequired(Source source) {

    if (DBMSType.BIGQUERY.getOhdsiDB().equals(source.getSourceDialect()) && ArrayUtils.isNotEmpty(source.getKeyfile())) {
      String connStr = source.getSourceConnection().replaceAll("OAuthPvtKeyPath=.+?(;|\\z)", "");
      source.setSourceConnection(connStr);
    }
  }

  private void setKeyfileData(Source updated, Source source, InputStream file) throws IOException {
     if (source.supportsKeyfile()) {
         if (updated.getKeyfileName() != null) {
           if (!Objects.equals(updated.getKeyfileName(), source.getKeyfileName())) {
             byte[] fileBytes = IOUtils.toByteArray(file);
             updated.setKeyfile(fileBytes);
           } else {
             updated.setKeyfile(source.getKeyfile());
           }
           return;
         }
     }
     updated.setKeyfile(null);
     updated.setKeyfileName(null);
   }

	/**
	 * Delete a source.
	 * 
	 * @summary Delete Source
	 * @param sourceId
	 * @return
	 * @throws Exception
	 */
	@Path("{sourceId}")
  @DELETE
  @Transactional
  public Response delete(@PathParam("sourceId") Integer sourceId) throws Exception {
    if (!securityEnabled){
        return getInsecureModeResponse();
    }
    Source source = sourceRepository.findBySourceId(sourceId);
    if (source != null) {
      sourceRepository.delete(source);
      publisher.publishEvent(new DeleteDataSourceEvent(this, sourceId, source.getSourceName()));
      sourceService.invalidateCache();
      return Response.ok().build();
    } else {
      throw new NotFoundException();
    }
  }

	/**
	 * Check source connection.
	 * 
	 * This method attempts to connect to the source by calling 'select 1' on the source connection.
	 * @summary Check connection
	 * @param sourceKey
	 * @return
	 */
	@Path("connection/{key}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional(noRollbackFor = CannotGetJdbcConnectionException.class)
  public SourceInfo checkConnection(@PathParam("key") final String sourceKey) {

    final Source source = sourceService.findBySourceKey(sourceKey);
    sourceService.checkConnection(source);
    return source.getSourceInfo();
  }

	/**
	 * Get the first daimon (ad associated source) that has priority. In the event
	 * of a tie, the first source searched wins.
	 *
	 * @summary Get Priority Daimons
	 * @return
	 */
	@Path("daimon/priority")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Map<SourceDaimon.DaimonType, SourceInfo> getPriorityDaimons() {

    return sourceService.getPriorityDaimons()
            .entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> new SourceInfo(e.getValue())
            ));
  }

	/**
	 * Set priority of daimon
	 * 
	 * Set the priority of the specified daimon of the specified source, and set the other daimons to 0. 
	 * @summary Set Priority
	 * @param sourceKey
	 * @param daimonTypeName
	 * @return
	 */
	@Path("{sourceKey}/daimons/{daimonType}/set-priority")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateSourcePriority(
          @PathParam("sourceKey") final String sourceKey,
          @PathParam("daimonType") final String daimonTypeName
  ) {
    if (!securityEnabled) {
        return getInsecureModeResponse();
    }
    SourceDaimon.DaimonType daimonType = SourceDaimon.DaimonType.valueOf(daimonTypeName);
    List<SourceDaimon> daimonList = sourceDaimonRepository.findByDaimonType(daimonType);
    daimonList.forEach(daimon -> {
      Integer newPriority = daimon.getSource().getSourceKey().equals(sourceKey) ? 1 : 0;
      daimon.setPriority(newPriority);
      sourceDaimonRepository.save(daimon);
    });
    sourceService.invalidateCache();
    return Response.ok().build();
  }

  private Response getInsecureModeResponse() {
      return Response.status(Response.Status.UNAUTHORIZED)
              .entity(SECURE_MODE_ERROR)
              .build();
  }

}