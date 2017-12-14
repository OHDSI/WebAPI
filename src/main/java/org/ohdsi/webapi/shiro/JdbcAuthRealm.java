/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Mikhail Mironov, Pavel Grafkin
 *
 */
package org.ohdsi.webapi.shiro;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcAuthRealm extends JdbcRealm {

    private static final Log log = LogFactory.getLog(JdbcAuthRealm.class);
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public JdbcAuthRealm(DataSource dataSource, String authenticationQuery) {

        setAuthenticationTokenClass(UsernamePasswordToken.class);

        this.setDataSource(dataSource);
        this.setAuthenticationQuery(authenticationQuery);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        SimpleAuthenticationInfo info;

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();

        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        } else {

            String password = this.getPasswordForUser(username);

            if (password == null ||
                    !bCryptPasswordEncoder.matches(new String(upToken.getPassword()), password)) {
                throw new AuthenticationException("Incorrect username or password");
            } else {
                info = new SimpleAuthenticationInfo(username, upToken.getPassword(), this.getName());
            }
        }
        return info;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        // Prevent from querying of permissions
        return new SimpleAuthorizationInfo();
    }

    private String getPasswordForUser(String username) {
        String result = null;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = createPreparedStatement(conn, username);
                ResultSet rs = ps.executeQuery()
        ) {
            for (boolean foundResult = false; rs.next(); foundResult = true) {
                if (foundResult) {
                    throw new AuthenticationException("More than one user row found for user [" + username + "]. Usernames must be unique.");
                }
                result = rs.getString(1);
            }
        } catch (SQLException e) {
            String message = "There was a SQL error while authenticating user [" + username + "]";
            if (log.isErrorEnabled()) {
                log.error(message, e);
            }
            result = null;
        }
        return result;
    }

    private PreparedStatement createPreparedStatement(Connection conn, String username) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(this.authenticationQuery);
        ps.setString(1, username);
        return ps;
    }

}
