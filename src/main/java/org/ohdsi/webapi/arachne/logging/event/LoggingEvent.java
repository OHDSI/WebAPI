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
 * Created: August 28, 2018
 *
 */

package org.ohdsi.webapi.arachne.logging.event;

import org.ohdsi.webapi.arachne.logging.LogLevel;
import org.springframework.context.ApplicationEvent;


public abstract class LoggingEvent extends ApplicationEvent {
    private LogLevel logLevel;

    public LoggingEvent(Object source, LogLevel logLevel) {
        super(source);
        this.logLevel = logLevel;
    }

    public LogLevel getLogLevel() {
        return this.logLevel;
    }
}
