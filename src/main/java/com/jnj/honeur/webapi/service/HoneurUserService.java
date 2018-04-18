package com.jnj.honeur.webapi.service;

import org.eclipse.collections.impl.block.factory.Comparators;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.shiro.Entities.PermissionEntity;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 *
 * @author gennadiy.anisimov
 */

@Component
@ConditionalOnProperty(name = "datasource.honeur.enabled", havingValue = "true")
public class HoneurUserService extends UserService{

  @Override
  public ArrayList<User> getUsers(){
    return new ArrayList<>();
//    throw new IllegalAccessException("Method is deprecated");
  }
}
