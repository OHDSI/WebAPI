package org.ohdsi.webapi.service;

import java.util.ArrayList;
import java.util.Collection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/source/")
@Component
public class SourceService extends AbstractDaoService {

  @Autowired
  private SourceRepository sourceRepository;

  @Path("sources")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RequiresPermissions("read:source:sources")
  public Collection<SourceInfo> getSources() {
    ArrayList<SourceInfo> sources = new ArrayList<>();
    for (Source source : sourceRepository.findAll()) {
      sources.add(new SourceInfo(source));
    }
    return sources;
  }
  
  @Path("{key}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SourceInfo getSource(@PathParam("key") final String sourceKey) {
    SecurityUtils.getSubject().checkPermission(
            String.format("read:source:source:%s", sourceKey));

    return sourceRepository.findBySourceKey(sourceKey).getSourceInfo();
  }    
}

