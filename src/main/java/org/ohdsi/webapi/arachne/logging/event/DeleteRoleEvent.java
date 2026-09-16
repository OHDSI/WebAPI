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
 * Created: September 17, 2018
 *
 */

package org.ohdsi.webapi.arachne.logging.event;

import org.ohdsi.webapi.arachne.logging.LogLevel;

public class DeleteRoleEvent extends LoggingEvent {
    private long id;

    public DeleteRoleEvent(Object source, LogLevel logLevel, long id) {
        super(source, logLevel);
        this.id = id;
    }

    public DeleteRoleEvent(Object source, long id) {
        this(source, LogLevel.INFO, id);
    }

    public long getId() {
        return id;
    }
}
