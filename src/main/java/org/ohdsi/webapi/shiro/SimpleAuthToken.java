package org.ohdsi.webapi.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 *
 * @author gennadiy.anisimov
 */
public class SimpleAuthToken extends UsernamePasswordToken {
  public SimpleAuthToken(String username) {
    super(username, "");
  }

}
