package org.ohdsi.webapi.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceDaimonRepository;
import org.ohdsi.webapi.source.SourceDetails;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.source.SourceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Path("/source/")
@Component
public class SourceService extends AbstractDaoService {

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

  @Path("details/{key}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SourceDetails getSourceDetails(@PathParam("key") String sourceKey) {
    return new SourceDetails(sourceRepository.findBySourceKey(sourceKey));
  }

  @Path("")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public SourceInfo createSource(SourceRequest request) throws Exception {
    Source source = conversionService.convert(request, Source.class);
    Source saved = sourceRepository.save(source);
    String sourceKey = saved.getSourceKey();
    cachedSources = null;
    securityManager.addSourceRole(sourceKey);
    return new SourceInfo(saved);
  }

  @Path("{key}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public SourceInfo updateSource(@PathParam("key") String sourceKey, SourceRequest request) {
    Source updated = conversionService.convert(request, Source.class);
    Source source = sourceRepository.findBySourceKey(sourceKey);
    if (source != null) {
      updated.setSourceId(source.getSourceId());
      updated.setSourceKey(sourceKey);
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

  @Path("{key}")
  @DELETE
  @Transactional
  public Response delete(@PathParam("key") String sourceKey) throws Exception {
    Source source = sourceRepository.findBySourceKey(sourceKey);
    if (source != null) {
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
      return Response.status(Response.Status.UNAUTHORIZED)
              .entity("Priority setting is available only in secured mode of application")
              .build();
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

}
