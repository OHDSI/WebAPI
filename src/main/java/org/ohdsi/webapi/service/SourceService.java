package org.ohdsi.webapi.service;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

@Path("/source/")
@Component
@Transactional
public class SourceService extends AbstractDaoService {

  public static final String SECURE_MODE_ERROR = "This feautre requires the administrator to enable security for the application";

  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private PBEStringEncryptor defaultStringEncryptor;
  @Autowired
  private Environment env;
  @Autowired
  private ApplicationEventPublisher publisher;
  @Value("${datasource.ohdsi.schema}")
  private String schema;

  private boolean encryptorPasswordSet = false;

  @PostConstruct
  public void ensureSourceEncrypted(){
    if (encryptorEnabled && isEncryptorReady()) {
        String query = "SELECT source_id, source_connection FROM ${schema}.source".replaceAll("\\$\\{schema\\}", schema);
        String update = "UPDATE ${schema}.source SET source_connection = ? WHERE source_id = ?".replaceAll("\\$\\{schema\\}", schema);
        getTransactionTemplateRequiresNew().execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                jdbcTemplate.query(query, rs -> {
                    int id = rs.getInt("source_id");
                    String connection = rs.getString("source_connection");
                    if (!PropertyValueEncryptionUtils.isEncryptedValue(connection)){
                        String encrypted = "ENC(" + defaultStringEncryptor.encrypt(connection) + ")";
                        jdbcTemplate.update(update, encrypted, id);
                    }
                });
            }
        });
    }
  }

  @EventListener
  public void onEncryptorPasswordUpdated(EncryptorPasswordUpdatedEvent event) {
    ensureSourceEncrypted();
  }

  public boolean isEncryptorReady() {

    return StringUtils.isNotEmpty(env.getProperty("jasypt.encryptor.password")) || encryptorPasswordSet;
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

  @Value("${security.enabled}")
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
  
  @Path("refresh")
  @GET
  @Produces(MediaType.APPLICATION_JSON)  
  public Collection<SourceInfo> refreshSources() {
    cachedSources = null;
    return getSources();
  }  

  @Path("priorityVocabulary")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SourceInfo getPriorityVocabularySourceInfo() {
    int priority = 0;
    SourceInfo priorityVocabularySourceInfo = null;

    for (Source source : sourceRepository.findAll()) {
      for (SourceDaimon daimon : source.getDaimons()) {
        if (daimon.getDaimonType() == SourceDaimon.DaimonType.Vocabulary) {
          int daimonPriority = daimon.getPriority();
          if (daimonPriority >= priority) {
            priority = daimonPriority;
            priorityVocabularySourceInfo = new SourceInfo(source);
          }
        }
      }
    }

    return priorityVocabularySourceInfo;
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
    SourceDetails details = new SourceDetails(source);
    if (!encryptorEnabled) {
      details.setConnectionString(source.getSourceConnection());
    }
    return details;
  }

  @Path("")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public SourceInfo createSource(SourceRequest request) throws Exception {
    if (!securityEnabled) {
      throw new NotAuthorizedException(SECURE_MODE_ERROR);
    }
    Source source = conversionService.convert(request, Source.class);
    Source saved = sourceRepository.save(source);
    String sourceKey = saved.getSourceKey();
    cachedSources = null;
    securityManager.addSourceRole(sourceKey);
    return new SourceInfo(saved);
  }

  @Path("{sourceId}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public SourceInfo updateSource(@PathParam("sourceId") Integer sourceId, SourceRequest request) {
    if (!securityEnabled) {
      throw new NotAuthorizedException(SECURE_MODE_ERROR);
    }
    Source updated = conversionService.convert(request, Source.class);
    Source source = sourceRepository.findBySourceId(sourceId);
    if (source != null) {
      updated.setSourceId(sourceId);
      updated.setSourceKey(source.getSourceKey());
      if (StringUtils.isBlank(updated.getSourceConnection()) ||
              Objects.equals(updated.getSourceConnection().trim(), Source.MASQUERADED_STRING)) {
        updated.setSourceConnection(source.getSourceConnection());
      }
      List<SourceDaimon> removed = source.getDaimons().stream().filter(d -> !updated.getDaimons().contains(d))
              .collect(Collectors.toList());
      sourceDaimonRepository.delete(removed);
      Source result = sourceRepository.save(updated);
      cachedSources = null;
      return new SourceInfo(result);
    } else {
      throw new NotFoundException();
    }
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
      cachedSources = null;
      securityManager.removeSourceRole(sourceKey);
      return Response.ok().build();
    } else {
      throw new NotFoundException();
    }
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

  @Path("encyptor")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response setEncryptorPassword(String password) {
    if (encryptorPasswordSet) {
      return Response.notModified().build();
    }
    defaultStringEncryptor.setPassword(password);
    encryptorPasswordSet = true;
    publisher.publishEvent(new EncryptorPasswordUpdatedEvent(this));
    return Response.ok().build();
  }

  private Response getInsecureModeResponse() {
      return Response.status(Response.Status.UNAUTHORIZED)
              .entity(SECURE_MODE_ERROR)
              .build();
  }

}
