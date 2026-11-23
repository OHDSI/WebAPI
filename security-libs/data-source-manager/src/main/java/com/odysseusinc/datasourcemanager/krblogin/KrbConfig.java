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
 * Created: September 26, 2018
 *
 */

package com.odysseusinc.datasourcemanager.krblogin;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.odysseusinc.datasourcemanager.krblogin.KerberosServiceImpl.KINIT_COMMAND;

public class KrbConfig {
    private static final String RUNTIME_ENV_KRB_KEYTAB = "KRB_KEYTAB";
    private static final String RUNTIME_ENV_KRB_CONF = "KRB_CONF";
    private static final String RUNTIME_ENV_KINIT_PARAMS = "KINIT_PARAMS";
    private static final String RUNTIME_ENV_KRB_PWD = "KRB_PASSWORD";
    private static final String KRB_KEYTAB_PATH = "/etc/krb.keytab";

    private Path confPath = Paths.get("");
    private RuntimeServiceMode mode;
    private KinitComponents components = new KinitComponents();

    public Map<String, String> getIsolatedRuntimeEnvs() {

        Map<String, String> krbEnvProps = new HashMap<>();
        krbEnvProps.put(RUNTIME_ENV_KRB_KEYTAB, components.getKeytabPath().toString());
        krbEnvProps.put(RUNTIME_ENV_KRB_CONF, confPath.toString());

        String kinitParamsLine = "";
        String[] kinitCommand = components.getKinitCommand();
        if (kinitCommand != null) {

            switch (components.getAuthMechanism()) {
                case PASSWORD: {
                    krbEnvProps.put(RUNTIME_ENV_KRB_PWD, components.getKrbPassword());
                    kinitParamsLine = components.getKinitPath() + KINIT_COMMAND + " " + components.getKrbUser() + "@" + components.getKrbRealm();
                    break;
                }
                case KEYTAB: {
                    String[] kinitParams = Arrays.copyOfRange(kinitCommand, 1, kinitCommand.length);
                    kinitParamsLine = StringUtils.join(kinitParams, " ").replace(components.getKeytabPath().toString(), KRB_KEYTAB_PATH);
                    break;
                }
            }
        }
        krbEnvProps.put(RUNTIME_ENV_KINIT_PARAMS, kinitParamsLine);
        return krbEnvProps;
    }

    public Path getConfPath() {

        return confPath;
    }

    public void setConfPath(Path confPath) {

        this.confPath = confPath;
    }

    public RuntimeServiceMode getMode() {

        return mode;
    }

    public void setMode(RuntimeServiceMode mode) {

        this.mode = mode;
    }

    public KinitComponents getComponents() {
        return components;
    }

    public void setComponents(KinitComponents components) {
        this.components = components;
    }
}
