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
 * Created: August 29, 2018
 *
 */

package org.ohdsi.webapi.arachne.logging.event;

import org.ohdsi.webapi.arachne.logging.LogLevel;

public class LockoutStopEvent extends LoggingEvent {
    private String login;

    public LockoutStopEvent(Object source, LogLevel logLevel, String login) {
        super(source, logLevel);
        this.login = login;
    }

    public LockoutStopEvent(Object source, String login) {
        this(source, LogLevel.INFO, login);
    }

    public String getLogin() {
        return login;
    }
}
