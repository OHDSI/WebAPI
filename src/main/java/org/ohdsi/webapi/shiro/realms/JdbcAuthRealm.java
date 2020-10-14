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
package org.ohdsi.webapi.shiro.realms;


import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.ohdsi.webapi.shiro.Entities.UserPrincipal;
import org.ohdsi.webapi.shiro.tokens.ActiveDirectoryUsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcAuthRealm extends JdbcRealm {

    private static final Logger log = LoggerFactory.getLogger(JdbcAuthRealm.class);
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public JdbcAuthRealm(DataSource dataSource, String authenticationQuery) {

        this.setDataSource(dataSource);
        this.setAuthenticationQuery(authenticationQuery);
    }

    @Override
    public boolean supports(AuthenticationToken token) {

        return token != null && token.getClass() == UsernamePasswordToken.class;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        SimpleAuthenticationInfo info;

        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();

        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        } else {

            UserPrincipal userPrincipal = this.getUser(username);

            if (userPrincipal == null || userPrincipal.getPassword() == null ||
                    !bCryptPasswordEncoder.matches(new String(upToken.getPassword()), userPrincipal.getPassword())) {
                throw new AuthenticationException("Incorrect username or password");
            } else {
                info = new SimpleAuthenticationInfo(userPrincipal, upToken.getPassword(), this.getName());
            }
        }
        return info;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        // Prevent from querying of permissions
        return new SimpleAuthorizationInfo();
    }

    private UserPrincipal getUser(String username) {
        UserPrincipal result = null;

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = createPreparedStatement(conn, username);
                ResultSet rs = ps.executeQuery()
        ) {
            for (boolean foundResult = false; rs.next(); foundResult = true) {
                if (foundResult) {
                    throw new AuthenticationException("More than one user row found for user [" + username + "]. Usernames must be unique.");
                }
                result = extractUserEntity(rs);
                result.setUsername(username);
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

    private UserPrincipal extractUserEntity(ResultSet rs) throws SQLException {
        UserPrincipal userEntity = new UserPrincipal();

        // Old style query is used - only password is queried from database
        if(rs.getMetaData().getColumnCount() == 1) {
            userEntity.setPassword(rs.getString(1));
        } else {
            // New style query - user name is also queried from database
            userEntity.setPassword(rs.getString("password"));
            String firstName = trim(rs.getString("firstname"));
            String midlleName = trim(rs.getString("middlename"));
            String lastName = trim(rs.getString("lastname"));

            StringBuilder name = new StringBuilder(firstName);
            if (!midlleName.isEmpty()) {
                name.append(' ').append(midlleName);
            }
            if (!lastName.isEmpty()) {
                name.append(' ').append(lastName);
            }

            userEntity.setName(name.toString().trim());
        }

        return userEntity;
    }

    private String trim(String value) {
        if(value != null) {
            return value.trim();
        }
        return StringUtils.EMPTY;
    }
}
