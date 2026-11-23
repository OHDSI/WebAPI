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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: May 18, 2017
 *
 */

package com.odysseusinc.arachne.commons.utils;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    public static void zipDirectory(Path archiveFile, Path directory) throws IOException {

        if (Files.isDirectory(directory)) {
            final OutputStream out = Files.newOutputStream(archiveFile, CREATE, WRITE);
            try (final ZipOutputStream zip = new ZipOutputStream(out)) {

                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                        String relName = directory.relativize(file).toString();
                        addZipEntry(zip, relName, file);
                        return CONTINUE;
                    }
                });
            }
        }
    }

    private static void addZipEntry(ZipOutputStream zos, String realName, Path file) throws IOException {

        ZipEntry entry = new ZipEntry(realName);
        entry.setSize(file.toFile().length());
        zos.putNextEntry(entry);
        zos.write(Files.readAllBytes(file));
        zos.closeEntry();
    }

    public static void zipFiles(Path archiveFile, List<Path> files) throws IOException {

        if (Files.isWritable(archiveFile)) {
            try(OutputStream out = Files.newOutputStream(archiveFile, CREATE, WRITE); ZipOutputStream zip = new ZipOutputStream(out)) {

                files.forEach(f -> {
                    try {
                        addZipEntry(zip, f.getFileName().toString(), f);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

}
