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

import java.util.ArrayList;
import java.util.List;

public class CommandBuilder {

    List<String> statements = new ArrayList<>();

    private CommandBuilder() {
    }

    public static CommandBuilder newCommand() {
        return new CommandBuilder();
    }

    public CommandBuilder statement(String statement) {
        statements.add(statement);
        return this;
    }

    public CommandBuilder withParam(String param) {
        statements.add(param);
        return this;
    }

    public String[] build() {
        return statements.toArray(new String[0]);
    }
}
