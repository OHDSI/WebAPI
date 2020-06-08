package org.ohdsi.webapi.service;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.logging.event.AddDataSourceEvent;
import com.odysseusinc.logging.event.ChangeDataSourceEvent;
import com.odysseusinc.logging.event.DeleteDataSourceEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.ohdsi.webapi.common.SourceMapKey;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;
import org.ohdsi.webapi.source.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Path("/source/")
@Component
@Transactional
public class SourceService extends AbstractDaoService {

  public static final String SECURE_MODE_ERROR = "This feature requires the administrator to enable security for the application";
  private static final String KRB_REALM = "KrbRealm";
  private static final String KRB_FQDN = "KrbHostFQDN";

  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private PBEStringEncryptor defaultStringEncryptor;
  @Autowired
  private Environment env;
  @Autowired
  private ApplicationEventPublisher publisher;
  @Autowired
  private VocabularyService vocabularyService;
  @Autowired
  private SourcePriorityService sourcePriorityService;

  @Autowired
  private SourceAccessor sourceAccessor;

  @Value("${datasource.ohdsi.schema}")
  private String schema;


  private boolean encryptorPasswordSet = false;

  @PostConstruct
  public void ensureSourceEncrypted(){
    if (encryptorEnabled) {
			String query = "SELECT source_id, username, password FROM ${schema}.source".replaceAll("\\$\\{schema\\}", schema);
			String update = "UPDATE ${schema}.source SET username = ?, password = ? WHERE source_id = ?".replaceAll("\\$\\{schema\\}", schema);
			getTransactionTemplateRequiresNew().execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
					jdbcTemplate.query(query, rs -> {
						int id = rs.getInt("source_id");
						String username = rs.getString("username");
						String password = rs.getString("password");
						if (username != null && !PropertyValueEncryptionUtils.isEncryptedValue(username)){
							username = "ENC(" + defaultStringEncryptor.encrypt(username) + ")";
						}
						if (password != null && !PropertyValueEncryptionUtils.isEncryptedValue(password)){
							password = "ENC(" + defaultStringEncryptor.encrypt(password) + ")";
						}
						jdbcTemplate.update(update, username, password, id);
					});
				}
			});
		}
	}

  public Source findBySourceKey(final String sourceKey) {

    return sourceRepository.findBySourceKey(sourceKey);
  }

  public Source findBySourceId(final Integer sourceId) {

    return sourceRepository.findBySourceId(sourceId);
  }

  public class SortByKey implements Comparator<SourceInfo>
  {
    private boolean isAscending;

    public SortByKey(boolean ascending) {
      isAscending = ascending;
    }

    public SortByKey() {
      this(true);
    }

    public int compare(SourceInfo s1, SourceInfo s2) {
      return s1.sourceKey.compareTo(s2.sourceKey) * (isAscending ? 1 : -1);
    }
  }
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

  @Value("${jasypt.encryptor.enabled}")
  private boolean encryptorEnabled;

  private static Collection<SourceInfo> cachedSources = null;

  @Path("sources")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SourceInfo> getSources() {

    if (cachedSources == null) {
      ArrayList<SourceInfo> sources = new ArrayList<>();
      for (Source source : sourceRepository.findAll()) {
        sources.add(new SourceInfo(source));
      }
      Collections.sort(sources, new SortByKey());
      cachedSources = sources;
    }
    return cachedSources;
  }

  public <T> Map<T, SourceInfo> getSourcesMap(SourceMapKey<T> mapKey) {

    return getSources().stream().collect(Collectors.toMap(mapKey.getKeyFunc(), s -> s));
  }

  @Path("refresh")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SourceInfo> refreshSources() {
    cachedSources = null;
    vocabularyService.clearVocabularyInfoCache();
		this.ensureSourceEncrypted();
    return getSources();
  }

  @Path("priorityVocabulary")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SourceInfo getPriorityVocabularySourceInfo() {
    return new SourceInfo(sourcePriorityService.getPrioritySourceForDaimon(SourceDaimon.DaimonType.Vocabulary));
  }

  @Path("{key}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SourceInfo getSource(@PathParam("key") final String sourceKey) {
    return sourceRepository.findBySourceKey(sourceKey).getSourceInfo();
  }

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
      throw new Exception("The source key has been already used.");
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
    Source saved = sourceRepository.save(source);
    String sourceKey = saved.getSourceKey();
    cachedSources = null;
    securityManager.addSourceRole(sourceKey);
    SourceInfo sourceInfo = new SourceInfo(saved);
    publisher.publishEvent(new AddDataSourceEvent(this, source.getSourceId(), source.getSourceName()));
    return sourceInfo;
  }

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
      List<SourceDaimon> removed = source.getDaimons().stream().filter(d -> !updated.getDaimons().contains(d))
              .collect(Collectors.toList());
      sourceDaimonRepository.delete(removed);
      Source result = sourceRepository.save(updated);
        publisher.publishEvent(new ChangeDataSourceEvent(this, updated.getSourceId(), updated.getSourceName()));
      cachedSources = null;
      return new SourceInfo(result);
    } else {
      throw new NotFoundException();
    }
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

  @Path("{sourceId}")
  @DELETE
  @Transactional
  public Response delete(@PathParam("sourceId") Integer sourceId) throws Exception {
    if (!securityEnabled){
        return getInsecureModeResponse();
    }
    Source source = sourceRepository.findBySourceId(sourceId);
    if (source != null) {
      final String sourceKey = source.getSourceKey();
      sourceRepository.delete(source);
      publisher.publishEvent(new DeleteDataSourceEvent(this, sourceId, source.getSourceName()));
      cachedSources = null;
      securityManager.removeSourceRole(sourceKey);
      return Response.ok().build();
    } else {
      throw new NotFoundException();
    }
  }

  @Path("connection/{key}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional(noRollbackFor = CannotGetJdbcConnectionException.class)
  public SourceInfo checkConnection(@PathParam("key") final String sourceKey) {

    final Source source = sourceRepository.findBySourceKey(sourceKey);
    sourcePriorityService.checkConnection(source);
    return source.getSourceInfo();
  }


  @Path("daimon/priority")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Map<SourceDaimon.DaimonType, SourceInfo> getPriorityDaimons() {

    return sourcePriorityService.getPriorityDaimons()
            .entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> new SourceInfo(e.getValue())
            ));
  }

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
    cachedSources = null;
    return Response.ok().build();
  }

  private Response getInsecureModeResponse() {
      return Response.status(Response.Status.UNAUTHORIZED)
              .entity(SECURE_MODE_ERROR)
              .build();
  }

}