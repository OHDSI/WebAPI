package com.jnj.honeur.webapi.service;

import org.ohdsi.webapi.service.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 *
 * @author gennadiy.anisimov
 */

@Component("honeurUserService")
@ConditionalOnProperty(name = "datasource.honeur.enabled", havingValue = "true")
public class HoneurUserService extends UserService{

  @Override
  public ArrayList<User> getUsers(){
    return new ArrayList<>();
//    throw new IllegalAccessException("Method is deprecated");
  }
}
