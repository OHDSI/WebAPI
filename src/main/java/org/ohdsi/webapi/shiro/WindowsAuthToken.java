/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 *
 * @author gennadiy.anisimov
 */
public class WindowsAuthToken extends UsernamePasswordToken {
  public WindowsAuthToken(String username, String password) {  
    super(username, password);
  }
}
