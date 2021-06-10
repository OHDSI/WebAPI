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
 * Authors: Pavel Grafkin
 *
 */
package org.ohdsi.webapi.shiro.realms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.ohdsi.webapi.shiro.tokens.JwtAuthToken;
import org.ohdsi.webapi.shiro.tokens.SpnegoToken;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// For instructions see README.MD in this package
public class KerberosAuthRealm extends AuthenticatingRealm {

    private String serviceProviderName;
    private String keytabPath;

    private final Log logger = LogFactory.getLog(KerberosAuthRealm.class);

    public KerberosAuthRealm(String serviceProviderName, String keytabPath) {
        this.serviceProviderName = serviceProviderName;
        this.keytabPath = keytabPath;
    }

    @Override
    public boolean supports(AuthenticationToken token) {

        return token != null && token.getClass() == SpnegoToken.class;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        SpnegoToken token = (SpnegoToken) authenticationToken;

        if (token.getCredentials() instanceof byte[]) {

            byte[] gssapiData = (byte[]) token.getCredentials();
            String username = validateTicket(this.serviceProviderName, this.keytabPath, gssapiData);

            if (username != null) {
                return new SimpleAuthenticationInfo(username, gssapiData, this.getName());
            }
        }

        throw new AuthenticationException();
    }

    private Configuration getJaasKrb5TicketCfg(
            final String principal, final String keytabPath) {

        return new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {

                Map<String, String> options = new HashMap<String, String>() {{
                    put("principal", principal);
                    put("keyTab", keytabPath);
                    put("doNotPrompt", "true");
                    put("useKeyTab", "true");
                    put("storeKey", "true");
                    put("isInitiator", "false");
                }};

                return new AppConfigurationEntry[]{
                        new AppConfigurationEntry(
                                "com.sun.security.auth.module.Krb5LoginModule",
                                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                Collections.unmodifiableMap(options)
                        )
                };
            }
        };
    }

    private String validateTicket(String spn, String keytabPath, byte[] ticket) {
        LoginContext ctx = null;
        try {
            // define the principal who will validate the ticket
            Principal principal = new KerberosPrincipal(spn, KerberosPrincipal.KRB_NT_SRV_INST);
            Set<Principal> principals = new HashSet<Principal>();
            principals.add(principal);

            // define the subject to execute our secure action as
            Subject subject = new Subject(false, principals, new HashSet<Object>(),
                    new HashSet<Object>());

            // login the subject
            Configuration cfg = getJaasKrb5TicketCfg(spn, keytabPath);
            ctx = new LoginContext("doesn't matter", subject, null, cfg);
            ctx.login();

            // create a validator for the ticket and execute it
            Krb5TicketValidateAction validateAction = new Krb5TicketValidateAction(ticket, spn);
            return Subject.doAs(subject, validateAction);
        } catch (LoginException e) {
            logger.error("Error creating validation LoginContext for " + spn + ": " + e);
        } catch (PrivilegedActionException e) {
            logger.error("Invalid ticket for " + spn + ": " + e);
        } finally {
            try {
                if(ctx!=null) { ctx.logout(); }
            } catch(LoginException e) { /* noop */ }
        }

        return null;
    }

    private class Krb5TicketValidateAction implements PrivilegedExceptionAction<String> {

        private final byte[] ticket;
        private final String spn;

        public Krb5TicketValidateAction(byte[] ticket, String spn) {
            this.ticket = ticket;
            this.spn = spn;
        }

        @Override
        public String run() throws Exception {
            final Oid spnegoOid = new Oid("1.3.6.1.5.5.2");

            GSSManager gssmgr = GSSManager.getInstance();

            // tell the GSSManager the Kerberos name of the service
            GSSName serviceName = gssmgr.createName(this.spn, GSSName.NT_USER_NAME);

            // get the service's credentials. note that this run() method was called by Subject.doAs(),
            // so the service's credentials (Service Principal Name and password) are already
            // available in the Subject
            GSSCredential serviceCredentials = gssmgr.createCredential(serviceName,
                    GSSCredential.INDEFINITE_LIFETIME, spnegoOid, GSSCredential.ACCEPT_ONLY);

            // create a security context for decrypting the service ticket
            GSSContext gssContext = gssmgr.createContext(serviceCredentials);

            // decrypt the service ticket
            System.out.println("Entering accpetSecContext...");
            gssContext.acceptSecContext(this.ticket, 0, this.ticket.length);

            // get the client name from the decrypted service ticket
            // note that Active Directory created the service ticket, so we can trust it
            String clientName = gssContext.getSrcName().toString();

            // clean up the context
            gssContext.dispose();

            // return the authenticated client name
            return clientName;
        }
    }
}
