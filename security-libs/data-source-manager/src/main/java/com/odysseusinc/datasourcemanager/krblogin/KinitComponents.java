/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Anastasiia Klochkova
 * Created: October 15, 2018
 *
 */

package com.odysseusinc.datasourcemanager.krblogin;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;

import java.nio.file.Path;
import java.nio.file.Paths;

public class KinitComponents {

    private String krbPassword;
    private String krbUser;
    private String krbRealm;
    private String[] kinitCommand;
    private Path keytabPath = Paths.get("");
    private KerberosAuthMechanism authMechanism;
    private String kinitPath;

    public String getKrbPassword() {
        return krbPassword;
    }

    public void setKrbPassword(String krbPassword) {
        this.krbPassword = krbPassword;
    }

    public String getKrbUser() {
        return krbUser;
    }

    public void setKrbUser(String krbUser) {
        this.krbUser = krbUser;
    }

    public String getKrbRealm() {
        return krbRealm;
    }

    public void setKrbRealm(String krbRealm) {
        this.krbRealm = krbRealm;
    }

    public String[] getKinitCommand() {
        return kinitCommand;
    }

    public void setKinitCommand(String[] kinitCommand) {
        this.kinitCommand = kinitCommand;
    }

    public Path getKeytabPath() {
        return keytabPath;
    }

    public void setKeytabPath(Path keytabPath) {
        this.keytabPath = keytabPath;
    }

    public KerberosAuthMechanism getAuthMechanism() {
        return authMechanism;
    }

    public void setAuthMechanism(KerberosAuthMechanism authMechanism) {
        this.authMechanism = authMechanism;
    }

    public String getKinitPath() {
        return kinitPath;
    }

    public void setKinitPath(String kinitPath) {
        this.kinitPath = kinitPath;
    }
}
