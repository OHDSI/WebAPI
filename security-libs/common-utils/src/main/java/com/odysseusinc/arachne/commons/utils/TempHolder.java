/*
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
 * Authors: Anton Gackovka
 * Created: March 1, 2018
 */

package com.odysseusinc.arachne.commons.utils;

import java.io.File;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempHolder {

    private static final Logger LOG = LoggerFactory.getLogger(TempHolder.class);

    private static final String TOMCAT_PREFIX = "tomcat.";
    private static final String INNER_FOLDER = "work/Tomcat/localhost/ROOT";
    private static final String TEMP_FILE = "temp-empty-file.txt";

    /**
     * Protects tomcat temp ROOT folder from deleting in CentOS.
     *
     * CentOS7 checks for old files in tmp directory once a day and
     * if it faces file or directory that hasn't been changed
     * for last 10days it will delete it.
     */
    public void hold() throws IOException {
        final File tmp = getTmpFolder();
        if (tmp.exists() && tmp.isDirectory()) {
            for (final File file : getTomcatFolders()) {
                final File folder = new File(file , INNER_FOLDER);
                if (folder.getParentFile().exists() && folder.isDirectory()) {
                    final File newCheckFile = new File(folder, TEMP_FILE);
                    newCheckFile.createNewFile();
                    newCheckFile.delete();
                }
            }
            LOG.debug("Refreshed ctime and mtime for tomcat folders.");
        }
    }

    private File[] getTomcatFolders() {

        return getTmpFolder().listFiles((full, name) -> StringUtils.startsWith(name, TOMCAT_PREFIX));
    }

    private File getTmpFolder() {

        return new File(System.getProperty("java.io.tmpdir"));
    }
}
